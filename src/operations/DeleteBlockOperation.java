package operations;

public class DeleteBlockOperation {
    private String blockId;

    public DeleteBlockOperation(String blockId) {
        this.blockId = blockId;
    }

    public String getBlockId() {
        return blockId;
    }
}