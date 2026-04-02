package operations;

public class InsertBlockOperation {
    private String blockId;
    private String parentBlockId;
    private int userId;
    private int clock;

    public InsertBlockOperation(String blockId, String parentBlockId, int userId, int clock) {
        this.blockId = blockId;
        this.parentBlockId = parentBlockId;
        this.userId=userId;
        this.clock=clock;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getAfterBlockId() {
        return parentBlockId;
    }
     public int getUserId() {
         return userId;
    }
    public int getClock() { 
        return clock;
    }
}