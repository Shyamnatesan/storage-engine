package com.shyamnatesan.page;

import com.shyamnatesan.Constants;
import com.shyamnatesan.btree.Btree;
import com.shyamnatesan.buffer.BufferManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    public byte[] data;

    public InternalPage() {
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
        this.slots = new Slot[Constants.M + 1];
        this.data = new byte[Constants.PageSize];
        this.generatePageHeader();
        BufferManager.addPageToBuffer(this);
    }

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
        return Arrays.toString(this.children);
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

    public int searchPage(int key) {
        System.out.println("searching for key in internal page " + key);
        int pos = 0;
        System.out.println("nk " + this.numOfKeys);
        for (; pos < this.numOfKeys; ) {
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
        Page page = BufferManager.getPage(this.children[pos]);
        return page.insert(key, value, tree);
    }

    @Override
    public void setParentPageId(int parentPageId) {
        this.parentPageId = parentPageId;
    }

    private int insertKeyInPage(int key) {
        int pos = 0;
        System.out.println("inserting key " + key + " in page " + this.getKeys());
        System.out.println(" num k : " + this.numOfKeys);

        for (; pos < this.numOfKeys; ) {
            if (key > this.keys[pos]) {
                pos++;
            } else if (key == this.keys[pos]) {
                System.out.println("key already exists, no duplicates allowed for now");
                return -1;
            } else {
                break;
            }
        }
        for (int i = this.numOfKeys - 1; i >= pos; i--) {
            this.keys[i + 1] = this.keys[i];
        }
        this.keys[pos] = key;
        this.numOfKeys++;

        System.out.println("inserted key at pos " + pos + " " + this.getKeys());
        return pos;
    }

    public void maxKeyThresholdReached(int key, Page rightChild, Btree tree) {
        System.out.println("this is the median Key " + key);
        System.out.println("this is the rightChild " + rightChild.toString());
        if (this.numOfKeys < Constants.MAX_NUM_OF_KEYS) {
//            insert here, space available
            int index = this.insertKeyInPage(key);
            if (rightChild != null) {
                for (int i = this.numOfKeys; i > index + 1; i--) {
                    this.children[i] = this.children[i - 1];
                }
                this.children[index + 1] = rightChild.getPageId();
                rightChild.setParentPageId(this.pageId);
            }
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

            InternalPage rightPage = new InternalPage();
            System.out.println("page id of the right internal page is " + rightPage.pageId);

            int j = 0;
            for (int i = medianIndex + 1; i < this.numOfKeys; i++) {
                rightPage.keys[j] = this.keys[i];
                rightPage.numOfKeys++;
                this.keys[i] = 0;
//                System.out.println(i);
//                this.slots[i].isDeleted = true;
                j++;
            }
            this.keys[medianIndex] = 0;
            System.out.println("current page is " + this.toString());
            System.out.println("right page is " + rightPage.toString());
            j = 0;
            System.out.println("median index is " + medianIndex);
            for (int i = medianIndex + 1; i < this.numOfKeys + 1; i++) {
                rightPage.children[j] = this.children[i];
                System.out.println(rightPage.getChildren());
                this.children[i] = 0;
                BufferManager.getPage(rightPage.children[j]).setParentPageId(rightPage.pageId);
                j++;
            }
            System.out.println("right page is " + rightPage.toString());

            this.numOfKeys = medianIndex;

            ByteBuffer dataBuffer = ByteBuffer.wrap(rightPage.data);
            for (int i = 0; i < rightPage.numOfKeys + 1; i++) {
                byte childPageId = (byte) rightPage.children[i];
                Slot slot = new Slot(4, rightPage.freeSpaceOffsetEnd - 4);
                rightPage.slots[i] = slot;
                byte[] slotSerialized = getSerializedSlots(slot);
                dataBuffer.put(rightPage.freeSpaceOffsetStart, slotSerialized);
                rightPage.freeSpaceOffsetStart += Constants.Slotsize;
                dataBuffer.put(slot.recordOffset, childPageId);
                rightPage.freeSpaceOffsetEnd = slot.recordOffset;
            }
            rightPage.isDirty = true;

            if (this.parentPageId == 0) {
                InternalPage parentPage = new InternalPage();
                parentPage.keys[0] = medianKey;
                parentPage.numOfKeys++;
                parentPage.children[0] = this.pageId;
                parentPage.children[1] = rightPage.pageId;
                this.parentPageId = parentPage.pageId;
                rightPage.parentPageId = parentPage.pageId;
                tree.root = parentPage;

            } else {
                InternalPage parent = (InternalPage) BufferManager.getPage(this.parentPageId);
                parent.maxKeyThresholdReached(medianKey, rightPage, tree);
            }


        }
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
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
