package ru.loginovsky.network;

import java.util.HashMap;
import java.util.Map;

public enum MessageType {
    CONNECTION_REQUEST((byte)1), CONNECTION_ANSWER((byte)2), CHAT_MESSAGE((byte)3), CHAT_MESSAGE_LONG((byte)4);

    private byte msgType;
    private static Map<Byte, MessageType> map = new HashMap<>();
    static {
        for (MessageType messageType: MessageType.values()) {
            map.put(messageType.msgType, messageType);
        }
    }
    MessageType(byte msgType) {
        this.msgType = msgType;
    }
    public byte getMsgType() {
        return msgType;
    }
    public static MessageType valueOf(byte msgType) {
        return map.get(msgType);
    }
}
