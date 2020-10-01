package ru.loginovsky.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server {
    private final static long SIZE_LIMIT = 1_000_000_000_000L;
    private final static int ANSWER_SIZE = 1;
    private final static String UPLOADS_DIRNAME = "uploads";
    private final static long TIME_INTERVAL = 3_000_000_000L;
    public static void main(String[] args) {
        String pwd = System.getProperty("user.dir");
        File uploadsDir = new File(pwd, UPLOADS_DIRNAME);
        uploadsDir.mkdir();
        if (args.length < 1) {
            System.out.println("Enter port number for a server");
            return;
        }
        int socketPort = Integer.parseInt(args[0]);
        try {
            ServerSocket serverSocket = new ServerSocket(socketPort);
            while (true) {
                Socket socket = serverSocket.accept();
                getConnectionThread(socket, uploadsDir).start();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
    static Thread getConnectionThread(Socket socket, File uploadsDir) {
        return new Thread(()-> {
            try {
                String[] header = readHeader(socket);
                String filename = formatFilename(header[0], uploadsDir);
                long filesize = Long.parseLong(header[1]);
                boolean accepted = fileAccepted(filename, filesize, uploadsDir);
                sendAnswer(socket, accepted);
                if (!accepted) {
                    return;
                }
                long readBytes = readFile(socket, filename, uploadsDir);
                boolean success = (readBytes == filesize);
                sendAnswer(socket, success);
                if (success) {
                    System.out.println(filename + " successfully uploaded to " + uploadsDir.getPath());
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    static String[] readHeader(Socket socket) throws IOException {
        InputStream socketIn = socket.getInputStream();
        int bufSize = socket.getReceiveBufferSize();
        System.out.println(bufSize);
        byte[] buf = new byte[bufSize];
        int bytesCount;
        StringBuilder str = new StringBuilder();
        while ((bytesCount = socketIn.read(buf)) != -1) {
            str.append(new String(buf, 0, bytesCount, StandardCharsets.UTF_8));
            if(str.toString().matches("^.+\\/\\d+\\/$")) {
                break;
            }
        }
        return str.toString().split("\\/");
    }
    static boolean fileAccepted(String filename, long filesize, File uploadsDir) {
        long freeSpace = uploadsDir.getUsableSpace();
        System.out.println(freeSpace);
        return filesize < freeSpace && filesize < SIZE_LIMIT;
    }
    static void sendAnswer(Socket socket, boolean accepted) throws IOException{
        OutputStream socketOut = socket.getOutputStream();
        byte[] answer = new byte[ANSWER_SIZE];
        answer[0] = accepted ? (byte)1 : (byte)0;
        socketOut.write(answer);
    }
    static long readFile(Socket socket, String filename, File uploadsDir) throws IOException {
        InputStream socketIn = socket.getInputStream();
        int bufSize = socket.getReceiveBufferSize();
        System.out.println(bufSize);
        byte[] buf = new byte[bufSize];
        File file = new File(uploadsDir, filename);
        OutputStream fileOut = new FileOutputStream(file);
        int bytesCount = 0;
        int lastBytes = 0;
        long totalBytesCount = 0L;
        long beforeTime = System.nanoTime();
        long startIntervalTime = 0L, startTime = 0L, finishTime = TIME_INTERVAL + 1;
        boolean statsPrinted = false;
        boolean firstTime = true;
        while (bytesCount != -1) {
            if (firstTime) {
                startIntervalTime = System.nanoTime();
                firstTime = false;
            } else if (finishTime - startIntervalTime > TIME_INTERVAL) {
                System.out.println(filename);
                System.out.println("Current speed: " + ((long)bytesCount * 1_000_000L / (finishTime - startTime)) + " Kb/s");
                System.out.println("Average speed: " + (totalBytesCount * 1_000_000L / (finishTime - beforeTime)) + " Kb/s");
                System.out.println();
                startIntervalTime = System.nanoTime();
                statsPrinted = true;
            }
            startTime = System.nanoTime();
            bytesCount = socketIn.read(buf);
            finishTime = System.nanoTime();
            if (bytesCount != -1) {
                fileOut.write(buf, 0, bytesCount);
                totalBytesCount += bytesCount;
                lastBytes = bytesCount;
            }
        }
        if(!statsPrinted) {
            System.out.println(filename);
            System.out.println("Current speed: " + ((long) lastBytes * 1_000_000L / (finishTime - startTime)) + " Kb/s");
            System.out.println("Average speed: " + (totalBytesCount * 1_000_000L / (finishTime - beforeTime)) + " Kb/s");
            System.out.println();
        }
        fileOut.close();
        return totalBytesCount;
    }
    static String formatFilename(String filename, File uploadsDir) {
        String LEFT_BRACKET = "(";
        String RIGHT_BRACKET = ")";
        File file = new File(uploadsDir, filename);
        String newFilename = filename;
        int i = 0;
        while (file.exists()) {
            newFilename = filename + LEFT_BRACKET + i + RIGHT_BRACKET;
            file = new File(uploadsDir, newFilename);
            i++;
        }
        return newFilename;
    }
}
