package com.shyamnatesan.page;

import com.shyamnatesan.btree.Btree;

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

    int[] getChildren();

    String getSlots();

    String getData();

    void setParentPageId(int parentPageId);

    void generatePageHeader();

    String insert(int key, String value, Btree tree);


}
