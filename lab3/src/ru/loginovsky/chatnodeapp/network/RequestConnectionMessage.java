package ru.loginovsky.chatnodeapp.network;

import java.net.SocketAddress;
import java.util.UUID;

public class RequestConnectionMessage extends Message {
    public RequestConnectionMessage(UUID uuid, String string, SocketAddress from) {
        super(uuid, string, from);
    }
    public RequestConnectionMessage(UUID uuid, String string) {
        super(uuid, string);
    }
    public RequestConnectionMessage() {
        super("request");
    }
    void doStuff() {

    }
}
