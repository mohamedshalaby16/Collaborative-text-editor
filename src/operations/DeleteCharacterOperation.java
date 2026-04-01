package operations;

public class DeleteCharacterOperation {
    private String charId;
    private String blockId;

    public DeleteCharacterOperation(String charId, String blockId) {
        this.charId = charId;
        this.blockId = blockId;
    }

    public String getCharId() {
        return charId;
    }

    public String getBlockId() {
        return blockId;
    }
}