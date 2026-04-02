package crdt.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockCRDT {

    public Map<String, Block> nodeMap;
    public  List<Block> roots;  

      public BlockCRDT() {
        nodeMap = new HashMap<>();
        roots = new ArrayList<>();
    }

        public void insert(int userId, int clock, String blockId, String parentBlockId) {
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
        blocks.sort((a, b) -> {
            if (a.clock != b.clock) return Integer.compare(b.clock, a.clock);
            return Integer.compare(a.userId, b.userId);
        });
    }

}
