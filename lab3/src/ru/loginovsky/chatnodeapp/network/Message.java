package ru.loginovsky.chatnodeapp.network;

import java.net.SocketAddress;
import java.util.UUID;

public abstract class Message {
    private final UUID messageUUID;
    private final String messageString;
    private SocketAddress messageSender;
    public Message(String string) {
        messageUUID = UUID.randomUUID();
        messageString = string;
    }
    public Message(UUID uuid, String string) {
        messageUUID = uuid;
        messageString = string;
    }
    public Message(String string, SocketAddress from) {
        messageUUID = UUID.randomUUID();
        messageString = string;
        messageSender = from;
    }
    public Message(UUID uuid, String string, SocketAddress from) {
        messageUUID = uuid;
        messageString = string;
        messageSender = from;
    }
    public UUID getMessageUUID() {
        return messageUUID;
    }
    public String getMessageString() {
        return messageString;
    }
    public SocketAddress getMessageSender() {
        return messageSender;
    }
}
