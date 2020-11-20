package ru.loginovsky.chatnodeapp;

import ru.loginovsky.chatnodeapp.client.ChatClient;
import ru.loginovsky.chatnodeapp.controller.ChatController;
import ru.loginovsky.chatnodeapp.controller.ChatNodeController;
import ru.loginovsky.chatnodeapp.network.MessageSenderReceiver;
import ru.loginovsky.chatnodeapp.network.SenderReceiver;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class ChatNodeApplication {
    private static final int REQUIRED_ARGS_MIN = 2;
    private static final int OPTIONAL_ARGS_MIN = 4;
    private static final int SCHEDULED_POOL_SIZE = 4;
    public static void main(String[] args) {
        if (args.length < REQUIRED_ARGS_MIN) {
            System.out.println("Required arguments: name port loss_percentage [optional: node_ip node_port]");
            return;
        }
        int socketPort = Integer.parseInt(args[1]);
        try {
            int lossPercentage = Integer.parseInt(args[2]);
            //SenderReceiver senderReceiver = new MessageSenderReceiver(socket, lossPercentage);
            SenderReceiver senderReceiver = new MessageSenderReceiver(socketPort, lossPercentage);
            ChatController chatController = new ChatNodeController(senderReceiver, Executors.newScheduledThreadPool(SCHEDULED_POOL_SIZE));
            String clientName = args[0];
            ChatClient client = new ChatClient(clientName, chatController);
            if (args.length >= OPTIONAL_ARGS_MIN) {
                InetAddress nodeAddress = InetAddress.getByName(args[3]);
                int nodePort = Integer.parseInt(args[4]);
                client.connectToNode(nodeAddress, nodePort);
            }
            boolean exit = client.enableEnteringMessages();
        } catch (UnknownHostException e) {
            System.err.println("Cannot connect to " + args[3] + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Cannot create socket on port " + socketPort + ": ");
        }



    }
}
