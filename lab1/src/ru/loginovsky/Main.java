package ru.loginovsky;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private final static String IPV4_MULTICAST_ADDRESS = "239.192.0.2";
    private final static String IPV6_MULTICAST_ADDRESS = "fff8::2";
    private final static String MSG = "yo";
    private final static int PORT = 1234;
    private final static int BUF_SIZE = 1024;
    private final static long UPDATE_RATE = 1000L;
    private final static long TIMEOUT_UPDATE = 100L;
    private final static long INITIAL_DELAY = 0L;
    private final static long TIMEOUT = 1500L;
    public static void main(String[] args) {
        InetAddress groupAddress;
        final int groupPort = args.length < 2 ? PORT : Integer.parseInt(args[1]);
        Map<String, Long> activeCopies = new HashMap<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        try {
            if (args.length < 1) {
                groupAddress = InetAddress.getByName(IPV4_MULTICAST_ADDRESS);
            } else {
                groupAddress = InetAddress.getByName(args[0]);
            }
            MulticastSocket sendSocket = new MulticastSocket();
            MulticastSocket recvSocket = new MulticastSocket(PORT);
            sendSocket.joinGroup(groupAddress);
            recvSocket.joinGroup(groupAddress);
            Thread sendingThread = new Thread(() -> {
                try {
                    DatagramPacket dp = new DatagramPacket(MSG.getBytes(), MSG.length(), groupAddress, groupPort);
                    sendSocket.send(dp);
                    for (var record : activeCopies.entrySet()) {
                        System.out.println(record.getKey());
                    }
                    System.out.println();

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            });
            Thread receivingThread = new Thread(() -> {
                byte[] buf = new byte[BUF_SIZE];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                while(true) {
                    try {
                        recvSocket.receive(dp);
                        activeCopies.put(dp.getAddress() + ":" + dp.getPort(), new Date().getTime());
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }
            });
            Thread timeoutThread = new Thread(() -> {
                long now = new Date().getTime();
                for (var record : activeCopies.entrySet()) {
                    if (now - record.getValue() > TIMEOUT) {
                        activeCopies.remove(record.getKey());
                    }
                }
            });
            scheduler.scheduleAtFixedRate(sendingThread, INITIAL_DELAY, UPDATE_RATE, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(timeoutThread, INITIAL_DELAY, TIMEOUT_UPDATE, TimeUnit.MILLISECONDS);
            receivingThread.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}
