package crdt.character;

public class CRDTCharacter {

    public String id;      // unique identifier
    public char value;     // the actual character
    public boolean visible; // for deletion (tombstone)

    public CRDTCharacter(String id, char value) {
        this.id = id;
        this.value = value;
        this.visible = true;
    }

    @Override
    public String toString() {
        if (visible) {
            return Character.toString(value);
        } else {
            return ""; // hidden if deleted
        }
    }
}