package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ClientHandler {

    private static final String AUTH_OK_COMMAND = "/authOk";
    private static final String AUTH_COMMAND = "/auth";
    private static final String AUTH_NOT_OK_COMMAND = "/authNotOk";
    private MyServer server;
    private final Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String username;


    public ClientHandler(MyServer server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void handle() throws IOException {
        inputStream = new DataInputStream(clientSocket.getInputStream());
        outputStream = new DataOutputStream(clientSocket.getOutputStream());

        new Thread(() -> {
            try {
                authentication();
                readMessages();
            } catch (IOException e) {
                System.err.println("Failed to process message from client");
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    System.err.println("Failed to close connection");
                }
            }
        }).start();
    }

    private void authentication() throws IOException {
        while (true) {
            String message = inputStream.readUTF();
            if (message.startsWith(AUTH_COMMAND)) {
                String[] parts = message.split(" ");
                String login = parts[1];
                String password = parts[2];


                username = server.getAuthService().getUsernameByLoginAndPassword(login, password);
                if (username == null) {
                    sendMessage("Некорректные логин и пароль!");

                }else if (server.findClient(username)) {
                    sendMessage(AUTH_NOT_OK_COMMAND);

                } else {
                    sendMessage(String.format("%s %s", AUTH_OK_COMMAND, username));
                    server.subscribe(this);
                    return;

                }
            }
        }
    }

    private void closeConnection() throws IOException {
        server.unsubscribe(this);
        clientSocket.close();
    }

    private void readMessages() throws IOException {
        while (true) {
            String message = inputStream.readUTF().trim();
            System.out.println("message: " + message);
            if (message.startsWith("/end")) {
                return;
            } else if (message.startsWith("/w ")){
                String [] tokens = message.split("\\s");
                String username = tokens[1];
                String msg = message.substring(4 + username.length());
                server.privateMessage(this, username, msg);
            }else {
                processMessage(message);

            }
        }
    }

    private void processMessage(String message) throws IOException {
        server.broadcastMessage(message, this);
    }

    public void sendMessage(String message) throws IOException {
        outputStream.writeUTF(message);
    }
    public String getUsername() {
        return username;
    }


}
