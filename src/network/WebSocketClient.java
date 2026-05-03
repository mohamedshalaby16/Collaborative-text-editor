package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class WebSocketClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;
    private volatile boolean connected;
    private final String serverAddress;
    private final int serverPort;
    private static final int CONNECT_TIMEOUT_MS = 5000;

    // Interface for receiving messages from server
    public interface MessageListener {
        void onMessageReceived(String message);

        void onConnected();

        void onDisconnected();
    }

    public WebSocketClient(String serverAddress, int serverPort, MessageListener listener) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.listener = listener;
        this.connected = false;
    }

    public void connect() {
        try {
            socket = new Socket();
            SocketAddress endpoint = new InetSocketAddress(serverAddress, serverPort);
            socket.connect(endpoint, CONNECT_TIMEOUT_MS);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;

            if (listener != null) {
                listener.onConnected();
            }

            // Start listening thread
            Thread listenerThread = new Thread(this::listenForMessages, "collab-client-listener");
            listenerThread.setDaemon(true);
            listenerThread.start();

            System.out.println("Connected to server at " + serverAddress + ":" + serverPort);

        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            disconnect();
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                if (listener != null) {
                    listener.onMessageReceived(message);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection lost: " + e.getMessage());
        } finally {
            disconnect();
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }

    public void sendMessage(String message) {
        if (connected && out != null) {
            out.println(message);
        } else {
            System.out.println("Cannot send message: not connected");
        }
    }

    public void disconnect() {
        connected = false;
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

    public boolean isConnected() {
        return connected;
    }
}
