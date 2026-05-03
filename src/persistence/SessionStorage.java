package persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import server.CollaborationSession;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * SessionStorage - Handles file persistence for collaboration sessions (TASK 4)
 */
public class SessionStorage {
    private static final String DATA_DIR = "data/sessions/";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SessionStorage() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            System.out.println("[Storage] Created data directory: " + DATA_DIR);
        } catch (IOException e) {
            System.err.println("[Storage] Failed to create data directory: " + e.getMessage());
        }
    }

    /**
     * Save a session to disk
     */
    public void saveSession(CollaborationSession session) {
        String filename = DATA_DIR + session.getDocumentId() + ".json";
        SessionData data = new SessionData(
                session.getDocumentId(),
                session.getEditorCode(),
                session.getViewerCode(),
                new ArrayList<>(session.getOperationHistory()) // Copy the list
        );

        try (Writer writer = new FileWriter(filename)) {
            gson.toJson(data, writer);
            System.out.println("[Storage] Saved session: " + session.getDocumentId() +
                    " with " + data.getOperationHistory().size() + " operations");
        } catch (IOException e) {
            System.err.println("[Storage] Failed to save session " + session.getDocumentId() +
                    ": " + e.getMessage());
        }
    }

    /**
     * Load all saved sessions from disk
     */
    public List<SessionData> loadAllSessions() {
        List<SessionData> sessions = new ArrayList<>();
        File dir = new File(DATA_DIR);

        if (!dir.exists()) {
            System.out.println("[Storage] No data directory found, starting fresh");
            return sessions;
        }

        File[] sessionFiles = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (sessionFiles == null) {
            return sessions;
        }

        for (File file : sessionFiles) {
            try (Reader reader = new FileReader(file)) {
                SessionData data = gson.fromJson(reader, SessionData.class);
                sessions.add(data);
                System.out.println("[Storage] Loaded session from " + file.getName() +
                        " with " + data.getOperationHistory().size() + " operations");
            } catch (IOException e) {
                System.err.println("[Storage] Failed to load session from " + file.getName() +
                        ": " + e.getMessage());
            }
        }

        System.out.println("[Storage] Loaded " + sessions.size() + " sessions total");
        return sessions;
    }

    /**
     * Delete a session from disk
     */
    public boolean deleteSession(String documentId) {
        String filename = DATA_DIR + documentId + ".json";
        File file = new File(filename);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("[Storage] Deleted session: " + documentId);
            } else {
                System.err.println("[Storage] Failed to delete session: " + documentId);
            }
            return deleted;
        }

        System.out.println("[Storage] Session not found for deletion: " + documentId);
        return false;
    }

    /**
     * SessionData - Serializable representation of a session
     */
    public static class SessionData {
        private String documentId;
        private String editorCode;
        private String viewerCode;
        private List<String> operationHistory;

        public SessionData(String documentId, String editorCode,
                String viewerCode, List<String> operationHistory) {
            this.documentId = documentId;
            this.editorCode = editorCode;
            this.viewerCode = viewerCode;
            this.operationHistory = operationHistory;
        }

        // Getters
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

        // Setters (for deserialization)
        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public void setEditorCode(String editorCode) {
            this.editorCode = editorCode;
        }

        public void setViewerCode(String viewerCode) {
            this.viewerCode = viewerCode;
        }

        public void setOperationHistory(List<String> operationHistory) {
            this.operationHistory = operationHistory;
        }
    }
}