package ru.loginovsky.chatnodeapp.network;

import java.net.SocketAddress;
import java.util.UUID;

public class PingMessage extends Message {
    public PingMessage(UUID uuid, String string, SocketAddress from) {
        super(uuid, string, from);
    }
    public PingMessage() {
        super("ping");
    }
    void doStuff() {

    }
}
