package com.shyamnatesan.page;

import com.shyamnatesan.Constants;
import com.shyamnatesan.btree.Btree;
import com.shyamnatesan.buffer.BufferManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class LeafPage implements Page {
    public boolean isLeaf;
    public boolean isDirty;
    public int pageId;
    public int numOfKeys;
    public int parentPageId;
    public int freeSpaceOffsetStart;
    public int freeSpaceOffsetEnd;
    public int[] keys;
    public String[] values;
    public Slot[] slots;
    public byte[] data;
    public int rightSibling;


    public LeafPage() {
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
        this.data = new byte[Constants.PageSize];
        this.rightSibling = 0;
        this.generatePageHeader();
        BufferManager.addPageToBuffer(this);
    }

    /*
     * generatePageHeader()
     *  - this method is called in the constructor when a page is created.
     *  - this method populates the header metadata of the page
     */
    @Override
    public void generatePageHeader() {
        ByteBuffer buffer = ByteBuffer.wrap(this.data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        byte pageType = isLeaf ? (byte) 0 : (byte) 1;
        buffer.put(pageType); // 1 byte
        buffer.putInt(this.pageId); // 4 bytes
        buffer.putInt(this.parentPageId); // 4 bytes
        buffer.putInt(this.freeSpaceOffsetStart); // 4 bytes
        buffer.putInt(this.freeSpaceOffsetEnd); // 4 bytes
        buffer.putInt(this.numOfKeys); // 4 bytes
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
    public String getKeys() {
        return Arrays.toString(this.keys);
    }

    @Override
    public String getChildren() {
        return "";
    }

    @Override
    public String getSlots() {
        String result = "";
        for (Slot slot : this.slots) {
            if (slot != null) {
                result += slot.toString();
                result += " ; ";
            }
        }
        return result;
    }

    @Override
    public String getData() {
        return Arrays.toString(this.data);
    }

    @Override
    public String insert(int key, String value, Btree tree) {
        System.out.println("inserting key " + key + " in page " + Arrays.toString(this.keys));
        System.out.println("the current page has " + this.numOfKeys + " keys");
        byte[] dataRecord = this.constructDataRecord(value);
        int lengthOfDataRecord = dataRecord.length;
        int recordOffset = this.freeSpaceOffsetEnd - lengthOfDataRecord;
        Slot slot = new Slot(lengthOfDataRecord, recordOffset);

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
        this.numOfKeys++;
        this.isDirty = true;

        byte[] slotSerialized = this.getSerializedSlots(slots);
        ByteBuffer dataBuffer = ByteBuffer.wrap(this.data);
        dataBuffer.put(Constants.PageHeaderSize, slotSerialized);
        dataBuffer.put(this.freeSpaceOffsetEnd - lengthOfDataRecord, dataRecord);

        this.freeSpaceOffsetEnd = slot.recordOffset;
        this.freeSpaceOffsetStart = Constants.PageHeaderSize + (Constants.Slotsize * this.numOfKeys);

        if (this.numOfKeys > Constants.MAX_NUM_OF_KEYS) {
            this.maxKeyThresholdReached(tree);
        }


        return this.toString();
    }

    @Override
    public void setParentPageId(int parentPageId) {
        this.parentPageId = parentPageId;
    }


    public void maxKeyThresholdReached(Btree tree) {
        System.out.println("Max Key threshold has reached. So we split");
        /*  Splitting page  */
        int medianIndex = this.numOfKeys / 2;
        int medianKey = this.keys[medianIndex];

        LeafPage rightPage = new LeafPage();
        System.out.println("page id of rightpage is " + rightPage.pageId);

        this.rightSibling = rightPage.pageId;

        int j = 0;
        for (int i = medianIndex; i < this.numOfKeys; i++) {
            rightPage.keys[j] = this.keys[i];
            rightPage.values[j] = this.values[i];
            rightPage.numOfKeys++;
            this.keys[i] = 0;
            this.values[i] = "";
            this.slots[i].isDeleted = true;
            j++;
        }
        this.numOfKeys = medianIndex;

        ByteBuffer dataBuffer = ByteBuffer.wrap(rightPage.data);

        for (int i = 0; i < rightPage.numOfKeys; i++) {
            byte[] dataRecord = this.constructDataRecord(rightPage.values[i]);
            Slot slot = new Slot(dataRecord.length, rightPage.freeSpaceOffsetEnd - dataRecord.length);
            System.out.println("rightpage slot of i " + i + " is " + slot);
            rightPage.slots[i] = slot;
            byte[] slotSerialized = getSerializedSlots(slot);
            dataBuffer.put(rightPage.freeSpaceOffsetStart, slotSerialized);
            rightPage.freeSpaceOffsetStart += Constants.Slotsize;
            dataBuffer.put(slot.recordOffset, dataRecord);
            rightPage.freeSpaceOffsetEnd = slot.recordOffset;
        }
        rightPage.isDirty = true;

        System.out.println("current page is " + this);
        System.out.println("right page is " + rightPage);

        if (this.parentPageId == 0) {
            System.out.println("creating a new parent page");
            InternalPage parentPage = new InternalPage();
            parentPage.keys[0] = medianKey;
            parentPage.children[0] = this.pageId;
            parentPage.children[1] = rightPage.pageId;
            this.parentPageId = parentPage.pageId;
            rightPage.parentPageId = parentPage.pageId;
            tree.root = parentPage;
            parentPage.isDirty = true;
            parentPage.numOfKeys++;
            System.out.println("this is the parent page aka new root " + parentPage);
        } else {
            InternalPage parent = (InternalPage) BufferManager.getPage(this.parentPageId);
            parent.maxKeyThresholdReached(medianKey, rightPage, tree);
        }


    }


    private byte[] constructDataRecord(String value) {
        int lengthOfValue = value.length();
        ByteBuffer buffer = ByteBuffer.allocate(4 + lengthOfValue);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(lengthOfValue);
        buffer.put(value.getBytes(StandardCharsets.UTF_8));
        return buffer.array();
    }


    private byte[] getSerializedSlots(Slot[] slots) {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.Slotsize * slots.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (Slot slot : slots) {
            if (slot != null) {
                byte isDeleted = slot.isDeleted ? (byte) 1 : (byte) 0;
                buffer.put(isDeleted);
                buffer.putInt(slot.lengthOfDataRecord);
                buffer.putInt(slot.recordOffset);
            }
        }
        return buffer.array();
    }

    private byte[] getSerializedSlots(Slot slot) {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.Slotsize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte isDeleted = slot.isDeleted ? (byte) 1 : (byte) 0;
        buffer.put(isDeleted);
        buffer.putInt(slot.lengthOfDataRecord);
        buffer.putInt(slot.recordOffset);
        return buffer.array();

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
                ", keys=" + Arrays.toString(keys) +
                ", values=" + Arrays.toString(values) +
                ", slots=" + Arrays.toString(slots) +
                ", data=" + Arrays.toString(data) +
                ", rightSibling=" + rightSibling +
                '}';
    }


}
