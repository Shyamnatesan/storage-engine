package com.shyamnatesan.page;

import com.shyamnatesan.Constants;
import com.shyamnatesan.btree.Btree;
import com.shyamnatesan.buffer.BufferManager;

import java.io.*;

import java.util.Arrays;

public class LeafPage implements Page {
    private final boolean isLeaf;
    private boolean isDirty;
    private final int pageId;
    private int numOfKeys;
    private int parentPageId;
    private int freeSpaceOffsetStart;
    private int freeSpaceOffsetEnd;
    private final int[] keys;
    private final String[] values;
    private final Slot[] slots;
    private int rightSibling;
    private final RandomAccessFile file;


    public LeafPage(RandomAccessFile file) {
        this.isLeaf = true;
        this.isDirty = false;
        this.numOfKeys = 0;
        this.pageId = PageIdentifier.pageId;
        PageIdentifier.increment();
        this.parentPageId = 0;
        this.freeSpaceOffsetStart = Constants.PageHeaderSize;
        this.freeSpaceOffsetEnd = Constants.PageSize;
        this.keys = new int[Constants.M];
        this.values = new String[Constants.M];
        this.slots = new Slot[Constants.M];
        this.rightSibling = this.pageId;
        this.file = file;
        BufferManager.addPageToBuffer(this);
    }


    @Override
    public int getPageId() {
        return this.pageId;
    }

    @Override
    public boolean isLeaf() {
        return this.isLeaf;
    }

    @Override
    public boolean isDirty() {
        return this.isDirty;
    }

    @Override
    public int getNumKeys() {
        return this.numOfKeys;
    }

    @Override
    public int getParentPageId() {
        return this.parentPageId;
    }

    @Override
    public int getFreeSpaceOffsetStart() {
        return this.freeSpaceOffsetStart;
    }

    @Override
    public int getFreeSpaceOffsetEnd() {
        return this.freeSpaceOffsetEnd;
    }

    @Override
    public int getRightPage() {
        return this.rightSibling;
    }

    @Override
    public String getKeys() {
        return Arrays.toString(this.keys);
    }

    @Override
    public String[] getValues() {
        return this.values;
    }

    @Override
    public int[] getChildren() {
        return new int[]{};
    }

    @Override
    public Slot[] getSlots() {
        return this.slots;
    }

    @Override
    public String insert(int key, String value, Btree tree) {
        System.out.println("inserting key " + key + " in page " + Arrays.toString(this.keys));
        System.out.println("the current page has " + this.numOfKeys + " keys");
        int lengthOfDataRecord = value.length();
        int recordOffset = this.freeSpaceOffsetEnd - lengthOfDataRecord;
        LeafSlot slot = new LeafSlot(lengthOfDataRecord, recordOffset);
        this.freeSpaceOffsetEnd = slot.recordOffset;

        int pos = 0;
        while (pos < this.numOfKeys && this.keys[pos] < key) {
            pos++;
        }
        for (int i = this.numOfKeys; i > pos; i--) {
            this.keys[i] = this.keys[i - 1];
            this.values[i] = this.values[i - 1];
            this.slots[i] = this.slots[i - 1];
        }
        this.keys[pos] = key;
        this.values[pos] = value;
        this.slots[pos] = slot;
        this.freeSpaceOffsetStart += Constants.LeafSlotsize;
        this.numOfKeys++;
        this.isDirty = true;

        if (this.numOfKeys > Constants.MAX_NUM_OF_KEYS) {
            this.maxKeyThresholdReached(tree);
        }


        return this.toString();
    }

    @Override
    public RandomAccessFile getFile() {
        return this.file;
    }


    @Override
    public void setParentPageId(int parentPageId) {
        this.parentPageId = parentPageId;
    }

    @Override
    public void setIsDirty(boolean dirty) {
        this.isDirty = dirty;
    }


    public void maxKeyThresholdReached(Btree tree) {
        System.out.println("Max Key threshold has reached. So we split");
        /*  Splitting page  */
        int medianIndex = this.numOfKeys / 2;
        int medianKey = this.keys[medianIndex];

        LeafPage rightPage = new LeafPage(this.file);
        System.out.println("page id of rightpage is " + rightPage.pageId);

        if (this.rightSibling != this.pageId) {
            rightPage.rightSibling = this.rightSibling;
        }
        this.rightSibling = rightPage.pageId;


        int j = 0;
        for (int i = medianIndex; i < this.numOfKeys; i++) {
            rightPage.keys[j] = this.keys[i];
            rightPage.values[j] = this.values[i];
            int lengthOfDataRecord = rightPage.values[j].length();
            LeafSlot newSlot = new LeafSlot(lengthOfDataRecord, rightPage.freeSpaceOffsetEnd - lengthOfDataRecord);
            rightPage.slots[j] = newSlot;
            rightPage.freeSpaceOffsetEnd = newSlot.recordOffset;
            rightPage.freeSpaceOffsetStart += Constants.LeafSlotsize;
            rightPage.numOfKeys++;
            this.keys[i] = 0;
            this.values[i] = "";
            this.slots[i].setIsDeleted(true);
            j++;
        }
        this.numOfKeys = medianIndex;
        rightPage.isDirty = true;

        System.out.println("current page is " + this);
        System.out.println("right page is " + rightPage);

        if (this.parentPageId == 0) {
            System.out.println("creating a new parent page");
            InternalPage parentPage = new InternalPage(this.file);
            parentPage.keys[0] = medianKey;
            parentPage.children[0] = this.pageId;
            parentPage.children[1] = rightPage.pageId;

            InternalSlot slot = new InternalSlot(4);
            slot.setLeftChildPointer(parentPage.freeSpaceOffsetEnd - 4);
            parentPage.freeSpaceOffsetEnd = slot.leftChildPointer;
            slot.setRightChildPointer(parentPage.freeSpaceOffsetEnd - 4);
            parentPage.freeSpaceOffsetEnd = slot.rightChildPointer;
            parentPage.slots[0] = slot;
            parentPage.freeSpaceOffsetStart += Constants.InternalSlotSize;

            this.parentPageId = parentPage.pageId;
            rightPage.parentPageId = parentPage.pageId;
            tree.root = parentPage;
            parentPage.isDirty = true;
            parentPage.numOfKeys++;
            System.out.println("this is the parent page aka new root " + parentPage);
        } else {
            InternalPage parent = (InternalPage) BufferManager.getPage(this.parentPageId);
            assert parent != null;
            parent.maxKeyThresholdReached(medianKey, rightPage, tree);
        }


    }

    @Override
    public String toString() {
        return "Page{" +
                "isLeaf=" + isLeaf +
                ", isDirty=" + isDirty +
                ", pageId=" + pageId +
                ", numOfKeys=" + numOfKeys +
                ", parentPageId=" + parentPageId +
                ", freeSpaceOffsetStart=" + freeSpaceOffsetStart +
                ", freeSpaceOffsetEnd=" + freeSpaceOffsetEnd +
                ", rightpage=" + this.rightSibling +
                ", keys=" + Arrays.toString(keys) +
                ", values=" + Arrays.toString(values) +
                ", slots=" + Arrays.toString(slots) +
                ", rightSibling=" + rightSibling +
                '}';
    }


}
