package crdt.character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterCRDT {

    Map<String, CRDTNode> nodeMap;
    List<CRDTNode> roots; //nodes with parent = null

    public CharacterCRDT() {
        nodeMap = new HashMap<>();
        roots = new ArrayList<>();
    }

    public void insert(String id, char value, String parentId) {
        CRDTNode parent = null;
        if (parentId != null) {
            parent = nodeMap.get(parentId);
        }
        CRDTNode newNode = new CRDTNode(id, value, parent);

        if (parent == null) {
            roots.add(newNode);
        } else {
            parent.children.add(newNode);
        }

        nodeMap.put(id, newNode);
    }

    public void delete(String id) {

        CRDTNode node = nodeMap.get(id);

        if (node != null) {
            node.del = true;
        }
    }

}