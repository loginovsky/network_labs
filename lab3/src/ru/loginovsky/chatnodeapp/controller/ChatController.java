package ru.loginovsky.chatnodeapp.controller;

import java.net.SocketAddress;

public interface ChatController {
    void sendChatMessage(String messageString);
    void connect(SocketAddress socketAddress);
}
