package ru.loginovsky.chatnodeapp.network;

import java.util.UUID;

import static ru.loginovsky.chatnodeapp.network.MessageConstants.MSG_SIZE_LENGTH;

public class MessageUtils {
    public static int byteArrayToInt(byte[] array) {
        int num = 0;
        int byteToInt;
        for (int i = 0; i < array.length; i++) {
            byteToInt = (int)array[i];
            if (byteToInt < 0) {
                byteToInt += 256;
            }
            num += byteToInt << ((array.length - 1 - i) * 8);
        }
        return num;
    }
    public static byte[] intToByteArray(int num) {
        /*int byteArraySize = 1;
        int tmp = num;
        while ((tmp = tmp >> 8) != 0) {
            byteArraySize++;
        }*/
        int byteArraySize = MSG_SIZE_LENGTH;
        int tmp = num;
        byte[] byteArray = new byte[byteArraySize];
        for (int i = 0; i < byteArraySize; i++) {
            tmp = num;
            byteArray[i] = (byte)(tmp >> ((byteArraySize - 1 - i) * 8));
        }
        return byteArray;
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
            long byteToLong = (long)byteArr[i];
            if (byteToLong < 0) {
                byteToLong += 256;
            }
            num = (byteToLong<<((7 - i) * 8)) | num;
        }
        return num;
    }
    public static UUID byteArrToUUID(byte[] byteArr) {
        long mostSigBits = MessageUtils.byteArrToLong(byteArr);
        //System.out.println("b2u most sig bits " + mostSigBits);
        byte[] leastSigBytes = new byte[8];
        System.arraycopy(byteArr, 8, leastSigBytes, 0, 8);
        long leastSigBits = MessageUtils.byteArrToLong(leastSigBytes);
        //System.out.println("b2u least sig bits " + leastSigBits);
        return new UUID(mostSigBits, leastSigBits);
    }
    public static byte[] uuidToByteArr(UUID uuid) {
        long mostSigBits = uuid.getMostSignificantBits();
        //System.out.println("u2b m " + mostSigBits);
        long leastSigBits = uuid.getLeastSignificantBits();
        //System.out.println("u2b l " + leastSigBits);
        byte[] mostSigBytes = MessageUtils.longToByteArr(mostSigBits);
        byte[] leastSigBytes = MessageUtils.longToByteArr(leastSigBits);
        byte[] byteArr = new byte[16];
        System.arraycopy(mostSigBytes, 0, byteArr, 0, 8);
        System.arraycopy(leastSigBytes, 0, byteArr, 8, 8);
        return byteArr;
    }
    /*public static UUID getUUIDFromBuf(byte[] buf) {
        byte[] byteArr = new byte[UUID_SIZE];
        System.arraycopy(buf, HEADER_SIZE - UUID_SIZE, byteArr, 0, UUID_SIZE);
        return Message.byteArrToUUID(byteArr);
    }*/
}
