package ru.loginovsky.network;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.UUID;

import static ru.loginovsky.network.NetworkConstants.*;

public class Message {

    private MessageType msgType;
    private byte msgOrder;
    private final byte[] length = new byte[LENGTH_SIZE];
    private String msgStr;
    public Message(MessageType type, String msgString) {
        this.msgType = type;
        this.msgStr = msgString;
    }
    public Queue<byte[]> getQueueOfBufs() {
        Queue<byte[]> queue = new ArrayDeque<>();
        int bufSize;
        //System.out.println("msg "+ msgStr);
        byte[] strBuf = msgStr.getBytes();
        int strBufOffset = 0;
        int strBufLen = strBuf.length;
        //System.out.println("STRBUFLEN=" + strBufLen);
        int maxStrLen = MAX_PACKAGE_SIZE - HEADER_SIZE;
        byte orderOfChunks = 0;
        byte numOfChunks = (byte)(strBufLen / maxStrLen + strBufLen % maxStrLen == 0 ? 0 : 1);
        while (strBufOffset < strBufLen) {
            bufSize = Math.min(strBufLen - strBufOffset, maxStrLen);
            byte[] buf = new byte[bufSize + HEADER_SIZE];
            UUID uuid = UUID.randomUUID();
            byte[] uuidBuf = Message.uuidToByteArr(uuid);
            buf[0] = msgType.getMsgType();
            buf[1] = numOfChunks;
            buf[2] = orderOfChunks++;
            //System.out.println("BUFSIZE="+bufSize);
            buf[3] =(byte) (bufSize >> 8);
            buf[4] =(byte) (bufSize & 255);
            System.arraycopy(uuidBuf, 0, buf, HEADER_SIZE - UUID_SIZE, UUID_SIZE);
            System.arraycopy(strBuf, strBufOffset, buf, HEADER_SIZE, bufSize);
            queue.add(buf);
            strBufOffset += bufSize;
        }
        return queue;
    }
    public static byte[] uuidToByteArr(UUID uuid) {
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();
        byte[] mostSigBytes = Message.longToByteArr(mostSigBits);
        byte[] leastSigBytes = Message.longToByteArr(leastSigBits);
        byte[] byteArr = new byte[16];
        System.arraycopy(mostSigBytes, 0, byteArr, 0, 8);
        System.arraycopy(leastSigBytes, 0, byteArr, 8, 8);
        return byteArr;
    }
    public static byte[] longToByteArr(long num) {
        byte[] byteArr = new byte[8];
        for (int i = 7; i >= 0; i--) {
            byteArr[i] = (byte)((num >> ((7 - i) * 8)) & 255);
        }
        return byteArr;
    }
    public static long byteArrToLong(byte[] byteArr) {
        long num = 0;
        for (int i = 0; i < 8; i++) {
            num = ((long)byteArr[i]<<((7 - i) * 8)) | num;
        }
        return num;
    }
    public static UUID byteArrToUUID(byte[] byteArr) {
        long mostSigBits = Message.byteArrToLong(byteArr);
        byte[] leastSigBytes = new byte[8];
        System.arraycopy(byteArr, 8, leastSigBytes, 0, 8);
        long leastSigBits = Message.byteArrToLong(leastSigBytes);
        return new UUID(mostSigBits, leastSigBits);
    }
    public static UUID getUUIDFromBuf(byte[] buf) {
        byte[] byteArr = new byte[16];
        System.arraycopy(buf, HEADER_SIZE - UUID_SIZE, byteArr, 0, 16);
        return Message.byteArrToUUID(byteArr);
    }
    public static int getLenFromBuf(byte[] buf) {
        return ((buf[3]<<8) | buf[4]);
    }
}
