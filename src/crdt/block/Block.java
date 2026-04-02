package crdt.block;

import crdt.character.CharacterCRDT;
import java.util.ArrayList;
import java.util.List;

public class Block {

     
   
    // private boolean deleted;
    
    public int userId;
    private final String id;
    private final CharacterCRDT charCRDT;
    public int clock;
    public boolean deleted;
    public Block parent;
    public List<Block> children;

    public Block(String id, int userId, int clock, Block parent) {
        this.id=id;
        this.userId=userId;
        this.clock=clock;
        this.parent=parent;
        this.children=new ArrayList<>();
        this.charCRDT = new CharacterCRDT();
        this.deleted = false;
    }

    public String  getId() {
        return id;
    }

    public CharacterCRDT getCharCRDT() {
        return charCRDT;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}