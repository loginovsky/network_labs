package ru.loginovsky;

import java.net.SocketAddress;

public class Node {
    private SocketAddress nodeAddress;
    private SocketAddress replacerAddress;
    public Node() {}
    public Node(SocketAddress address) {
        nodeAddress = address;
    }
    public SocketAddress getNodeAddress() {
        return nodeAddress;
    }
    public SocketAddress getReplacerAddress() {
        return replacerAddress;
    }

}
