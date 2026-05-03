package network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import operations.InsertCharacterOperation;
import operations.DeleteCharacterOperation;
import operations.InsertBlockOperation;
import operations.DeleteBlockOperation;
import java.util.List;

public class MessageHandler {

    private static final Gson gson = new GsonBuilder().create();

    // ============================================================
    // Session Management Messages
    // ============================================================

    public static String createSessionMessage(int userId, String username) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "CREATE_SESSION");
        json.addProperty("userId", userId);
        json.addProperty("username", username);
        return gson.toJson(json);
    }

    public static String sessionCreatedMessage(String documentId, String editorCode, String viewerCode) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "SESSION_CREATED");
        json.addProperty("documentId", documentId);
        json.addProperty("editorCode", editorCode);
        json.addProperty("viewerCode", viewerCode);
        json.addProperty("role", "EDITOR");
        return gson.toJson(json);
    }

    public static String joinSessionMessage(int userId, String username, String code) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "JOIN_SESSION");
        json.addProperty("userId", userId);
        json.addProperty("username", username);
        json.addProperty("code", code);
        return gson.toJson(json);
    }

    public static String joinAcceptedMessage(String documentId, String role, List<String> operationHistory) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "JOIN_ACCEPTED");
        json.addProperty("documentId", documentId);
        json.addProperty("role", role);
        JsonArray historyArray = new JsonArray();
        if (operationHistory != null) {
            for (String op : operationHistory) {
                historyArray.add(op);
            }
        }
        json.add("operationHistory", historyArray);
        return gson.toJson(json);
    }

    public static String joinRejectedMessage(String reason) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "JOIN_REJECTED");
        json.addProperty("reason", reason);
        return gson.toJson(json);
    }

    // ============================================================
    // Operation Messages with documentId (FIXED)
    // ============================================================

    public static String operationToMessage(Object operation, String documentId) {
        JsonObject json = new JsonObject();
        json.addProperty("documentId", documentId); // ✅ CRITICAL FIX: Add documentId to all operations

        if (operation instanceof InsertCharacterOperation) {
            InsertCharacterOperation op = (InsertCharacterOperation) operation;
            json.addProperty("type", "INSERT_CHAR");
            json.addProperty("userId", op.getUserId());
            json.addProperty("clock", op.getClock());
            json.addProperty("value", String.valueOf(op.getValue()));
            json.addProperty("parentId", op.getParentCharId());
            json.addProperty("blockId", op.getBlockId());

        } else if (operation instanceof DeleteCharacterOperation) {
            DeleteCharacterOperation op = (DeleteCharacterOperation) operation;
            json.addProperty("type", "DELETE_CHAR");
            json.addProperty("userId", op.getUserId());
            json.addProperty("clock", op.getClock());
            json.addProperty("charId", op.getCharId());
            json.addProperty("blockId", op.getBlockId());

        } else if (operation instanceof InsertBlockOperation) {
            InsertBlockOperation op = (InsertBlockOperation) operation;
            json.addProperty("type", "INSERT_BLOCK");
            json.addProperty("userId", op.getUserId());
            json.addProperty("clock", op.getClock());
            json.addProperty("blockId", op.getBlockId());
            json.addProperty("parentBlockId", op.getAfterBlockId());

        } else if (operation instanceof DeleteBlockOperation) {
            DeleteBlockOperation op = (DeleteBlockOperation) operation;
            json.addProperty("type", "DELETE_BLOCK");
            json.addProperty("blockId", op.getBlockId());

        } else {
            return null;
        }

        return gson.toJson(json);
    }

    // ============================================================
    // Presence Messages with documentId
    // ============================================================

    public static String cursorToMessage(int userId, String username, int position, String anchorCharId,
            String documentId) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "CURSOR");
        json.addProperty("documentId", documentId);
        json.addProperty("userId", userId);
        json.addProperty("username", username);
        json.addProperty("position", position);
        if (anchorCharId != null) {
            json.addProperty("anchorCharId", anchorCharId);
        }
        return gson.toJson(json);
    }

    public static String joinToMessage(int userId, String username, String documentId) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "JOIN");
        json.addProperty("documentId", documentId);
        json.addProperty("userId", userId);
        json.addProperty("username", username);
        return gson.toJson(json);
    }

    public static String leaveToMessage(int userId, String documentId) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "LEAVE");
        json.addProperty("documentId", documentId);
        json.addProperty("userId", userId);
        return gson.toJson(json);
    }

    // ============================================================
    // Helper Methods for Extracting Data
    // ============================================================

    public static String getDocumentId(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has("documentId") && !json.get("documentId").isJsonNull()) {
                return json.get("documentId").getAsString();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String getShareCode(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has("editorCode")) {
                return json.get("editorCode").getAsString();
            }
            if (json.has("viewerCode")) {
                return json.get("viewerCode").getAsString();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String getRole(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has("role")) {
                return json.get("role").getAsString();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String getRejectionReason(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has("reason")) {
                return json.get("reason").getAsString();
            }
        } catch (Exception e) {
        }
        return "Unknown error";
    }

    public static List<String> getOperationHistory(String message) {
        List<String> history = new java.util.ArrayList<>();
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has("operationHistory")) {
                JsonArray arr = json.getAsJsonArray("operationHistory");
                for (int i = 0; i < arr.size(); i++) {
                    history.add(arr.get(i).getAsString());
                }
            }
        } catch (Exception e) {
        }
        return history;
    }

    public static Object messageToOperation(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();

            switch (type) {
                case "INSERT_CHAR": {
                    int userId = json.get("userId").getAsInt();
                    int clock = json.get("clock").getAsInt();
                    char value = json.get("value").getAsString().charAt(0);
                    String parentId = json.has("parentId") && !json.get("parentId").isJsonNull()
                            ? json.get("parentId").getAsString()
                            : null;
                    String blockId = json.get("blockId").getAsString();
                    return new InsertCharacterOperation(userId, clock, value, parentId, blockId);
                }
                case "DELETE_CHAR": {
                    int userId = json.get("userId").getAsInt();
                    int clock = json.get("clock").getAsInt();
                    String charId = json.get("charId").getAsString();
                    String blockId = json.get("blockId").getAsString();
                    return new DeleteCharacterOperation(userId, clock, charId, blockId);
                }
                case "INSERT_BLOCK": {
                    int userId = json.get("userId").getAsInt();
                    int clock = json.get("clock").getAsInt();
                    String blockId = json.get("blockId").getAsString();
                    String parentBlockId = json.has("parentBlockId") && !json.get("parentBlockId").isJsonNull()
                            ? json.get("parentBlockId").getAsString()
                            : null;
                    return new InsertBlockOperation(blockId, parentBlockId, userId, clock);
                }
                case "DELETE_BLOCK": {
                    String blockId = json.get("blockId").getAsString();
                    return new DeleteBlockOperation(blockId);
                }
                default:
                    return null;
            }
        } catch (Exception e) {
            System.out.println("Failed to parse message: " + e.getMessage());
            return null;
        }
    }

    // ============================================================
    // Type-check Helpers
    // ============================================================

    public static boolean isSessionCreatedMessage(String message) {
        return isMessageType(message, "SESSION_CREATED");
    }

    public static boolean isJoinAcceptedMessage(String message) {
        return isMessageType(message, "JOIN_ACCEPTED");
    }

    public static boolean isJoinRejectedMessage(String message) {
        return isMessageType(message, "JOIN_REJECTED");
    }

    public static boolean isCursorMessage(String message) {
        return isMessageType(message, "CURSOR");
    }

    public static boolean isJoinMessage(String message) {
        return isMessageType(message, "JOIN");
    }

    public static boolean isLeaveMessage(String message) {
        return isMessageType(message, "LEAVE");
    }

    private static boolean isMessageType(String message, String expectedType) {
        try {
            return expectedType.equals(
                    JsonParser.parseString(message).getAsJsonObject().get("type").getAsString());
        } catch (Exception e) {
            return false;
        }
    }

    public static int getCursorUserId(String m) {
        return getIntField(m, "userId");
    }

    public static int getPresenceUserId(String m) {
        return getIntField(m, "userId");
    }

    public static String getCursorUsername(String m) {
        return getStringField(m, "username");
    }

    public static String getPresenceUsername(String m) {
        return getStringField(m, "username");
    }

    public static int getCursorPosition(String m) {
        return getIntField(m, "position");
    }

    public static String getCursorAnchorCharId(String m) {
        return getStringField(m, "anchorCharId");
    }

    private static int getIntField(String message, String field) {
        try {
            return JsonParser.parseString(message).getAsJsonObject().get(field).getAsInt();
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getStringField(String message, String field) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has(field) && !json.get(field).isJsonNull()) {
                return json.get(field).getAsString();
            }
        } catch (Exception e) {
        }
        return null;
    }
}