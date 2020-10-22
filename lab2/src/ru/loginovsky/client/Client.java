package ru.loginovsky.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private final static int BUF_SIZE = 4096;
    private final static int ANSWER_SIZE = 1;
    private final static byte YES_ANSWER = 1;
    private final static char SEPARATOR = '/';
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Client \"path to file\" \"server address\" \"server port\"");
        }
        String host = args[1];
        int port = Integer.parseInt(args[2]);
        File file = new File(args[0]);
        if (!file.canRead()) {
            System.out.println("Cannot read file " + args[0]);
            return;
        }

        try (InputStream fileIn = new FileInputStream(file);
                Socket socket = new Socket(host, port)) {
            byte[] buf = new byte[BUF_SIZE];
            OutputStream socketOut = socket.getOutputStream();
            InputStream socketIn = socket.getInputStream();
            byte[] header = (file.getName() + SEPARATOR + file.length() + SEPARATOR).getBytes();
            socketOut.write(header);
            byte[] answer = new byte[ANSWER_SIZE];
            socketIn.read(answer);
            if (answer[0] != YES_ANSWER) {
                System.out.println("Server rejected file " + file.getName());
                return;
            }
            int bytesCount;
            while ((bytesCount = fileIn.read(buf)) != -1) {
                socketOut.write(buf,0,bytesCount);
            }
            socket.shutdownOutput();
            byte[] successAnswer = new byte[ANSWER_SIZE];
            socketIn.read(successAnswer);
            if (successAnswer[0] == YES_ANSWER) {
                System.out.println("Upload completed successfully");
            } else {
                System.out.println("Upload was unsuccessful");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File " + args[0] + " does not exist");
        } catch (IOException e) {
            System.err.println("Cannot connect to host " + host + " : " + e.getMessage());
        }
    }

}
