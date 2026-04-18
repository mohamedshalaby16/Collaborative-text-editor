package network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import operations.InsertCharacterOperation;
import operations.DeleteCharacterOperation;
import operations.InsertBlockOperation;
import operations.DeleteBlockOperation;

public class MessageHandler {

    private static final Gson gson = new GsonBuilder().create();

    // ========== CONVERT OPERATION TO JSON STRING ==========
    public static String operationToMessage(Object operation) {
        JsonObject json = new JsonObject();

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

        // ========== CURSOR MESSAGE ==========
    public static String cursorToMessage(int userId, String username, int position) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "CURSOR");
        json.addProperty("userId", userId);
        json.addProperty("username", username);
        json.addProperty("position", position);
        return gson.toJson(json);
    }

    
    // ========== CONVERT JSON STRING TO OPERATION ==========
    public static Object messageToOperation(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();

            switch (type) {
                case "INSERT_CHAR":
                    int userId = json.get("userId").getAsInt();
                    int clock = json.get("clock").getAsInt();
                    char value = json.get("value").getAsString().charAt(0);
                    String parentId = json.has("parentId") && !json.get("parentId").isJsonNull()
                            ? json.get("parentId").getAsString()
                            : null;
                    String blockId = json.get("blockId").getAsString();
                    return new InsertCharacterOperation(userId, clock, value, parentId, blockId);

                case "DELETE_CHAR":
                    int userIdDel = json.get("userId").getAsInt();
                    int clockDel = json.get("clock").getAsInt();
                    String blockIdDel = json.get("blockId").getAsString();
                    return new DeleteCharacterOperation(userIdDel, clockDel, blockIdDel);

                case "INSERT_BLOCK":
                    int userIdBlock = json.get("userId").getAsInt();
                    int clockBlock = json.get("clock").getAsInt();
                    String blockIdNew = json.get("blockId").getAsString();
                    String parentBlockId = json.has("parentBlockId") && !json.get("parentBlockId").isJsonNull()
                            ? json.get("parentBlockId").getAsString()
                            : null;
                    return new InsertBlockOperation(blockIdNew, parentBlockId, userIdBlock, clockBlock);

                case "DELETE_BLOCK":
                    String blockIdDelBlock = json.get("blockId").getAsString();
                    return new DeleteBlockOperation(blockIdDelBlock);

                default:
                    return null;
            }
        } catch (Exception e) {
            System.out.println("Failed to parse JSON message: " + e.getMessage());
            return null;
        }
    }
     // ========== CHECK IF MESSAGE IS CURSOR ==========
    public static boolean isCursorMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            return "CURSOR".equals(json.get("type").getAsString());
        } catch (Exception e) {
            return false;
        }
    }
 
    public static int getCursorUserId(String message) {
        return JsonParser.parseString(message).getAsJsonObject().get("userId").getAsInt();
    }
 
    public static String getCursorUsername(String message) {
        return JsonParser.parseString(message).getAsJsonObject().get("username").getAsString();
    }
 
    public static int getCursorPosition(String message) {
        return JsonParser.parseString(message).getAsJsonObject().get("position").getAsInt();
    }
}
