package crdt.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.OrderingUtil;

public class BlockCRDT {

    private final Map<String, Block> nodeMap;
    private final List<Block> roots;

    public BlockCRDT() {
        nodeMap = new HashMap<>();
        roots = new ArrayList<>();
    }

    public void insert(int userId, int clock, String blockId, String parentBlockId) {
        // Ignore duplicate insert
        if (nodeMap.containsKey(blockId)) {
            return;
        }

        Block parent = null;

        if (parentBlockId != null) {
            parent = nodeMap.get(parentBlockId);

            if (parent == null) {
                System.out.println("Insert failed: parent block " + parentBlockId + " not found.");
                return;
            }
        }

        Block newBlock = new Block(blockId, userId, clock, parent);

        if (parent == null) {
            roots.add(newBlock);
            sortBlocks(roots);
        } else {
            parent.children.add(newBlock);
            sortBlocks(parent.children);
        }

        nodeMap.put(blockId, newBlock);
    }

    public void delete(String blockId) {
        Block block = nodeMap.get(blockId);

        if (block != null) {
            block.setDeleted(true);
        }
    }

    public Block getBlockById(String blockId) {
        return nodeMap.get(blockId);
    }

    public boolean containsBlock(String blockId) {
        return nodeMap.containsKey(blockId);
    }

    public List<Block> getBlocks() {
        List<Block> result = new ArrayList<>();

        for (Block root : roots) {
            buildList(root, result);
        }

        return result;
    }

    private void buildList(Block block, List<Block> result) {
        if (!block.deleted) {
            result.add(block);
        }

        for (Block child : block.children) {
            buildList(child, result);
        }
    }

    private void sortBlocks(List<Block> blocks) {
        blocks.sort((a, b) -> OrderingUtil.compare(a.clock, a.userId, b.clock, b.userId));
    }

    public Block getBlock(String blockId) {
        return nodeMap.get(blockId);
    }

    public void putBlock(String blockId, Block block) {
        nodeMap.put(blockId, block);
    }

    public void addRoot(Block block) {
        roots.add(block);
    }
}