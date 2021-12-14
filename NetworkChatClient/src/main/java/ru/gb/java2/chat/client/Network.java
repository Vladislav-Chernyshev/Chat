package ru.gb.java2.chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public class Network {

    public static final int SERVER_PORT = 8189;
    public static final String SERVER_HOST = "localhost";

    private static Network INSTANCE;

    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream socketInput;
    private DataOutputStream socketOutput;

    public static Network getInstance(){
        if (INSTANCE == null){
            INSTANCE = new Network();
        }
        return INSTANCE;
    }

    private Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private Network() {
        this(SERVER_HOST, SERVER_PORT);

    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            socketInput = new DataInputStream(socket.getInputStream());
            socketOutput = new DataOutputStream(socket.getOutputStream());
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to establish connection");
            return false;
        }
    }

    public void sendMessage(String message) throws IOException {
        try {
            socketOutput.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Failed to send message to server");
            throw e;
        }
    }

    public void waitMessages(Consumer<String> messageHandler) {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()){
                        break;
                    }
                    String massage = socketInput.readUTF();
                    messageHandler.accept(massage);

                } catch (IOException e) {
                    System.err.println("Failed to read massage from server");
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();

    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
