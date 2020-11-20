package ru.loginovsky.chatnodeapp.network;

import java.net.SocketAddress;
import java.util.UUID;

public class AcceptConnectionMessage extends Message {
    public AcceptConnectionMessage(UUID uuid, String string, SocketAddress from) {
        super(uuid, string, from);
    }
    public AcceptConnectionMessage(UUID uuid, String string) {
        super(uuid, string);
    }
    public AcceptConnectionMessage() {
        super("accept:)");
    }
    void doStuff() {

    }
}
