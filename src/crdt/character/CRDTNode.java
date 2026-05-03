package crdt.character;

import java.util.ArrayList;
import java.util.List;
import util.IdGenerator;

public class CRDTNode {
    public final int userId;
    public final int clock;
    public final char value;
    public boolean del;
    public boolean bold;
    public boolean italic;
    public final CRDTNode parent;
    public final List<CRDTNode> children;

    public CRDTNode(int userId, int clock, char value, CRDTNode parent) {
        this.userId = userId;
        this.clock = clock;
        this.value = value;
        this.parent = parent;
        this.del = false;
        this.bold = false;
        this.italic = false;
        this.children = new ArrayList<>();
    }

    public String getId() {
        return IdGenerator.generate(userId, clock);
    }
}