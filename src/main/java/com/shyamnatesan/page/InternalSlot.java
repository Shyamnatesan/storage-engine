package com.shyamnatesan.page;

public class InternalSlot implements Slot {
    public boolean isDeleted;
    public int lengthOfDataRecord;
    public int leftChildPointer;
    public int rightChildPointer;


    public InternalSlot(int lengthOfDataRecord) {
        this.isDeleted = false;
        this.lengthOfDataRecord = lengthOfDataRecord;
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
