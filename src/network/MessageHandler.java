package network;

import operations.InsertCharacterOperation;
import operations.DeleteCharacterOperation;
import operations.InsertBlockOperation;
import operations.DeleteBlockOperation;

public class MessageHandler {

    // Message format: TYPE|userId|clock|value|parentId|blockId
    // Types: INSERT_CHAR, DELETE_CHAR, INSERT_BLOCK, DELETE_BLOCK

    public static String operationToMessage(Object operation) {
        if (operation instanceof InsertCharacterOperation) {
            InsertCharacterOperation op = (InsertCharacterOperation) operation;
            return "INSERT_CHAR|" + op.getUserId() + "|" + op.getClock() + "|" +
                    op.getValue() + "|" + nullToEmpty(op.getParentCharId()) + "|" +
                    op.getBlockId();

        } else if (operation instanceof DeleteCharacterOperation) {
            DeleteCharacterOperation op = (DeleteCharacterOperation) operation;
            return "DELETE_CHAR|" + op.getUserId() + "|" + op.getClock() + "|" +
                    "|" + op.getCharId() + "|" + op.getBlockId();

        } else if (operation instanceof InsertBlockOperation) {
            InsertBlockOperation op = (InsertBlockOperation) operation;
            return "INSERT_BLOCK|" + op.getUserId() + "|" + op.getClock() + "|" +
                    op.getBlockId() + "|" + nullToEmpty(op.getAfterBlockId()) + "|";

        } else if (operation instanceof DeleteBlockOperation) {
            DeleteBlockOperation op = (DeleteBlockOperation) operation;
            return "DELETE_BLOCK|0|0|" + op.getBlockId() + "||";
        }

        return null;
    }

    public static Object messageToOperation(String message) {
        String[] parts = message.split("\\|");
        String type = parts[0];

        switch (type) {
            case "INSERT_CHAR":
                int userId = Integer.parseInt(parts[1]);
                int clock = Integer.parseInt(parts[2]);
                char value = parts[3].charAt(0);
                String parentId = emptyToNull(parts[4]);
                String blockId = parts[5];
                return new InsertCharacterOperation(userId, clock, value, parentId, blockId);

            case "DELETE_CHAR":
                int userIdDel = Integer.parseInt(parts[1]);
                int clockDel = Integer.parseInt(parts[2]);
                String blockIdDel = parts[5];
                return new DeleteCharacterOperation(userIdDel, clockDel, blockIdDel);

            case "INSERT_BLOCK":
                int userIdBlock = Integer.parseInt(parts[1]);
                int clockBlock = Integer.parseInt(parts[2]);
                String blockIdNew = parts[3];
                String parentBlockId = emptyToNull(parts[4]);
                return new InsertBlockOperation(blockIdNew, parentBlockId, userIdBlock, clockBlock);

            case "DELETE_BLOCK":
                String blockIdDelBlock = parts[3];
                return new DeleteBlockOperation(blockIdDelBlock);
        }

        return null;
    }

    private static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    private static String emptyToNull(String str) {
        return (str == null || str.isEmpty()) ? null : str;
    }
}