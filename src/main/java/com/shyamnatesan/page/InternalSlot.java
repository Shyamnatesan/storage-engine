package com.shyamnatesan.page;

public class InternalSlot implements Slot {
    public boolean isDeleted;
    public int lengthOfDataRecord;
    public int leftChildPointer;
    public int rightChildPointer;
    public int key;


    public InternalSlot(int lengthOfDataRecord, int key, int leftChildPointer, int rightChildPointer) {
        this.isDeleted = false;
        this.lengthOfDataRecord = lengthOfDataRecord;
        this.key = key;
        this.leftChildPointer = leftChildPointer;
        this.rightChildPointer = rightChildPointer;
    }

    @Override
    public boolean isDeleted() {
        return this.isDeleted;
    }

    @Override
    public void setIsDeleted(boolean flag) {
        this.isDeleted = flag;
    }

    @Override
    public int getLengthOfDataRecord() {
        return this.lengthOfDataRecord;
    }

    @Override
    public int[] getRecordOffsets() {
        return new int[]{this.leftChildPointer, this.rightChildPointer};
    }

    @Override
    public int getKey() {
        return this.key;
    }

    @Override
    public void setKey(int key) {
        this.key = key;
    }

    public void setLeftChildPointer(int leftChildPointer) {
        this.leftChildPointer = leftChildPointer;
    }

    public void setRightChildPointer(int rightChildPointer) {
        this.rightChildPointer = rightChildPointer;
    }

    @Override
    public String toString() {
        return "InternalSlot{" +
                "isDeleted=" + isDeleted +
                ", lengthOfDataRecord=" + lengthOfDataRecord +
                ", leftChildPointer=" + leftChildPointer +
                ", rightChildPointer=" + rightChildPointer +
                '}';
    }
}
