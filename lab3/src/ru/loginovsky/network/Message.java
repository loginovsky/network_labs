package ru.loginovsky.network;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

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
            buf[0] = msgType.getMsgType();
            buf[1] = numOfChunks;
            buf[2] = orderOfChunks++;
            //System.out.println("BUFSIZE="+bufSize);
            buf[3] =(byte) (bufSize >> 8);
            buf[4] =(byte) (bufSize & 255);
            System.arraycopy(strBuf, strBufOffset, buf, HEADER_SIZE, bufSize);
            queue.add(buf);
            strBufOffset += bufSize;
        }
        return queue;
    }
    public static int getLenFromBuf(byte[] buf) {
        return ((buf[3]<<8) | buf[4]);
    }
}
