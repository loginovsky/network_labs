package ru.loginovsky.chatnodeapp.controller;

import ru.loginovsky.chatnodeapp.network.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.loginovsky.chatnodeapp.network.NetworkConstants.*;

public class ChatNodeController implements ChatController {
    static Logger logger = Logger.getLogger(ChatController.class.getName());
    private static String IP = "127.0.0.1";
    private SenderReceiver senderReceiver;
    private List<SocketAddress> neighbours = new ArrayList<>();
    private Map<SocketAddress, SocketAddress> neighboursReplacers = new HashMap<>();
    private SocketAddress replacer;
    private ScheduledExecutorService scheduler;
    private Map<Class<? extends Message>, Consumer<Message>> messageHandler = new HashMap<>();
    private final Map<SocketAddress, Long> neighboursLastPing = new HashMap<>();
    private int requestConnectionSent = 0;
    private ScheduledFuture<?> requestFuture;
    private SocketAddress requestAddress;
    private final Map<UUID, MessageInfo> uuidMessageInfoMap = new HashMap<>();
    private final List<UUID> receivedMessages = new ArrayList<>();
    private final List<UUID> alreadyAckedMessages = new ArrayList<>();
    public ChatNodeController(SenderReceiver senderReceiver, ScheduledExecutorService scheduler) {
        this.senderReceiver = senderReceiver;
        this.scheduler = scheduler;
        this.scheduler.scheduleAtFixedRate(this::sendPingMessage, 0, PING_DELAY_MS, TimeUnit.MILLISECONDS);
        this.scheduler.scheduleAtFixedRate(this::checkPingTimeout, 0, CHECK_PING_PERIOD_MS, TimeUnit.MILLISECONDS);
        initMessageHandler();
        getReceivingMessagesThread().start();
        logger.log(Level.INFO, "Chat node created");
    }
    private void checkPingTimeout() {
        long now = new Date().getTime();
        for (var record : neighboursLastPing.entrySet()) {
            if (now - record.getValue() > MAX_PERIOD_WITHOUT_PING_MS) {
                SocketAddress timedOutNeighbour = record.getKey();
                connectToNeighbourReplacer(timedOutNeighbour);
                cleanupNeighbour(timedOutNeighbour);
                if (timedOutNeighbour.equals(replacer)) {
                    replacer = findNewReplacer();
                    sendReplacerMessageToAll(replacer);
                }
                logger.log(Level.INFO, record.getKey() + " timed out");
            }
        }
    }
    private void cleanupNeighbour(SocketAddress neighbour) {
        neighboursLastPing.remove(neighbour);
        neighbours.remove(neighbour);
        neighboursReplacers.remove(neighbour);
    }
    private void connectToNeighbourReplacer(SocketAddress neighbour) {
        SocketAddress neighbourReplacer = neighboursReplacers.get(neighbour);
        SocketAddress me = senderReceiver.getSocketAddress();
        if (neighbourReplacer != null && !neighbourReplacer.equals(me) && !neighbours.contains(neighbourReplacer)) {
            connect(neighbourReplacer);
        }
    }
    private SocketAddress findNewReplacer() {
        if(neighbours.isEmpty()) {
            return null;
        }
        SocketAddress newReplacer = neighbours.get(0);
        return newReplacer;
    }
    @Override
    public void connect(SocketAddress socketAddress) {
        requestAddress = socketAddress;
        requestConnectionSent++;
        sendRequestConnectionMessage(socketAddress);
        requestFuture = scheduler.scheduleAtFixedRate(this::checkConnectionAnswer, CONNECTION_REQUEST_INIT_DELAY_MS, CONNECTION_REQUEST_DELAY_MS, TimeUnit.MILLISECONDS);
    }
    private void checkConnectionAnswer() {
        if (requestConnectionSent >= MAX_REQUEST_NUM) {
            requestFuture.cancel(false);
            requestConnectionSent = 0;
            logger.log(Level.INFO, "Unable to connect to " + requestAddress);
        } else {
            sendRequestConnectionMessage(requestAddress);
            requestConnectionSent++;
        }
    }
    private void sendRequestConnectionMessage(SocketAddress socketAddress) {
        Message msg = new RequestConnectionMessage();
        senderReceiver.send(msg, socketAddress);
        logger.log(Level.INFO, "Connection request sent to " + socketAddress);
    }
    @Override
    public void sendChatMessage(String messageString) {
        for (var neighbour : neighbours) {
            Message msg = new ChatMessage(messageString);
            senderReceiver.send(msg, neighbour);
            //logger.log(Level.INFO, "Chat message " + msg.getMessageUUID() + " sent to " + neighbour);
            startCheckingAck(msg, neighbour);
        }
    }
    private void sendPingMessage() {
        for (var neighbour : neighbours) {
            Message msg = new PingMessage();
            senderReceiver.send(msg, neighbour);
        }
    }
    private Thread getReceivingMessagesThread() {
        return new Thread(()-> {
            while (true) {
                Message msg = senderReceiver.receive();
                Thread messageHandleThread = new Thread(()->{
                    messageHandler.get(msg.getClass()).accept(msg);
                });
                messageHandleThread.start();
            }
        });
    }
    private void initMessageHandler() {
        messageHandler.put(PingMessage.class.asSubclass(Message.class), (Message msg)-> {
            SocketAddress msgSender = msg.getMessageSender();
            neighboursLastPing.put(msgSender, new Date().getTime());
        });
        messageHandler.put(ChatMessage.class.asSubclass(Message.class), (Message msg) -> {
            if (isAlreadyReceived(msg.getMessageUUID())) {
                return;
            }
            receivedMessages.add(msg.getMessageUUID());
            System.out.println(msg.getMessageString());
            sendAckMessage(msg.getMessageUUID(), msg.getMessageSender());
            forwardMessage(msg.getMessageString(), msg.getMessageSender());
        });
        messageHandler.put(AckMessage.class.asSubclass(Message.class), (Message msg) -> {
            stopCheckingAck(msg);
            //logger.log(Level.INFO, "Got ack for message " + msg.getMessageUUID() + " from " + msg.getMessageSender());
        });
        messageHandler.put(RequestConnectionMessage.class.asSubclass(Message.class), (Message msg) -> {
            //logger.log(Level.INFO, "Got connection request from " + msg.getMessageSender());
            sendAcceptConnectionMessage(msg.getMessageSender());
            sendReplacerMessage(replacer, msg.getMessageSender());
            addNeighbour(msg.getMessageSender());
        });
        messageHandler.put(AcceptConnectionMessage.class.asSubclass(Message.class), (Message msg) -> {
            //logger.log(Level.INFO, "Got connection accept from " + msg.getMessageSender());
            stopCheckingConnectionAnswer();
            sendReplacerMessage(replacer, msg.getMessageSender());
            addNeighbour(msg.getMessageSender());
        });
        messageHandler.put(ReplacerMessage.class.asSubclass(Message.class), (Message msg) -> {
           changeReplacerForNeighbour(msg.getMessageSender(), msg.getMessageString());
        });
    }
    private void changeReplacerForNeighbour(SocketAddress neighbour, String address) {
        String[] str = address.split(":");
        try {
            InetAddress newReplacerInetAddress = InetAddress.getByName(str[0]);
            int newReplacerPort = Integer.parseInt(str[1]);
            SocketAddress newReplacer = new InetSocketAddress(newReplacerInetAddress, newReplacerPort);
            neighboursReplacers.put(neighbour, newReplacer);
            logger.log(Level.INFO, "Changed replacer for " + neighbour + " to " + newReplacer);
        } catch (UnknownHostException e) {
            System.err.println("Error while changing replacer for " + neighbour + ": " + e.getMessage());
        }

    }
    private void forwardMessage(String text, SocketAddress sender) {
        for (var neighbour : neighbours) {
            if (!neighbour.equals(sender)) {
                Message msg = new ChatMessage(text);
                senderReceiver.send(msg, neighbour);
                //logger.log(Level.INFO, "Message forwarded to " + neighbour);
                startCheckingAck(msg, neighbour);
            }
        }
    }
    synchronized private void stopCheckingAck(Message msg) {
        UUID uuid = msg.getMessageUUID();
        MessageInfo msgInfo = uuidMessageInfoMap.get(uuid);
        if (msgInfo == null) {
            alreadyAckedMessages.add(uuid);
            return;
        }
        msgInfo.future.cancel(false);
        uuidMessageInfoMap.remove(uuid);
    }
    synchronized private void startCheckingAck(Message msg, SocketAddress receiver) {
        UUID uuid = msg.getMessageUUID();
        if (alreadyAckedMessages.contains(uuid)) {
            alreadyAckedMessages.remove(uuid);
            return;
        }
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(getCheckingAckRunnable(uuid), MSG_SEND_INIT_DELAY_MS, MSG_SEND_PERIOD_MS, TimeUnit.MILLISECONDS);
        uuidMessageInfoMap.put(uuid, new MessageInfo(1, msg, scheduledFuture, receiver));
    }

    private Runnable getCheckingAckRunnable(UUID uuid) {
        return new Runnable() {
            @Override
            public void run() {
                MessageInfo msgInfo = uuidMessageInfoMap.get(uuid);
                if (msgInfo == null) {
                    return;
                }
                if (msgInfo.messageSentCount >= MAX_MSG_SEND_NUM) {
                    msgInfo.future.cancel(false);
                    uuidMessageInfoMap.remove(uuid);
                    return;
                }
                senderReceiver.send(msgInfo.msg, msgInfo.receiver);
                msgInfo.messageSentCount++;
                //logger.log(Level.INFO, "Chat message " + uuid + " sent to " + msgInfo.receiver + " for " + msgInfo.messageSentCount + " time");
                uuidMessageInfoMap.put(uuid, msgInfo);
            }
        };
    }

    private void sendAckMessage(UUID uuid, SocketAddress sender) {
        Message msg = new AckMessage(uuid);
        senderReceiver.send(msg, sender);
        //logger.log(Level.INFO, "Sent ack to " + sender);
    }
    private void sendAcceptConnectionMessage(SocketAddress requester) {
        Message msg = new AcceptConnectionMessage();
        senderReceiver.send(msg, requester);
        logger.log(Level.INFO, "Sent connection accept to " + requester);
    }
    private void stopCheckingConnectionAnswer() {
        if (requestFuture!=null)
            requestFuture.cancel(false);
        requestConnectionSent = MAX_REQUEST_NUM;
    }
    private void addNeighbour(SocketAddress neighbour) {
        neighbours.add(neighbour);
        //logger.log(Level.INFO, neighbour + " added to list of neighbours");
        if (replacer == null) {
            replacer = neighbour;
            sendReplacerMessageToAll(neighbour);
        }
    }
    private void sendReplacerMessageToAll(SocketAddress replacer) {
        for (var neighbour : neighbours) {
            sendReplacerMessage(replacer, neighbour);
            //logger.log(Level.INFO, "New replacer " + replacer + " sent to " + neighbour);
        }
    }
    private void sendReplacerMessage(SocketAddress replacer, SocketAddress receiver) {
        if (replacer == null) {
            return;
        }
        String addressString = replacer.toString().substring(1);
        Message msg = new ReplacerMessage(addressString);
        senderReceiver.send(msg, receiver);
        logger.log(Level.INFO, "Replacer" + replacer +" sent to " + receiver);
    }
    private boolean isAlreadyReceived(UUID uuid) {
        for (var msgUUID: receivedMessages) {
            if (msgUUID.equals(uuid)) {
                return true;
            }
        }
        return false;
    }
    private class MessageInfo {
        boolean cancelled;
        int messageSentCount;
        ScheduledFuture<?> future;
        Message msg;
        SocketAddress receiver;
        public MessageInfo(int messageSentCount, Message msg, ScheduledFuture<?> future, SocketAddress receiver) {
            this.messageSentCount = messageSentCount;
            this.msg = msg;
            this.future = future;
            this.receiver = receiver;
            cancelled = false;
        }
    }
}
