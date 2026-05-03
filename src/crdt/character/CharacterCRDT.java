package crdt.character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.IdGenerator;
import util.OrderingUtil;

public class CharacterCRDT {

    private final Map<String, CRDTNode> nodeMap;
    private final List<CRDTNode> roots; // nodes with parent = null

    public CharacterCRDT() {
        nodeMap = new HashMap<>();
        roots = new ArrayList<>();
    }

    public void insert(int userId, int clock, char value, String parentId) {
        String newId = IdGenerator.generate(userId, clock);

        // Ignore duplicate insert
        if (nodeMap.containsKey(newId)) {
            return;
        }

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

    public boolean containsNode(String id) {
        return nodeMap.containsKey(id);
    }

    public String getText() {
        StringBuilder result = new StringBuilder();

        for (CRDTNode node : roots) {
            buildText(node, result);
        }

        return result.toString();
    }

    public List<String> getVisibleCharacterIds() {
        List<String> result = new ArrayList<>();

        for (CRDTNode node : roots) {
            buildVisibleIds(node, result);
        }

        return result;
    }

    private void buildText(CRDTNode node, StringBuilder result) {
        if (!node.del) {
            result.append(node.value);
        }

        for (CRDTNode child : node.children) {
            buildText(child, result);
        }
    }

    private void buildVisibleIds(CRDTNode node, List<String> result) {
        if (!node.del) {
            result.add(node.getId());
        }

        for (CRDTNode child : node.children) {
            buildVisibleIds(child, result);
        }
    }

    private void sortNodes(List<CRDTNode> nodes) {
        nodes.sort((a, b) -> OrderingUtil.compare(a.clock, a.userId, b.clock, b.userId));
    }
    public void undelete(String id) {
    CRDTNode node = nodeMap.get(id);
    if (node != null) {
        node.del = false;
    }
    }
    public void setFormat(String id, boolean bold, boolean italic) {
    CRDTNode node = nodeMap.get(id);
    if (node != null) {
        node.bold = bold;
        node.italic = italic;
    }
}

public CRDTNode getNode(String id) {
    return nodeMap.get(id);
}
}

