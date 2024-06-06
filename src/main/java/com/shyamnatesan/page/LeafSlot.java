package com.shyamnatesan.page;

public class LeafSlot implements Slot {
    public boolean isDeleted;
    public int lengthOfDataRecord;
    public int recordOffset;


    public LeafSlot(int lengthOfDataRecord, int recordOffset) {
        this.isDeleted = false;
        this.lengthOfDataRecord = lengthOfDataRecord;
        this.recordOffset = recordOffset;
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
        int[] result = new int[1];
        result[0] = this.recordOffset;
        return result;
    }

    @Override
    public String toString() {
        return "LeafSlot{" +
                "isDeleted=" + isDeleted +
                ", lengthOfDataRecord=" + lengthOfDataRecord +
                ", recordOffset=" + recordOffset +
                '}';
    }
}
