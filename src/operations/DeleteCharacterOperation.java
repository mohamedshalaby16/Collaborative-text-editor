package operations;

import util.IdGenerator;

public class DeleteCharacterOperation {

    private int userId;
    private int clock;
    private String blockId;

    public DeleteCharacterOperation(int userId, int clock, String blockId) {
        this.userId = userId;
        this.clock = clock;
        this.blockId = blockId;
    }

    public int getUserId() {
        return userId;
    }

    public int getClock() {
        return clock;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getCharId() {
        return IdGenerator.generate(userId, clock);
    }
}