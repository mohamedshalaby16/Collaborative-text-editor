package model;

import crdt.block.Block;
import crdt.block.BlockCRDT;
import java.util.List;
import operations.DeleteBlockOperation;
import operations.DeleteCharacterOperation;
import operations.InsertBlockOperation;
import operations.InsertCharacterOperation;

public class Document {

    private BlockCRDT blockCRDT; // manages the ordering of blocks
    

    public Document() {
        blockCRDT = new BlockCRDT();
       
    }

    // Creates a new block and inserts it into the document
    public void apply(InsertBlockOperation myOperation) {
        blockCRDT.insert(myOperation.getUserId(), myOperation.getClock(), myOperation.getBlockId(), myOperation.getAfterBlockId());
    }
        // and add block to block Crdt of the document
    

    // Marks a block as deleted (tombstone)
    public void apply(DeleteBlockOperation myOperation) {
        blockCRDT.delete(myOperation.getBlockId());

    }

    // Gets the correct blcok and calls the isnert crdt function
    public void apply(InsertCharacterOperation myOperation) {
       Block currBlock = blockCRDT.nodeMap.get(myOperation.getBlockId());

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
        Block currBlock = blockCRDT.nodeMap.get(myOperation.getBlockId());

        if (currBlock != null) {
            currBlock.getCharCRDT().delete(myOperation.getCharId());
        }
    }

    // Gets the whole text
    public String getText() {
        List<Block> blocks = blockCRDT.getBlocks();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            sb.append(blocks.get(i).getCharCRDT().getText());
            if (i < blocks.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

}