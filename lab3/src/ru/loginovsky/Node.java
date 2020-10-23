package ru.loginovsky;

import java.net.SocketAddress;

public class Node {
    private SocketAddress nodeAddress;
    private SocketAddress replacerAddress;
    private int nodeSenderPort;
    public Node() {}
    public Node(SocketAddress address, int port) {
        nodeAddress = address;
        nodeSenderPort = port;
    }
    public SocketAddress getNodeAddress() {
        return nodeAddress;
    }
    public SocketAddress getReplacerAddress() {
        return replacerAddress;
    }
    public int getNodeSenderPort() {
        return nodeSenderPort;
    }
}
