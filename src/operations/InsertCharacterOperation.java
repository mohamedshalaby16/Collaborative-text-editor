package operations;

public class InsertCharacterOperation {

    private int userId;
    private int clock;
    private char value;
    private String parentId;
    private String blockId;

    public InsertCharacterOperation(int userId, int clock, char value, String parentId, String blockId) {
        this.userId = userId;
        this.clock = clock;
        this.value = value;
        this.parentId = parentId;
        this.blockId = blockId;
    }

    public int getUserId() {
        return userId;
    }

    public int getClock() {
        return clock;
    }

    public char getValue() {
        return value;
    }

    public String getParentCharId() {
        return parentId;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getCharId() {
        return userId + "-" + clock;
    }
}