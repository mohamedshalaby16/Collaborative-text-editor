package network;

import server.CollaborationSession;
import model.UserRole;
import persistence.SessionStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.UUID;

public class WebSocketServer {

    private ServerSocket serverSocket;
    private boolean running;

    // Map: documentId -> CollaborationSession (one room per document)
    private final Map<String, CollaborationSession> sessions = new ConcurrentHashMap<>();

    // Keep track of all connected sockets (for cleanup)
    private final List<ClientHandler> allClients = new CopyOnWriteArrayList<>();

    // Task 4: Persistence storage
    private final SessionStorage storage;

    public WebSocketServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            storage = new SessionStorage();
            loadExistingSessions(); // Task 4: Load saved sessions on startup
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            System.out.println("Failed to start server: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Task 4: Load saved sessions from disk
    private void loadExistingSessions() {
        List<SessionStorage.SessionData> savedSessions = storage.loadAllSessions();
        for (SessionStorage.SessionData data : savedSessions) {
            CollaborationSession session = new CollaborationSession(
                    data.getDocumentId(),
                    data.getEditorCode(),
                    data.getViewerCode());
            // Load operation history into the session
            for (String op : data.getOperationHistory()) {
                session.saveOperation(op);
            }
            sessions.put(data.getDocumentId(), session);
            System.out.println("Loaded session: " + data.getDocumentId() +
                    " with " + data.getOperationHistory().size() + " operations");
        }
    }

    public void start() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                allClients.add(handler);
                new Thread(handler).start();
                System.out.println("New client connected. Total sockets: " + allClients.size());
            } catch (IOException e) {
                System.out.println("Error accepting connection: " + e.getMessage());
            }
        }
    }

    // ========== Session Management ==========

    private String generateShareCode() {
        // Generate random 8-character code
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public CollaborationSession createSession(String creatorUsername) {
        String documentId = "doc_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4);
        String editorCode = "E-" + generateShareCode();
        String viewerCode = "V-" + generateShareCode();

        CollaborationSession session = new CollaborationSession(documentId, editorCode, viewerCode);
        sessions.put(documentId, session);

        // Task 4: Save the newly created session
        storage.saveSession(session);

        System.out.println("Created new session: " + documentId);
        return session;
    }

    public CollaborationSession getSession(String documentId) {
        return sessions.get(documentId);
    }

    /**
     * Check if a code is valid and return which document it belongs to
     */
    public String validateJoinCode(String code) {
        for (CollaborationSession session : sessions.values()) {
            if (code.equals(session.getEditorCode())) {
                return session.getDocumentId();
            }
            if (code.equals(session.getViewerCode())) {
                return session.getDocumentId();
            }
        }
        return null; // Invalid code
    }

    /**
     * Get the role (EDITOR or VIEWER) for a given code
     */
    public UserRole getRoleForCode(String code) {
        for (CollaborationSession session : sessions.values()) {
            if (code.equals(session.getEditorCode())) {
                return UserRole.EDITOR;
            }
            if (code.equals(session.getViewerCode())) {
                return UserRole.VIEWER;
            }
        }
        return null;
    }

    // ========== Message Routing ==========

    /**
     * Route a message to the correct session
     * This is the main method that handles all incoming messages
     */
    public void routeMessage(String message, ClientHandler sender) {
        String messageType = getMessageType(message);

        // Handle CREATE_SESSION request
        if ("CREATE_SESSION".equals(messageType)) {
            int userId = getUserIdFromMessage(message);
            String username = getUsernameFromMessage(message);
            CollaborationSession session = createSession(username);

            String response = MessageHandler.sessionCreatedMessage(
                    session.getDocumentId(),
                    session.getEditorCode(),
                    session.getViewerCode());
            sender.sendMessage(response);
            sender.currentSession = session;
            sender.currentDocumentId = session.getDocumentId();
            sender.userRole = UserRole.EDITOR;
            session.addClient(sender);
            System.out.println("User " + username + " created document " + session.getDocumentId());
            return;
        }

        // Handle JOIN_SESSION request
        if ("JOIN_SESSION".equals(messageType)) {
            String code = getJoinCodeFromMessage(message);
            String documentId = validateJoinCode(code);

            if (documentId == null) {
                sender.sendMessage(MessageHandler.joinRejectedMessage("Invalid share code"));
                System.out.println("Rejected join attempt with invalid code: " + code);
                return;
            }

            CollaborationSession session = sessions.get(documentId);
            UserRole role = getRoleForCode(code);
            String username = getUsernameFromMessage(message);

            sender.currentSession = session;
            sender.currentDocumentId = documentId;
            sender.userRole = role;
            session.addClient(sender);

            // Add client to session
            session.addClient(sender);

            // Send join acceptance with operation history
            String response = MessageHandler.joinAcceptedMessage(
                    documentId,
                    role.toString(),
                    role == UserRole.EDITOR ? session.getEditorCode() : null,
                    role == UserRole.EDITOR ? session.getViewerCode() : null,
                    session.getOperationHistory());
            sender.sendMessage(response);

            // Task 3: Replay all past operations to the new joiner
            session.replayHistoryTo(sender);


            System.out.println("User " + username + " joined document " + documentId + " as " + role);
            return;
        }

        // Handle regular messages (edits, cursor, leave)
        String documentId = MessageHandler.getDocumentId(message);
        if (documentId == null) {
            System.out.println("Message has no documentId - dropping: " + message);
            return;
        }

        CollaborationSession session = sessions.get(documentId);
        if (session == null) {
            System.out.println("No session found for documentId: " + documentId + " - dropping.");
            return;
        }

        // Save edit operations to history (for late joiners) - TASK 3
        boolean isEdit = messageType != null && (messageType.equals("INSERT_CHAR") ||
                messageType.equals("DELETE_CHAR") ||
                messageType.equals("INSERT_BLOCK") ||
                messageType.equals("DELETE_BLOCK"));

        if (isEdit) {
            if (sender.userRole != UserRole.EDITOR) {
                sender.sendMessage(MessageHandler.permissionDeniedMessage("Viewers cannot edit this document"));
                System.out.println("Rejected edit from non-editor client: " + sender.getClientId());
                return;
            }
            session.saveOperation(message);
            // Task 4: Auto-save after each operation (you might want to batch this)
            storage.saveSession(session);
        }

        // Broadcast to everyone else in the SAME session only
        session.broadcast(message, sender);
    }

    // ========== Helper Methods ==========

    private String getMessageType(String message) {
        try {
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(message).getAsJsonObject();
            if (json.has("type")) {
                return json.get("type").getAsString();
            }
        } catch (Exception e) {
            // Ignore parse errors
        }
        return null;
    }

    private int getUserIdFromMessage(String message) {
        try {
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(message).getAsJsonObject();
            if (json.has("userId")) {
                return json.get("userId").getAsInt();
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private String getUsernameFromMessage(String message) {
        try {
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(message).getAsJsonObject();
            if (json.has("username")) {
                return json.get("username").getAsString();
            }
        } catch (Exception e) {
        }
        return "unknown";
    }

    private String getJoinCodeFromMessage(String message) {
        try {
            com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(message).getAsJsonObject();
            if (json.has("code")) {
                return json.get("code").getAsString();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void removeClient(ClientHandler client) {
        allClients.remove(client);
        // Remove from whichever session this client belongs to
        if (client.currentSession != null) {
            client.currentSession.removeClient(client);

            // Task 4: Save session when last client leaves
            if (client.currentSession.isEmpty()) {
                storage.saveSession(client.currentSession);
                System.out.println("Session " + client.currentDocumentId + " is empty, saved to disk");
            }
        }
        System.out.println("Client removed. Total sockets: " + allClients.size());
    }

    public void stop() {
        running = false;

        // Task 4: Save all sessions before shutdown
        System.out.println("Saving all sessions before shutdown...");
        for (CollaborationSession session : sessions.values()) {
            storage.saveSession(session);
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========== ClientHandler Inner Class ==========

    public class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private WebSocketServer server;
        private String clientId;

        // Track which session this client is in
        public CollaborationSession currentSession = null;
        public String currentDocumentId = null;
        public UserRole userRole = null;

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
                    server.routeMessage(message, this);
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
                System.out.println("Sent to " + clientId + ": " + message);
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

        public String getClientId() {
            return clientId;
        }
    }
}
