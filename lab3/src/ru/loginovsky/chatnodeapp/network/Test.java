package ru.loginovsky.chatnodeapp.network;

import ru.loginovsky.chatnodeapp.network.exceptions.NoMessageClassException;

import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

public class Test {
    public static void main(String[] args) {
        UUID u = UUID.randomUUID();
        System.out.println("u = " + u);
        byte[] a = MessageUtils.uuidToByteArr(u);
        for(var b : a) {
            System.out.println("b = " + b);
        }
        UUID u2 = MessageUtils.byteArrToUUID(a);
        System.out.println("u2 = " + u2);
    }
}
