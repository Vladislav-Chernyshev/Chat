package chat;

import chat.auth.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MyServer {

    private final List<ClientHandler> clients = new ArrayList<>();
    private AuthService authService;

    public void start(int port) {




        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has been started");
            authService = new AuthService();
            while (true) {
                waitAndProcessNewClientConnection(serverSocket);

            }


        } catch (IOException e) {
            System.err.println("Failed to bind port " + port);
            e.printStackTrace();
        }
    }

    private void waitAndProcessNewClientConnection(ServerSocket serverSocket) throws IOException {
        System.out.println("Waiting for new client connection...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client has been connected");
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();


    }

    public void broadcastMessage(String message, ClientHandler sender) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender){
                client.sendMessage(message);
            }
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }



    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }

}
