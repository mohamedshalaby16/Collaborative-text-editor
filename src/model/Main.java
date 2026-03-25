package model;

import crdt.character.CRDTCharacter;

public class Main {
    public static void main(String[] args) {

        CRDTCharacter c1 = new CRDTCharacter("1", 'A');
        CRDTCharacter c2 = new CRDTCharacter("2", 'B');

        System.out.println(c1); // A
        System.out.println(c2); // B

        c1.visible = false;

        System.out.println(c1); // (should print nothing)
    }
}