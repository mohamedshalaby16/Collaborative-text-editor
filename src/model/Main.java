package model;

import crdt.character.CharacterCRDT;
import operations.InsertCharacterOperation;

public class Main {
    public static void main(String[] args) {

        CharacterCRDT crdt = new CharacterCRDT();

        crdt.insert("1", 'A', null);
        crdt.insert("2", 'B', "1");
        crdt.insert("3", 'C', "2");
        crdt.insert("4", 'D', "1");

        crdt.delete("2");

        // System.out.println("Final visible text: " + crdt.getText());
        // InsertCharacterOperation myop = new InsertCharacterOperation('a', "asd",
        // null, null);
        // Document myDoc = new Document();
        // myDoc.apply(myop);

    }
}