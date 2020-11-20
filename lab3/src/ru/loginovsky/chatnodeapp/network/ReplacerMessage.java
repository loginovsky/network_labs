package ru.loginovsky.chatnodeapp.network;

import java.net.SocketAddress;
import java.util.UUID;

public class ReplacerMessage extends Message {
    public ReplacerMessage(UUID uuid, String string, SocketAddress from) {
        super(uuid, string, from);
    }
    public ReplacerMessage(String address) {
        super(address);
    }
}
