package operations;

public class InsertCharacterOperation {

    private char value;
    private String charId;
    private String parentId;
    private String blockId;

    public InsertCharacterOperation(char value, String charId, String parentId, String blockId) {
        this.value = value;
        this.charId = charId;
        this.parentId = parentId;
        this.blockId = blockId;
    }

    public String getCharId() {
        return charId;
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

}
