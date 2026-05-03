package persistence;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import server.CollaborationSession;
import java.util.ArrayList;
import java.util.List;

public class MongoSessionStorage {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> sessionsCollection;

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "collab_editor";
    private static final String COLLECTION_NAME = "sessions";

    public MongoSessionStorage() {
        this.mongoClient = MongoClients.create(CONNECTION_STRING);
        this.database = mongoClient.getDatabase(DATABASE_NAME);
        this.sessionsCollection = database.getCollection(COLLECTION_NAME);

        // Create indexes for faster lookups
        sessionsCollection.createIndex(new Document("documentId", 1));
        sessionsCollection.createIndex(new Document("editorCode", 1));
        sessionsCollection.createIndex(new Document("viewerCode", 1));

        System.out.println("[MongoDB] Connected to " + DATABASE_NAME);
    }

    public void saveSession(CollaborationSession session) {
        Document doc = new Document()
                .append("documentId", session.getDocumentId())
                .append("editorCode", session.getEditorCode())
                .append("viewerCode", session.getViewerCode())
                .append("operationHistory", new ArrayList<>(session.getOperationHistory()))
                .append("lastSaved", System.currentTimeMillis());

        sessionsCollection.updateOne(
                new Document("documentId", session.getDocumentId()),
                new Document("$set", doc),
                new com.mongodb.client.model.UpdateOptions().upsert(true));

        System.out.println("[MongoDB] Saved: " + session.getDocumentId());
    }

    public List<SessionData> loadAllSessions() {
        List<SessionData> sessions = new ArrayList<>();

        for (Document doc : sessionsCollection.find()) {
            SessionData data = new SessionData(
                    doc.getString("documentId"),
                    doc.getString("editorCode"),
                    doc.getString("viewerCode"),
                    (List<String>) doc.get("operationHistory"));
            sessions.add(data);
        }

        System.out.println("[MongoDB] Loaded " + sessions.size() + " sessions");
        return sessions;
    }

    public boolean deleteSession(String documentId) {
        long deleted = sessionsCollection.deleteOne(new Document("documentId", documentId)).getDeletedCount();
        return deleted > 0;
    }

    public String findDocumentIdByCode(String code) {
        Document query = new Document("$or", List.of(
                new Document("editorCode", code),
                new Document("viewerCode", code)));
        Document result = sessionsCollection.find(query).first();
        return result != null ? result.getString("documentId") : null;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("[MongoDB] Disconnected");
        }
    }

    public static class SessionData {
        private String documentId;
        private String editorCode;
        private String viewerCode;
        private List<String> operationHistory;

        public SessionData(String documentId, String editorCode, String viewerCode, List<String> operationHistory) {
            this.documentId = documentId;
            this.editorCode = editorCode;
            this.viewerCode = viewerCode;
            this.operationHistory = operationHistory != null ? operationHistory : new ArrayList<>();
        }

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
    }
}