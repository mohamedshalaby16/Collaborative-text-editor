package crdt.block;

import crdt.character.CharacterCRDT;

public class Block {

    private String id;
    private CharacterCRDT charCRDT;
    private boolean deleted;

    public Block(String id) {
        this.id = id;
        this.charCRDT = new CharacterCRDT();
        this.deleted = false;
    }

    public String getId() {
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