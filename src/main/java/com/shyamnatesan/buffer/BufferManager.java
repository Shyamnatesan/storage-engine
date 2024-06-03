package com.shyamnatesan.buffer;

import com.shyamnatesan.page.LeafPage;
import com.shyamnatesan.page.Page;

import java.util.HashMap;
import java.util.Map;

public class BufferManager {

    private static Map<Integer, Page> BufferPool;
    private static final int dirtyPageThreshold = 7;

    public BufferManager() {
        this.BufferPool = new HashMap<>(100);
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


}
