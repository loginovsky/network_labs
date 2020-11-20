package ru.loginovsky.chatnodeapp.network;

public class MessageConstants {
    public static final int MSG_CLASS_BYTE_NUM = 0;
    public static final int MSG_CLASS_LENGTH = 1;
    public static final int MSG_SIZE_BYTE_NUM = 1;
    public static final int MSG_SIZE_LENGTH = 2;
    public static final int MSG_UUID_BYTE_NUM = MSG_SIZE_BYTE_NUM + MSG_SIZE_LENGTH;
    public static final int MSG_UUID_LENGTH = 16;
    public static final int MSG_STRING_BYTE_NUM = MSG_UUID_BYTE_NUM + MSG_UUID_LENGTH;
    public static final int MSG_HEADER_LENGTH = MSG_CLASS_LENGTH + MSG_SIZE_LENGTH + MSG_UUID_LENGTH;
}
