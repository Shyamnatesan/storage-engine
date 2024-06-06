package com.shyamnatesan.page;

public interface Slot {
    boolean isDeleted();


    void setIsDeleted(boolean flag);

    int getLengthOfDataRecord();

    int[] getRecordOffsets();

    int getKey();

    void setKey(int key);
}
