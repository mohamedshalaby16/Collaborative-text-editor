package model;

import operations.*;


public class Main {
    public static void main(String[] args) {

        Document doc = new Document();

        // Insert two blocks
        doc.apply(new InsertBlockOperation("block-1", null, 1, 0));
        doc.apply(new InsertBlockOperation("block-2", "block-1", 1, 1));

        // Add text to block-1
        doc.apply(new InsertCharacterOperation(1, 0, 'H', null, "block-1"));
        doc.apply(new InsertCharacterOperation(1, 1, 'i', "1-0", "block-1"));

        // Add text to block-2
        doc.apply(new InsertCharacterOperation(1, 2, 'H', null, "block-2"));
        doc.apply(new InsertCharacterOperation(1, 3, 'e', "1-2", "block-2"));
        doc.apply(new InsertCharacterOperation(1, 4, 'y', "1-3", "block-2"));

       System.out.println("After inserts:");
        System.out.println(doc.getText());

         doc.apply(new DeleteBlockOperation("block-1"));
         System.out.println("\nAfter deleting block-1:");
        System.out.println(doc.getText());

        // CharacterCRDT crdt = new CharacterCRDT();

        // // Insert first character A => id = 1-0
        // crdt.insert(1, 0, 'A', null);

        // // Insert B after A => id = 1-1
        // crdt.insert(1, 1, 'B', "1-0");

        // // Insert C after A => id = 2-0
        // crdt.insert(2, 0, 'C', "1-0");

        // System.out.println("After inserts: " + crdt.getText());

        // // Delete B
        // crdt.delete("1-1");

        // System.out.println("After deleting B: " + crdt.getText());
    }
}