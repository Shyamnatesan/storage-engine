package com.shyamnatesan.buffer;


import com.shyamnatesan.diskIO.DiskManager;
import com.shyamnatesan.page.Page;

import java.io.RandomAccessFile;
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

        execService.scheduleWithFixedDelay(BufferManager::scanBufferPool, 0, 25, TimeUnit.SECONDS);
    }

    public static void addPageToBuffer(Page page) {
        BufferPool.put(page.getPageId(), page);
    }

    public static Page getPage(int pageId, RandomAccessFile file) {
        if (BufferPool.containsKey(pageId)) {
            return BufferPool.get(pageId);
        } else {
            return DiskManager.readPage(pageId, file);
        }
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
                DiskManager.writePage(page);
                page.setIsDirty(false);
            }
        }
        BufferPool.clear();
    }
}
