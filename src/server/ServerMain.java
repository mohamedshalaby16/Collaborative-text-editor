package server;

import network.WebSocketServer;

public class ServerMain {
    public static void main(String[] args) {
        WebSocketServer server = new WebSocketServer(9091);// 8080 denied killinng
        server.start();
    }
}