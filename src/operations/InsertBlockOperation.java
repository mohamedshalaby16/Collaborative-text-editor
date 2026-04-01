package operations;

public class InsertBlockOperation {
    private String blockId;
    private String afterBlockId;

    public InsertBlockOperation(String blockId, String afterBlockId) {
        this.blockId = blockId;
        this.afterBlockId = afterBlockId;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getAfterBlockId() {
        return afterBlockId;
    }
}