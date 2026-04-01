package model;

import crdt.character.CharacterCRDT;

public class Main {
    public static void main(String[] args) {

        CharacterCRDT crdt = new CharacterCRDT();

        // Insert first character A => id = 1-0
        crdt.insert(1, 0, 'A', null);

        // Insert B after A => id = 1-1
        crdt.insert(1, 1, 'B', "1-0");

        // Insert C after A => id = 2-0
        crdt.insert(2, 0, 'C', "1-0");

        System.out.println("After inserts: " + crdt.getText());

        // Delete B
        crdt.delete("1-1");

        System.out.println("After deleting B: " + crdt.getText());
    }
}