package com.shyamnatesan;

import com.shyamnatesan.btree.Btree;
import com.shyamnatesan.buffer.BufferManager;
import com.shyamnatesan.page.Page;


public class Main {
    public static void main(String[] args) {
        Btree btree = new Btree();
        System.out.println(btree.insert(100, "shyam"));
        System.out.println(btree.insert(200, "monish"));
        System.out.println(btree.insert(300, "navdee"));
        System.out.println(btree.insert(400, "kishan"));
        System.out.println(btree.insert(500, "darsheel"));
        System.out.println(btree.insert(600, "natesan"));
        System.out.println(btree.insert(700, "suganthi"));
        System.out.println(btree.insert(800, "sivakumar"));
        System.out.println(btree.insert(900, "deepa"));
        System.out.println(btree.insert(1000, "mani"));


        System.out.println(btree.insert(2000, "dummy"));
        System.out.println(btree.insert(1500, "dummy"));
        System.out.println(btree.insert(550, "dummy"));
        System.out.println(btree.insert(650, "dummy"));
        System.out.println(btree.insert(250, "dummy"));
        System.out.println(btree.insert(3000, "dummy"));
        System.out.println(btree.insert(4000, "dummy"));

        System.out.println(btree.insert(5000, "dummy"));
        System.out.println(btree.insert(50, "dummy"));
        System.out.println(btree.insert(6000, "dummy"));
        System.out.println(btree.insert(70, "dummy"));

//        System.out.println(btree.insert(6000, "dummy"));
        System.out.println(btree.insert(7000, "dummy"));
        System.out.println(btree.insert(8000, "dummy"));
        System.out.println(btree.insert(9000, "dummy"));

        System.out.println(btree.insert(9100, "dummy"));
        System.out.println(btree.insert(9200, "dummy"));
        System.out.println(btree.insert(9300, "dummy"));
//        System.out.println(btree.insert(7, "dummy"));


        System.out.println("checking the results");

        Page[] pages = BufferManager.getPages();

        for (Page page : pages) {
            System.out.println("{");
            System.out.println("    page id is " + page.getPageId());
            System.out.println("    number of keys in page is " + page.getNumKeys());
            System.out.println("    keys in page are, " + page.getKeys());
            System.out.println("    children of this page are, " + page.getChildren());
            System.out.println("    is leaf page ? " + page.isLeaf());
            System.out.println("    is dirty page ? " + page.isDirty());
            System.out.println("    freeSpaceOffsetStart of page is " + page.getFreeSpaceOffsetStart());
            System.out.println("    freeSpaceOffsetEnd of page is " + page.getFreeSpaceOffsetEnd());
            System.out.println("    parent page id is " + page.getParentPageId());
            System.out.println("    slots of this page is " + page.getSlots());
            System.out.println("    data is " + page.getData());
            System.out.println("}");
            System.out.println();
            System.out.println();
            System.out.println();
        }


    }
}