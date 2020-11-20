package ru.loginovsky.chatnodeapp.network;

import java.io.IOException;
import java.net.SocketAddress;

public interface SenderReceiver {
    public void send(Message msg, SocketAddress receiver);
    public Message receive();
    public SocketAddress getSocketAddress();
    public int getPort();
}
