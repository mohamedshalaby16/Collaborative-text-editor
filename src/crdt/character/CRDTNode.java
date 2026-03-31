package crdt.character;

import java.util.ArrayList;
import java.util.List;

public class CRDTNode {
    public String id;
    public char value;
    public boolean del;
    public CRDTNode parent;
    public List<CRDTNode> children;

    public CRDTNode(String id, char value, CRDTNode parent) {
        this.id = id;
        this.value = value;
        this.parent = parent;
        this.del = false; //default
        this.children = new ArrayList<>();
    }


}
