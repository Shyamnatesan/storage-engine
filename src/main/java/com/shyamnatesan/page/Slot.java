package com.shyamnatesan.page;

public class Slot {
    public boolean isDeleted;
    public int lengthOfDataRecord;
    public int recordOffset;

    public Slot(int lengthOfDataRecord, int recordOffset) {
        this.isDeleted = false;
        this.lengthOfDataRecord = lengthOfDataRecord;
        this.recordOffset = recordOffset;
    }

    @Override
    public String toString() {
        return "Slot{" +
                "isDeleted=" + isDeleted +
                ", lengthOfDataRecord=" + lengthOfDataRecord +
                ", recordOffset=" + recordOffset +
                '}';
    }
}
