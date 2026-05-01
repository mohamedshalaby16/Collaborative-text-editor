package server;

import network.ClientHandler;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CollaborationSession - Manages a single document session
 * 
 * Before Phase 3: Server had ONE room and broadcast to everyone
 * After Phase 3: Each document has its own session
 * 
 * A session stores:
 * - Document ID (unique identifier)
 * - Editor code (for editors to join)
 * - Viewer code (for viewers to join)
 * - Only clients connected to THIS document
 * - Full operation history for late joiners
 */
public class CollaborationSession {

    private final String documentId;
    private final String editorCode;
    private final String viewerCode;

    // Thread-safe list of clients in this session only
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    // Every edit operation ever sent in this session, in order
    // When a new user joins, replay all of these so they get the full document
    private final List<String> operationHistory = new CopyOnWriteArrayList<>();

    public CollaborationSession(String documentId, String editorCode, String viewerCode) {
        this.documentId = documentId;
        this.editorCode = editorCode;
        this.viewerCode = viewerCode;
        System.out.println("[Session] Created: " + documentId);
        System.out.println("  Editor code: " + editorCode);
        System.out.println("  Viewer code: " + viewerCode);
    }

    // ========== Getters ==========

    public String getDocumentId() {
        return documentId;
    }

    public String getEditorCode() {
        return editorCode;
    }

    public String getViewerCode() {
        return viewerCode;
    }

    public List<String> getOperationHistory() {
        return operationHistory;
    }

    // ========== Client Management ==========

    public void addClient(ClientHandler client) {
        clients.add(client);
        System.out.println("[Session " + documentId + "] Client joined. Total: " + clients.size());
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("[Session " + documentId + "] Client left. Total: " + clients.size());
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public boolean isEmpty() {
        return clients.isEmpty();
    }

    // ========== Operation History ==========

    /**
     * Save a message into the history.
     * Only save real edit operations (INSERT_CHAR, DELETE_CHAR, etc.)
     * Don't save CURSOR / JOIN / LEAVE - those are just presence events
     */
    public void saveOperation(String message) {
        operationHistory.add(message);
        System.out.println("[Session " + documentId + "] Saved operation. Total history: " + operationHistory.size());
    }

    /**
     * Send the full history to a newly connected client
     * This way they start with the complete document, not empty
     */
    public void replayHistoryTo(ClientHandler client) {
        System.out.println("[Session " + documentId + "] Replaying "
                + operationHistory.size() + " operations to new client.");
        for (String op : operationHistory) {
            client.sendMessage(op);
        }
    }

    // ========== Broadcast ==========

    /**
     * Send a message to every client in THIS session except the sender
     */
    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
}