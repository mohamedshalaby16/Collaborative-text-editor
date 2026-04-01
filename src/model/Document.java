package model;

import crdt.block.Block;
import crdt.block.BlockCRDT;
import operations.DeleteBlockOperation;
import operations.DeleteCharacterOperation;
import operations.InsertBlockOperation;
import operations.InsertCharacterOperation;

import java.util.HashMap;
import java.util.Map;

public class Document {

    private BlockCRDT blockCRDT; // manages the ordering of blocks
    private Map<String, Block> blockMap; // quick lookup: blockId -> Block

    public Document() {
        blockCRDT = new BlockCRDT();
        blockMap = new HashMap<>();
    }

    // Creates a new block and inserts it into the document
    public void apply(InsertBlockOperation myOperation) {
        Block newBlock = new Block(myOperation.getBlockId());
        blockMap.put(myOperation.getBlockId(), newBlock);
        // and add block to block Crdt of the document
    }

    // Marks a block as deleted (tombstone)
    public void apply(DeleteBlockOperation myOperation) {
        Block currBlock = blockMap.get(myOperation.getBlockId());

        if (currBlock != null) {
            currBlock.setDeleted(true);
        }
    }

    // Gets the correct blcok and calls the isnert crdt function
    public void apply(InsertCharacterOperation myOperation) {
        Block currBlock = blockMap.get(myOperation.getBlockId());

        if (currBlock != null) {
            currBlock.getCharCRDT().insert(
                    myOperation.getUserId(),
                    myOperation.getClock(),
                    myOperation.getValue(),
                    myOperation.getParentCharId()
            );
        }
    }

    // Gets block then make the character deleted
    public void apply(DeleteCharacterOperation myOperation) {
        Block currBlock = blockMap.get(myOperation.getBlockId());

        if (currBlock != null) {
            currBlock.getCharCRDT().delete(myOperation.getCharId());
        }
    }

    // Gets the whole text
    public String getText() {
        return "PLACE HOLDER";
    }

}