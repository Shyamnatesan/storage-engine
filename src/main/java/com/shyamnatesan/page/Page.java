package com.shyamnatesan.page;

import com.shyamnatesan.btree.Btree;

import java.io.*;

public interface Page {
    int getPageId();

    boolean isLeaf();

    boolean isDirty();

    int getNumKeys();

    int getParentPageId();

    int getFreeSpaceOffsetStart();

    int getFreeSpaceOffsetEnd();

    int getRightPage();

    String getKeys();

    String[] getValues();

    int[] getChildren();

    Slot[] getSlots();

    void setParentPageId(int parentPageId);

    void setIsDirty(boolean dirty);

    String insert(int key, String value, Btree tree);

    RandomAccessFile getFile();

}
