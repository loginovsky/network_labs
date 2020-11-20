package ru.loginovsky.chatnodeapp.client;

import ru.loginovsky.chatnodeapp.controller.ChatController;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Scanner;

public class ChatClient {
    private final ChatController chatController;
    private final String clientName;
    public ChatClient(String clientName, ChatController chatController) {
        this.clientName = clientName;
        this.chatController = chatController;
    }
    public void connectToNode(InetAddress nodeAddress, int nodePort) {
        SocketAddress socketAddress = new InetSocketAddress(nodeAddress, nodePort);
        chatController.connect(socketAddress);
    }
    public void startReceivingMessages() {

    }
    public boolean enableEnteringMessages() {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();

        while (!line.equals("exit")) {
            chatController.sendChatMessage(clientName + ": " + line);
            line = scanner.nextLine();
        }
        return true;
    }
}
