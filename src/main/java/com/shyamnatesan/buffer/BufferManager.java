package com.shyamnatesan.buffer;


import com.shyamnatesan.Constants;
import com.shyamnatesan.page.Page;
import com.shyamnatesan.page.Slot;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BufferManager {

    private static Map<Integer, Page> BufferPool;

    private final ScheduledExecutorService execService = Executors.newScheduledThreadPool(1);

    public BufferManager() {
        BufferPool = new ConcurrentHashMap<>(100);

        execService.scheduleWithFixedDelay(BufferManager::scanBufferPool, 0, 30, TimeUnit.SECONDS);
    }

    public static void addPageToBuffer(Page page) {
        BufferPool.put(page.getPageId(), page);
    }

    public static Page getPage(int PageId) {
        if (BufferPool.containsKey(PageId)) {
            return BufferPool.get(PageId);
        }
        return null;
    }

    public static Page[] getPages() {
        Page[] pages = new Page[BufferPool.size()];
        int i = 0;
        for (Map.Entry<Integer, Page> entry : BufferPool.entrySet()) {
            pages[i++] = entry.getValue();
        }
        return pages;
    }

    private static void scanBufferPool() {
        System.out.println("scanning the buffer pool");
        for (Map.Entry<Integer, Page> entry : BufferPool.entrySet()) {
            Page page = entry.getValue();
            if (page.isDirty()) {
                flushPageToDisk(page);
            }
        }
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
            System.out.println("now serializing specific to leaf page");
//            serialize rightSibling
            buffer.putInt(page.getRightPage());
//            serialize slots and their corresponding data records/values
            Slot[] slots = page.getSlots();
            String[] values = page.getValues();
            int index = 0;
            for (int i = 0; i < page.getNumKeys(); i++) {
                byte isDeleted = slots[i].isDeleted() ? (byte) 1 : (byte) 0;
                buffer.put(isDeleted);
                buffer.putInt(slots[i].getLengthOfDataRecord());
                int recordOffset = slots[i].getRecordOffsets()[0];
                buffer.putInt(recordOffset);
                buffer.put(recordOffset, values[index++].getBytes(StandardCharsets.UTF_8));
            }
        } else {
            System.out.println("now serializing specific to internal page");
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

    private static void flushPageToDisk(Page page) {
        System.out.println("flushing page " + page.getPageId() + " to disk");
        byte[] serializedPage = serializePageToByteArray(page);
        System.out.println(serializedPage.length);
        RandomAccessFile file = page.getFile();
        int offset = page.getPageId() * Constants.PageSize;
        System.out.println("page offset is " + offset);
        try {
            file.seek(offset);
            file.write(serializedPage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("setting page to dirty");
        page.setIsDirty(false);

    }


}
