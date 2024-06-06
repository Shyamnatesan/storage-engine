package com.shyamnatesan.page;

import com.shyamnatesan.Constants;
import com.shyamnatesan.btree.Btree;
import com.shyamnatesan.buffer.BufferManager;

import java.io.*;
import java.util.Arrays;

public class InternalPage implements Page {
    public boolean isLeaf;
    public boolean isDirty;
    public int pageId;
    public int numOfKeys;
    public int parentPageId;
    public int freeSpaceOffsetStart;
    public int freeSpaceOffsetEnd;
    public int[] keys;
    public int[] children;
    public Slot[] slots;
    public RandomAccessFile file;

    public InternalPage(RandomAccessFile file) {
        this.isLeaf = false;
        this.isDirty = false;
        this.pageId = PageIdentifier.pageId;
        PageIdentifier.increment();
        this.numOfKeys = 0;
        this.parentPageId = 0;
        this.freeSpaceOffsetStart = Constants.PageHeaderSize;
        this.freeSpaceOffsetEnd = Constants.PageSize;
        this.keys = new int[Constants.M];
        this.children = new int[Constants.M + 1];
        this.slots = new Slot[Constants.M];
        BufferManager.addPageToBuffer(this);
        this.file = file;
    }

    @Override
    public int getPageId() {
        return this.pageId;
    }

    @Override
    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    @Override
    public boolean isLeaf() {
        return this.isLeaf;
    }

    @Override
    public void setIsLeaf(boolean flag) {
        this.isLeaf = flag;
    }

    @Override
    public boolean isDirty() {
        return this.isDirty;
    }

    @Override
    public void setIsDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    @Override
    public int getNumKeys() {
        return this.numOfKeys;
    }

    @Override
    public void setNumKeys(int numKeys) {
        this.numOfKeys = numKeys;
    }

    @Override
    public int getParentPageId() {
        return this.parentPageId;
    }

    @Override
    public void setParentPageId(int parentPageId) {
        this.parentPageId = parentPageId;
    }

    @Override
    public int getFreeSpaceOffsetStart() {
        return this.freeSpaceOffsetStart;
    }

    @Override
    public void setFreeSpaceOffsetStart(int freeSpaceOffsetStart) {
        this.freeSpaceOffsetStart = freeSpaceOffsetStart;
    }

    @Override
    public int getFreeSpaceOffsetEnd() {
        return this.freeSpaceOffsetEnd;
    }

    @Override
    public void setFreeSpaceOffsetEnd(int freeSpaceOffsetEnd) {
        this.freeSpaceOffsetEnd = freeSpaceOffsetEnd;
    }

    @Override
    public int getRightPage() {
        return 0;
    }

    @Override
    public void setRightPage(int rightPage) {
    }

    @Override
    public String getKeys() {
        return Arrays.toString(this.keys);
    }

    @Override
    public void setKeys(int[] keys) {
        this.keys = keys;
    }

    @Override
    public String[] getValues() {
        return new String[0];
    }

    @Override
    public void setValues(String[] values) {
    }

    @Override
    public int[] getChildren() {
        return this.children;
    }

    @Override
    public void setChildren(int[] children) {
        this.children = children;
    }

    public Slot[] getSlots() {
        return this.slots;
    }

    @Override
    public void setSlots(Slot[] slots) {
        this.slots = slots;
    }

    @Override
    public RandomAccessFile getFile() {
        return this.file;
    }

    public int searchPage(int key) {
        System.out.println("searching for key in internal page " + key);
        int pos = 0;
        System.out.println("nk " + this.numOfKeys);
        while (pos < this.numOfKeys) {
            if (key > this.keys[pos]) {
                pos++;
            } else if (key == this.keys[pos]) {
                System.out.println("match found");
            } else {
                break;
            }
        }

        return pos;
    }

    @Override
    public String insert(int key, String value, Btree tree) {
        System.out.println("inserting in the internal page");
        int pos = this.searchPage(key);
        System.out.println("going to page at pos " + pos);
        Page page = BufferManager.getPage(this.children[pos], this.file);
        assert page != null;
        return page.insert(key, value, tree);
    }


    @Override
    public String search(int key) {
        int pos = 0;
        while (pos < this.numOfKeys) {
            if (key >= this.keys[pos]) {
                pos++;
            } else {
                break;
            }
        }
        Page page = BufferManager.getPage(this.children[pos], this.file);
        return page.search(key);
    }

    private int insertKeyInPage(int key) {
        int pos = 0;
        System.out.println("inserting key " + key + " in page " + this.getKeys());

        while (pos < this.numOfKeys) {
            if (key > this.keys[pos]) {
                pos++;
            } else if (key == this.keys[pos]) {
                System.out.println("key already exists, no duplicates allowed for now");
                return -1;
            } else {
                break;
            }
        }
        int leftChildPointer = this.freeSpaceOffsetEnd - 4;
        this.freeSpaceOffsetEnd = leftChildPointer;
        int rightChildPointer = this.freeSpaceOffsetEnd - 4;
        this.freeSpaceOffsetEnd = rightChildPointer;
        InternalSlot slot = new InternalSlot(4, key, leftChildPointer, rightChildPointer);


        for (int i = this.numOfKeys - 1; i >= pos; i--) {
            this.keys[i + 1] = this.keys[i];
            this.slots[i + 1] = this.slots[i];
        }
        this.keys[pos] = key;
        this.slots[pos] = slot;
        this.numOfKeys++;
        this.freeSpaceOffsetStart += Constants.InternalSlotSize;

        System.out.println("inserted key at pos " + pos + " " + this.getKeys());
        this.isDirty = true;
        return pos;
    }

    public void maxKeyThresholdReached(int key, Page rightChild, Btree tree) {
        System.out.println("this is the median Key " + key);
        System.out.println("this is the rightChild " + rightChild.toString());
        if (this.numOfKeys < Constants.MAX_NUM_OF_KEYS) {
//            insert here, space available
            int index = this.insertKeyInPage(key);
            for (int i = this.numOfKeys; i > index + 1; i--) {
                this.children[i] = this.children[i - 1];
            }
            this.children[index + 1] = rightChild.getPageId();
            rightChild.setParentPageId(this.pageId);
        } else {

//            insert the key and childId here
//            and perform split again
            int index = this.insertKeyInPage(key);
            for (int i = this.numOfKeys; i > index + 1; i--) {
                this.children[i] = this.children[i - 1];
            }
            this.children[index + 1] = rightChild.getPageId();
            rightChild.setParentPageId(this.pageId);

            System.out.println("here also max key reached");
            System.out.println(this.numOfKeys);
            System.out.println(Arrays.toString(this.keys));

            /*  splitting internal page  */
            int medianIndex = this.numOfKeys / 2;
            int medianKey = this.keys[medianIndex];

            InternalPage rightPage = new InternalPage(this.file);
            System.out.println("page id of the right internal page is " + rightPage.pageId);

            int j = 0;
            for (int i = medianIndex + 1; i < this.numOfKeys; i++) {
                rightPage.keys[j] = this.keys[i];
                int leftChildPointer = rightPage.freeSpaceOffsetEnd - 4;
                rightPage.freeSpaceOffsetEnd = leftChildPointer;
                int rightChildPointer = rightPage.freeSpaceOffsetEnd - 4;
                rightPage.freeSpaceOffsetEnd = rightChildPointer;
                InternalSlot newslot = new InternalSlot(4, rightPage.keys[j], leftChildPointer, rightChildPointer);

                rightPage.slots[j] = newslot;
                rightPage.freeSpaceOffsetStart += Constants.InternalSlotSize;
                rightPage.numOfKeys++;
                this.keys[i] = 0;
                this.slots[i] = null;
                j++;
            }
            this.keys[medianIndex] = 0;
            System.out.println("current page is " + this);
            System.out.println("right page is " + rightPage);

            System.out.println("median index is " + medianIndex);
            j = 0;
            for (int i = medianIndex + 1; i < this.numOfKeys + 1; i++) {
                rightPage.children[j] = this.children[i];
                this.children[i] = 0;
                BufferManager.getPage(rightPage.children[j], this.file).setParentPageId(rightPage.pageId);
                j++;
            }
            System.out.println("right page is " + rightPage);

            this.numOfKeys = medianIndex;

            rightPage.isDirty = true;

            if (this.parentPageId == 0) {
                InternalPage parentPage = new InternalPage(this.file);
                parentPage.keys[0] = medianKey;
                parentPage.numOfKeys++;
                parentPage.children[0] = this.pageId;
                parentPage.children[1] = rightPage.pageId;

                int leftChildPointer = parentPage.freeSpaceOffsetEnd - 4;
                parentPage.freeSpaceOffsetEnd = leftChildPointer;
                int rightChildPointer = parentPage.freeSpaceOffsetEnd - 4;
                parentPage.freeSpaceOffsetEnd = rightChildPointer;

                InternalSlot newslot = new InternalSlot(4, medianKey, leftChildPointer, rightChildPointer);
                parentPage.slots[0] = newslot;
                parentPage.freeSpaceOffsetStart += Constants.InternalSlotSize;

                this.parentPageId = parentPage.pageId;
                rightPage.parentPageId = parentPage.pageId;
                tree.root = parentPage;
                parentPage.isDirty = true;

            } else {
                InternalPage parent = (InternalPage) BufferManager.getPage(this.parentPageId, this.file);
                assert parent != null;
                parent.maxKeyThresholdReached(medianKey, rightPage, tree);
            }


        }
    }

    @Override
    public String toString() {
        return "InternalPage{" +
                "isLeaf=" + isLeaf +
                ", isDirty=" + isDirty +
                ", pageId=" + pageId +
                ", numOfKeys=" + numOfKeys +
                ", parentPageId=" + parentPageId +
                ", freeSpaceOffsetStart=" + freeSpaceOffsetStart +
                ", freeSpaceOffsetEnd=" + freeSpaceOffsetEnd +
                ", keys=" + Arrays.toString(keys) +
                ", children=" + Arrays.toString(children) +
                ", slots=" + Arrays.toString(slots) +
                '}';
    }
}
