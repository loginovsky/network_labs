package ru.loginovsky.chatnodeapp.network;

import ru.loginovsky.chatnodeapp.controller.ChatController;
import ru.loginovsky.chatnodeapp.network.exceptions.NoMessageClassException;
import ru.loginovsky.chatnodeapp.network.exceptions.WrongClassByteException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.Random;
import java.util.UUID;

import static ru.loginovsky.chatnodeapp.network.MessageConstants.*;
import static ru.loginovsky.chatnodeapp.network.NetworkConstants.MAX_PACKAGE_SIZE;

public class MessageSenderReceiver implements SenderReceiver {
    DatagramSocket socket;
    int lossPercentage;
    CodeMessageTable codeMessageTable = new CodeMessageTable();
    public MessageSenderReceiver(DatagramSocket socket, int lossPercentage) {
        this.socket = socket;
        this.lossPercentage = lossPercentage;
    }
    public MessageSenderReceiver(int socketPort, int lossPercentage) throws SocketException {
        SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", socketPort);
        socket = new DatagramSocket(socketAddress);
        Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
            socket.close();
        }});
        this.lossPercentage = lossPercentage;
    }

    @Override
    public int getPort() {
        return socket.getLocalPort();
    }

    @Override
    public SocketAddress getSocketAddress() {
        return socket.getLocalSocketAddress();
    }

    @Override
    public void send(Message msg, SocketAddress receiver) {
        try {
            byte[] buf = createBuf(msg);
            fillBuf(buf, msg, codeMessageTable);
            DatagramPacket dp = new DatagramPacket(buf, buf.length, receiver);
            socket.send(dp);
        } catch (NoMessageClassException e) {
            System.err.println("Cannot send message of this type " + msg.getClass());
        } catch (IOException e) {
            System.err.println("Error sending message to " + receiver + ": " + e.getMessage());
        }
    }
    private byte[] createBuf(Message msg) {
        String messageString = msg.getMessageString();
        int messageStringLength = messageString.length();
        int bufSize = calculateBufSize(messageStringLength);
        return new byte[bufSize];
    }
    private void fillBuf(byte[] buf, Message msg, CodeMessageTable table ) throws NoMessageClassException{
        writeClassByteToBuf(buf, msg, table);
        writeStringSizeToBuf(buf, msg);
        writeUUIDToBuf(buf, msg);
        writeStringToBuf(buf, msg);
    }
    private int calculateBufSize(int messageStringLength) {
        int bufSize = messageStringLength + MSG_HEADER_LENGTH;
        if (bufSize > MAX_PACKAGE_SIZE) {
            bufSize = MAX_PACKAGE_SIZE;
        }
        return bufSize;
    }
    private void writeClassByteToBuf(byte[] buf, Message msg, CodeMessageTable table) throws NoMessageClassException {
        buf[MSG_CLASS_BYTE_NUM] = table.getClassByteByMessage(msg.getClass().asSubclass(Message.class));
    }
    private void writeStringSizeToBuf(byte[] buf, Message msg) {
        String messageString = msg.getMessageString();
        int messageStringLength = messageString.length();
        byte[] sizeArray = MessageUtils.intToByteArray(messageStringLength);
        System.arraycopy(sizeArray, 0, buf, MSG_SIZE_BYTE_NUM, MSG_SIZE_LENGTH);
    }
    private void writeUUIDToBuf(byte[] buf, Message msg) {
        UUID uuid = msg.getMessageUUID();
        byte[] uuidArray = MessageUtils.uuidToByteArr(uuid);
        System.arraycopy(uuidArray, 0, buf, MSG_UUID_BYTE_NUM, MSG_UUID_LENGTH);
    }
    private void writeStringToBuf(byte[] buf, Message msg) {
        String messageString = msg.getMessageString();
        byte[] stringArray = messageString.getBytes();
        System.arraycopy(stringArray, 0, buf, MSG_STRING_BYTE_NUM, messageString.length());
    }
    @Override
    public Message receive() {
        byte[] buf = new byte[MAX_PACKAGE_SIZE];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        while (true) {
            try {
                socket.receive(dp);
                Class<? extends Message> messageClass = getMessageClassFromBuf(buf, codeMessageTable);
                if (packageLost(messageClass)) {
                    continue;
                }
                UUID messageUUID = getMessageUUIDFromBuf(buf);
                String messageString = getMessageStringFromBuf(buf);
                return messageClass.getDeclaredConstructor(UUID.class, String.class, SocketAddress.class).newInstance(messageUUID, messageString, dp.getSocketAddress());
            } catch (WrongClassByteException e) {
                System.err.println("Wrong message class byte in package from " + dp.getSocketAddress());
            } catch (IOException e) {
                System.err.println("Exception during receiving package " + e.getMessage());
            } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
                System.err.println("Exception while creating message instance " + e.getMessage());
            }
        }
    }
    private void inspectPackage(byte[] buf) {

    }
    private boolean packageLost(Class<? extends Message> messageClass) {
        if (messageClass.equals(ChatMessage.class)) {
            if (new Random().nextInt(100) < lossPercentage) {
                return true;
            }
        }
        return false;
    }
    private byte getMessageClassByte(byte[] buf) {
        return buf[MSG_CLASS_BYTE_NUM];
    }
    private Class<? extends Message> getMessageClassFromBuf(byte[] buf, CodeMessageTable table) throws WrongClassByteException{
        byte classByte = getMessageClassByte(buf);
        return table.getMessageClassByByte(classByte);
    }
    private int getMessageSizeFromBuf(byte[] buf) {
        byte[] sizeArray = new byte[MSG_SIZE_LENGTH];
        System.arraycopy(buf, MSG_SIZE_BYTE_NUM, sizeArray, 0, MSG_SIZE_LENGTH);
        return MessageUtils.byteArrayToInt(sizeArray);
    }
    private UUID getMessageUUIDFromBuf(byte[] buf) {
        byte[] byteArr = new byte[MSG_UUID_LENGTH];
        System.arraycopy(buf, MSG_UUID_BYTE_NUM, byteArr, 0, MSG_UUID_LENGTH);
        return MessageUtils.byteArrToUUID(byteArr);
    }
    private String getMessageStringFromBuf(byte[] buf) {
        int messageSize = getMessageSizeFromBuf(buf);
        return new String(buf, MSG_STRING_BYTE_NUM, messageSize);
    }
}
