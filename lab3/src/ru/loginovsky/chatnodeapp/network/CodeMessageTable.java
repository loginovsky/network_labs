package ru.loginovsky.chatnodeapp.network;

import ru.loginovsky.chatnodeapp.network.exceptions.NoMessageClassException;
import ru.loginovsky.chatnodeapp.network.exceptions.WrongClassByteException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeMessageTable {
    private final Map<Byte, Class <? extends Message>> table;
    private final Map<Class <? extends  Message>, Byte> inverseTable;
    public CodeMessageTable() {
        table = new HashMap<>();
        table.put((byte)1, PingMessage.class.asSubclass(Message.class));
        table.put((byte)2, ChatMessage.class.asSubclass(Message.class));
        table.put((byte)3, AckMessage.class.asSubclass(Message.class));
        table.put((byte)4, RequestConnectionMessage.class.asSubclass(Message.class));
        table.put((byte)5, AcceptConnectionMessage.class.asSubclass(Message.class));
        table.put((byte)6, ReplacerMessage.class.asSubclass(Message.class));
        inverseTable = table.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }
    public Class<? extends Message> getMessageClassByByte(byte b) throws WrongClassByteException {
        Class<? extends Message> messageClass = table.get(b);
        if (messageClass == null) {
            throw new WrongClassByteException();
        }
        return messageClass;
    }
    public byte getClassByteByMessage(Class<? extends Message> messageClass) throws NoMessageClassException {
        Byte classByte = inverseTable.get(messageClass);
        if (classByte == null) {
            throw new NoMessageClassException();
        }
        return classByte;
    }
}
