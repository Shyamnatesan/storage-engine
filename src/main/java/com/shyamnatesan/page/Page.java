package com.shyamnatesan.page;

import com.shyamnatesan.btree.Btree;

import java.io.*;

public interface Page {
    int getPageId();

    void setPageId(int pageId);

    boolean isLeaf();

    void setIsLeaf(boolean flag);

    boolean isDirty();

    void setIsDirty(boolean flag);

    int getNumKeys();

    void setNumKeys(int numKeys);

    int getParentPageId();

    void setParentPageId(int parentPageId);

    int getFreeSpaceOffsetStart();

    void setFreeSpaceOffsetStart(int freeSpaceOffsetStart);

    int getFreeSpaceOffsetEnd();

    void setFreeSpaceOffsetEnd(int freeSpaceOffsetEnd);

    int getRightPage();

    void setRightPage(int rightPage);

    String getKeys();

    void setKeys(int[] keys);

    String[] getValues();

    void setValues(String[] values);

    int[] getChildren();

    void setChildren(int[] children);

    Slot[] getSlots();

    void setSlots(Slot[] slots);

    String insert(int key, String value, Btree tree);

    RandomAccessFile getFile();

    String search(int key);
}
