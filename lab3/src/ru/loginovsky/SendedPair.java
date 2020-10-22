package ru.loginovsky;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;

class SendedPair {
    private SocketAddress socketAddress;
    private UUID uuid;
    SendedPair(SocketAddress socketAddress, UUID uuid) {
        this.socketAddress = socketAddress;
        this.uuid = uuid;
    }
    SocketAddress getSocketAddress() {
        return socketAddress;
    }
    UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendedPair that = (SendedPair) o;
        return socketAddress.equals(that.socketAddress) &&
                uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socketAddress, uuid);
    }
}
