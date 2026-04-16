package launcher;

import model.Document;
import network.WebSocketClient;
import network.MessageHandler;
import operations.InsertCharacterOperation;
import operations.DeleteCharacterOperation;

public class ClientLauncher {
    private static int clock = 0;

    public static void main(String[] args) {
        Document doc = new Document();

        // Create an array to hold the client reference (workaround for "might not be
        // initialized")
        final WebSocketClient[] clientHolder = new WebSocketClient[1];

        WebSocketClient client = new WebSocketClient("localhost", 9091, new WebSocketClient.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                Object op = MessageHandler.messageToOperation(message);
                System.out.println(">>> REMOTE EDIT RECEIVED: " + op);
                doc.applyRemote(op);
                System.out.println(">>> DOCUMENT TEXT NOW: \"" + doc.getText() + "\"");
                System.out.println();
            }

            @Override
            public void onConnected() {
                System.out.println("✓ Connected to server!");
                // FIXED: Use clientHolder[0] instead of 'client'
                doc.setWebSocketClient(clientHolder[0]);

                System.out.println("Creating initial block...");

                // Simulate user typing
                simulateUserTyping(doc);
            }

            @Override
            public void onDisconnected() {
                System.out.println("✗ Disconnected from server!");
            }
        });

        // Store the client in the array BEFORE calling connect
        clientHolder[0] = client;

        client.connect();

        // Keep program running
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    }

    private static void simulateUserTyping(Document doc) {
        try {
            Thread.sleep(1000);

            System.out.println("\n=== SIMULATING USER TYPING ===");

            String lastId = null;

            // H
            clock++;
            InsertCharacterOperation op1 = new InsertCharacterOperation(1, clock, 'H', null, "block-1");
            System.out.println("Local insert: 'H' (id: 1-" + clock + ", parent: null)");
            doc.apply(op1);
            lastId = "1-" + clock;
            Thread.sleep(500);

            // e
            clock++;
            InsertCharacterOperation op2 = new InsertCharacterOperation(1, clock, 'e', lastId, "block-1");
            System.out.println("Local insert: 'e' (id: 1-" + clock + ", parent: " + lastId + ")");
            doc.apply(op2);
            lastId = "1-" + clock;
            Thread.sleep(500);

            // l
            clock++;
            InsertCharacterOperation op3 = new InsertCharacterOperation(1, clock, 'l', lastId, "block-1");
            System.out.println("Local insert: 'l' (id: 1-" + clock + ", parent: " + lastId + ")");
            doc.apply(op3);
            lastId = "1-" + clock;
            Thread.sleep(500);

            // l
            clock++;
            InsertCharacterOperation op4 = new InsertCharacterOperation(1, clock, 'l', lastId, "block-1");
            System.out.println("Local insert: 'l' (id: 1-" + clock + ", parent: " + lastId + ")");
            doc.apply(op4);
            lastId = "1-" + clock;
            Thread.sleep(500);

            // o
            clock++;
            InsertCharacterOperation op5 = new InsertCharacterOperation(1, clock, 'o', lastId, "block-1");
            System.out.println("Local insert: 'o' (id: 1-" + clock + ", parent: " + lastId + ")");
            doc.apply(op5);

            System.out.println("\n=== FINAL RESULT ===");
            System.out.println("Final text: \"" + doc.getText() + "\"");
            System.out.println("Expected: \"Hello\"");

            if (doc.getText().equals("Hello")) {
                System.out.println("✓ TEST PASSED!");
            } else {
                System.out.println("✗ TEST FAILED!");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}