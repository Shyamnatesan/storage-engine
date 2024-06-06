package com.shyamnatesan;


import com.shyamnatesan.buffer.BufferManager;
import com.shyamnatesan.page.Page;
import com.shyamnatesan.storageManager.FileManager;
import com.shyamnatesan.storageManager.StorageManager;

import java.util.Arrays;


public class Main {
    public static void main(String[] args) {
        StorageManager storageManager = new StorageManager();
        FileManager fileManager = storageManager.createFile("firstfile");


        for (int i = 1; i <= 200; ) {
            fileManager.Insert(i, "value" + i);
            i += 1;
        }

        for (int i = 400; i >= 201; i--) {
            fileManager.Insert(i, "value" + i);
        }
// page 7 at 32th time

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("running search");
        for (int i = 1; i <= 400; i++) {
            System.out.println(fileManager.Search(i));
        }

//        System.out.println(fileManager.getBtree().root);
//
//        /* to print data pages */
//        Btree btree = fileManager.getBtree();
//        printDataPages(btree.root);
//
//        /* to print all pages in buffer */
//        Page[] pages = BufferManager.getPages();
//        for (Page page : pages) {
//            System.out.println(page);
//        }
    }


    public static void printDataPages(Page root) {
        if (root != null && root.isLeaf()) {
            Page current = root;
            while (current != null) {
                System.out.println("{");
                System.out.println("    page id is " + current.getPageId());
                System.out.println("    number of keys in page is " + current.getNumKeys());
                System.out.println("    keys in page are, " + current.getKeys());
                System.out.println("    children of this page are, " + Arrays.toString(current.getChildren()));
                System.out.println("    rightpage of current is " + current.getRightPage());
                System.out.println("    is leaf page ? " + current.isLeaf());
                System.out.println("    is dirty page ? " + current.isDirty());
                System.out.println("    freeSpaceOffsetStart of page is " + current.getFreeSpaceOffsetStart());
                System.out.println("    freeSpaceOffsetEnd of page is " + current.getFreeSpaceOffsetEnd());
                System.out.println("    parent page id is " + current.getParentPageId());
                System.out.println("    slots of this page is " + current.getSlots());
                System.out.println("}");
                if (current.getRightPage() == current.getPageId()) {
                    current = null;
                } else {
                    current = BufferManager.getPage(current.getRightPage(), current.getFile());
                }
            }
            return;
        }
        assert root != null;
        int[] rootsChildren = root.getChildren();
        Page childPage = BufferManager.getPage(rootsChildren[0], root.getFile());
        printDataPages(childPage);
    }
}