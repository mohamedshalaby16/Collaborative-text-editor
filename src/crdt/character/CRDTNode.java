package crdt.character;

import java.util.ArrayList;
import java.util.List;

public class CRDTNode {
    public int userId;
    public int clock;
    public char value;
    public boolean del;
    public CRDTNode parent;
    public List<CRDTNode> children;

    public CRDTNode(int userId, int clock, char value, CRDTNode parent) {
        this.userId = userId;
        this.clock = clock;
        this.value = value;
        this.parent = parent;
        this.del = false;
        this.children = new ArrayList<>();
    }

    public String getId() {
        return userId + "-" + clock;
    }
}