package model;

import crdt.block.Block;
import crdt.block.BlockCRDT;
import java.util.Collections;
import java.util.List;
import operations.DeleteBlockOperation;
import operations.DeleteCharacterOperation;
import operations.InsertBlockOperation;
import operations.InsertCharacterOperation;

public class Document {

    private BlockCRDT blockCRDT; // manages the ordering of blocks

    public Document() {
        blockCRDT = new BlockCRDT();

        // Create initial block for testing
        Block initialBlock = new Block("block-1", 0, 0, null);
        blockCRDT.putBlock("block-1", initialBlock);
        blockCRDT.addRoot(initialBlock);
    }

    // Apply an operation that came from another user (remote)
    public void applyRemote(Object operation) {
        if (operation instanceof InsertCharacterOperation) {
            apply((InsertCharacterOperation) operation);
        } else if (operation instanceof DeleteCharacterOperation) {
            apply((DeleteCharacterOperation) operation);
        } else if (operation instanceof InsertBlockOperation) {
            apply((InsertBlockOperation) operation);
        } else if (operation instanceof DeleteBlockOperation) {
            apply((DeleteBlockOperation) operation);
        }
    }

    // Creates a new block and inserts it into the document
    public void apply(InsertBlockOperation myOperation) {
        blockCRDT.insert(myOperation.getUserId(), myOperation.getClock(), myOperation.getBlockId(),
                myOperation.getAfterBlockId());
    }

    // Marks a block as deleted (tombstone)
    public void apply(DeleteBlockOperation myOperation) {
        blockCRDT.delete(myOperation.getBlockId());
    }

    // Gets the correct block and calls the insert crdt function
    public void apply(InsertCharacterOperation myOperation) {
        Block currBlock = blockCRDT.getBlock(myOperation.getBlockId());

        if (currBlock != null) {
            currBlock.getCharCRDT().insert(
                    myOperation.getUserId(),
                    myOperation.getClock(),
                    myOperation.getValue(),
                    myOperation.getParentCharId());
        }
    }

    // Gets block then make the character deleted
    public void apply(DeleteCharacterOperation myOperation) {
        Block currBlock = blockCRDT.getBlock(myOperation.getBlockId());

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
            if (i < blocks.size() - 1)
                sb.append("\n");
        }
        return sb.toString();
    }

    public List<String> getVisibleCharacterIds(String blockId) {
        Block block = blockCRDT.getBlock(blockId);

        if (block == null || block.isDeleted()) {
            return Collections.emptyList();
        }

        return block.getCharCRDT().getVisibleCharacterIds();
    }

    public String getCharIdBeforeOffset(String blockId, int offset) {
        List<String> visibleIds = getVisibleCharacterIds(blockId);

        if (offset <= 0 || visibleIds.isEmpty()) {
            return null;
        }

        int index = Math.min(offset - 1, visibleIds.size() - 1);
        return visibleIds.get(index);
    }

    public String getCharIdAtOffset(String blockId, int offset) {
        List<String> visibleIds = getVisibleCharacterIds(blockId);

        if (offset < 0 || offset >= visibleIds.size()) {
            return null;
        }

        return visibleIds.get(offset);
    }

}
