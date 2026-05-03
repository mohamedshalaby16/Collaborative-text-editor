package operations;

import util.IdGenerator;

public class DeleteCharacterOperation {

    private int userId;
    private int clock;
    private String charId; // Added: explicit charId
    private String blockId;

    // New constructor with charId
    public DeleteCharacterOperation(int userId, int clock, String charId, String blockId) {
        this.userId = userId;
        this.clock = clock;
        this.charId = charId;
        this.blockId = blockId;
    }

    // Legacy constructor (maintains compatibility)
    public DeleteCharacterOperation(int userId, int clock, String blockId) {
        this.userId = userId;
        this.clock = clock;
        this.charId = IdGenerator.generate(userId, clock);
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
        return charId;
    }
}