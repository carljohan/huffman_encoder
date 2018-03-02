package alda.huffman;

import java.util.Comparator;


public class HNode {
    private int value;
    private char character;
    private HNode left;
    private HNode right;


    public HNode(int value, char character) {
        this.value = value;
        this.character = character;
    }

    public HNode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public char getCharacter() {
        return character;
    }


    public HNode getLeft() {
        return left;
    }

    public void setLeft(HNode left) {
        this.left = left;
    }

    public HNode getRight() {
        return right;
    }

    public void setRight(HNode right) {
        this.right = right;
    }


    public String toString() {
        return value + " " + character;
    }
}

 class NodeComparator implements Comparator<HNode> {
    public int compare(HNode q, HNode w) {
        return q.getValue() - w.getValue();
    }
}
