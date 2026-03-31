package crdt.character;

import java.util.ArrayList;
import java.util.List;

public class CRDTNode {
    String id;
    char value;
    boolean del;
    CRDTNode parent;
    List<CRDTNode> children;

    public CRDTNode(String id, char value, CRDTNode parent) {
        this.id = id;
        this.value = value;
        this.parent = parent;
        this.del = false; //default
        this.children = new ArrayList<>();
    }


}
