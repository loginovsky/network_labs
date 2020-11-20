package ru.loginovsky.chatnodeapp.network;

import java.net.SocketAddress;
import java.util.UUID;

public class AckMessage extends Message {
    public AckMessage(UUID uuid, String string, SocketAddress from) {
        super(uuid, string, from);
    }
    public AckMessage(UUID uuid, String string) {
        super(uuid, string);
    }
    public AckMessage(UUID uuid) {
        super(uuid,"ack");
    }
}
