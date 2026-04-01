package crdt.character;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterCRDT {

    public Map<String, CRDTNode> nodeMap;
    public List<CRDTNode> roots; // nodes with parent = null

    public CharacterCRDT() {
        nodeMap = new HashMap<>();
        roots = new ArrayList<>();
    }

    public void insert(int userId, int clock, char value, String parentId) {
        CRDTNode parent = null;

        if (parentId != null) {
            parent = nodeMap.get(parentId);

            if (parent == null) {
                System.out.println("Insert failed: parent with id " + parentId + " not found.");
                return;
            }
        }

        CRDTNode newNode = new CRDTNode(userId, clock, value, parent);

        if (parent == null) {
            roots.add(newNode);
            sortNodes(roots);
        } else {
            parent.children.add(newNode);
            sortNodes(parent.children);
        }

        nodeMap.put(newNode.getId(), newNode);
    }

    public void delete(String id) {
        CRDTNode node = nodeMap.get(id);

        if (node != null) {
            node.del = true;
        }
    }

    public String getText() {
        StringBuilder result = new StringBuilder();

        for (CRDTNode node : roots) {
            buildText(node, result);
        }

        return result.toString();
    }

    private void buildText(CRDTNode node, StringBuilder result) {
        if (!node.del) {
            result.append(node.value);
        }

        for (CRDTNode child : node.children) {
            buildText(child, result);
        }
    }

    private void sortNodes(List<CRDTNode> nodes) {
        nodes.sort(new Comparator<CRDTNode>() {
            @Override
            public int compare(CRDTNode a, CRDTNode b) {

                if (a.clock != b.clock) {
                    return Integer.compare(b.clock, a.clock);
                }
                return Integer.compare(a.userId, b.userId);
            }
        });
    }
}