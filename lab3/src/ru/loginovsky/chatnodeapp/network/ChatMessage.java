package ru.loginovsky.chatnodeapp.network;

import java.net.SocketAddress;
import java.util.UUID;

public class ChatMessage extends Message {
    public ChatMessage(UUID uuid, String string, SocketAddress from) {
        super(uuid, string, from);
    }
    public ChatMessage(String string, SocketAddress from) {
        super(string, from);
    }
    public ChatMessage(UUID uuid, String string) {
        super(uuid, string);
    }
    public ChatMessage(String string) {
        super(string);
    }

}
