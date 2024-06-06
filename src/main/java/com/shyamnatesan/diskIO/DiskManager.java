package com.shyamnatesan.diskIO;

import com.shyamnatesan.Constants;
import com.shyamnatesan.page.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class DiskManager {

    public static void writePage(Page page) {
        byte[] serializedPage = serializePageToByteArray(page);
        int offset = page.getPageId() * Constants.PageSize;
        RandomAccessFile file = page.getFile();
        try {
            file.seek(offset);
            file.write(serializedPage);
            System.out.println("page " + page.getPageId() + " successfully written to disk at offset " + offset + " " + page);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }


    public static Page readPage(int pageId, RandomAccessFile file) {
//        System.out.println("reading page from disk");
        byte[] pageBytes = new byte[4096];
        int offset = pageId * Constants.PageSize;
        try {
            file.seek(offset);
            file.read(pageBytes);
//            System.out.println("successfully read page from disk ");
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return deserializeByteArrayToPage(pageBytes, file);
    }

    public static Page deserializeByteArrayToPage(byte[] pageBytes, RandomAccessFile file) {
//        System.out.println("starting deserialization");
        ByteBuffer buffer = ByteBuffer.wrap(pageBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        Page page;
        boolean isLeaf = buffer.get() == 1;
        page = isLeaf ? new LeafPage(file) : new InternalPage(file);
        page.setIsLeaf(isLeaf);

//        common for both pages
        page.setPageId(buffer.getInt());
        page.setNumKeys(buffer.getInt());
        page.setParentPageId(buffer.getInt());
        page.setFreeSpaceOffsetStart(buffer.getInt());
        page.setFreeSpaceOffsetEnd(buffer.getInt());
        int[] keys = new int[Constants.M];
        Slot[] slots = new Slot[Constants.M];
        String[] values = new String[Constants.M];
        if (isLeaf) {
//            System.out.println("deserialization specific to leaf page");
            page.setRightPage(buffer.getInt());
            for (int i = 0; i < page.getNumKeys(); i++) {
                boolean isDeleted = buffer.get() == 1;
                int key = buffer.getInt();
                int lengthOfDataRecord = buffer.getInt();
                int recordOffset = buffer.getInt();
//                System.out.println("buffer position before reading string " + buffer.position());
                byte[] valueBytes = new byte[lengthOfDataRecord];
                buffer.get(recordOffset, valueBytes);
                String value = new String(valueBytes, StandardCharsets.UTF_8);
//                System.out.println("buffer position after reading string " + buffer.position());
                keys[i] = key;
                Slot slot = new LeafSlot(lengthOfDataRecord, recordOffset, key);
                slots[i] = slot;
                values[i] = value;
            }
            page.setKeys(keys);
            page.setValues(values);
            page.setSlots(slots);
        } else {
//            System.out.println("deserialization specific to internal page");
            int[] children = new int[Constants.M + 1];
//            if (page.getPageId() == 7) {
//                System.out.println("rakkama " + page.getNumKeys());
//            }
            for (int i = 0; i < page.getNumKeys(); i++) {
                boolean isDeleted = buffer.get() == 1;
                int key = buffer.getInt();
                int lengthOfDataRecord = buffer.getInt();
                int leftChildPointer = buffer.getInt();
                int rightChildPointer = buffer.getInt();
                Slot slot = new InternalSlot(lengthOfDataRecord, key, leftChildPointer, rightChildPointer);
                int leftChildId = buffer.getInt(leftChildPointer);
                int rightChildId = buffer.getInt(rightChildPointer);
                keys[i] = key;
//                if (page.getPageId() == 7) {
//                    System.out.println("key " + key);
//                }
                slots[i] = slot;
                if (i == page.getNumKeys() - 1) {
                    children[i] = leftChildId;
                    children[i + 1] = rightChildId;
                } else {
                    children[i] = leftChildId;
                }
            }
//            if (page.getPageId() == 7) {
//                System.out.println("rakkama " + keys);
//            }
            page.setKeys(keys);
            page.setSlots(slots);
            page.setChildren(children);
        }

        return page;
    }


    private static byte[] serializePageToByteArray(Page page) {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.PageSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

//        common for both leaf and internal pages
        byte isLeaf = page.isLeaf() ? (byte) 1 : (byte) 0;
        buffer.put(isLeaf);
        buffer.putInt(page.getPageId());
        buffer.putInt(page.getNumKeys());
        buffer.putInt(page.getParentPageId());
        buffer.putInt(page.getFreeSpaceOffsetStart());
        buffer.putInt(page.getFreeSpaceOffsetEnd());

        if (page.isLeaf()) {
//            System.out.println("now serializing specific to leaf page");
//            serialize rightSibling
            buffer.putInt(page.getRightPage());
//            serialize slots and their corresponding data records/values
            Slot[] slots = page.getSlots();
            String[] values = page.getValues();
            int index = 0;
            for (int i = 0; i < page.getNumKeys(); i++) {
                byte isDeleted = slots[i].isDeleted() ? (byte) 1 : (byte) 0;
                buffer.put(isDeleted);
                buffer.putInt(slots[i].getKey());
                buffer.putInt(slots[i].getLengthOfDataRecord());
                int recordOffset = slots[i].getRecordOffsets()[0];
                buffer.putInt(recordOffset);
                buffer.put(recordOffset, values[index++].getBytes(StandardCharsets.UTF_8));
            }
        } else {
//            System.out.println("now serializing specific to internal page");
//            serialize slots and their corresponding child offsets
            Slot[] slots = page.getSlots();
            System.out.println("internal slots " + slots);
            int[] children = page.getChildren();
            System.out.println("internal children " + children);
            int index = 0;
            for (int i = 0; i < page.getNumKeys(); i++) {
                if (slots[i] != null) {
                    byte isDeleted = slots[i].isDeleted() ? (byte) 1 : (byte) 0;
                    buffer.put(isDeleted);
                    buffer.putInt(slots[i].getKey());
                    buffer.putInt(slots[i].getLengthOfDataRecord());
                    int[] recordOffsets = slots[i].getRecordOffsets();
//                    recordOffsets = [leftChildPointer, rightChildPointer]
                    buffer.putInt(recordOffsets[0]);
                    buffer.putInt(recordOffsets[1]);
                    buffer.putInt(recordOffsets[0], children[index]);
                    buffer.putInt(recordOffsets[1], children[index + 1]);
                    index++;
                }
            }
        }

        System.out.println("finished serialization");

        return buffer.array();
    }
}
