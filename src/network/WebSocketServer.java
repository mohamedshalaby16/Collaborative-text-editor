package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList; // FIX: thread-safe list

public class WebSocketServer {

    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private boolean running;

    public WebSocketServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clients = new CopyOnWriteArrayList<>(); // FIX: was new ArrayList<>()
            running = true;
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            System.out.println("Failed to start server: " + e.getMessage());
        }
    }

    public void start() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                new Thread(handler).start();
                System.out.println("New client connected. Total clients: " + clients.size());
            } catch (IOException e) {
                System.out.println("Error accepting connection: " + e.getMessage());
            }
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        System.out.println("Broadcasting to " + (clients.size() - 1) + " other clients: " + message);
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected. Total clients: " + clients.size());
    }

    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class to handle each client in its own thread
    class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private WebSocketServer server;
        private String clientId;

        public ClientHandler(Socket socket, WebSocketServer server) {
            this.socket = socket;
            this.server = server;
            this.clientId = socket.getRemoteSocketAddress().toString();

            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.out.println("Error setting up client streams: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received from " + clientId + ": " + message);
                    server.broadcast(message, this); // broadcast to all OTHER clients
                }
            } catch (IOException e) {
                System.out.println("Client " + clientId + " disconnected");
            } finally {
                close();
                server.removeClient(this);
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        public void close() {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}