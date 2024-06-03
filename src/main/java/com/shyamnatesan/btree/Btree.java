package com.shyamnatesan.btree;

import com.shyamnatesan.buffer.BufferManager;
import com.shyamnatesan.page.LeafPage;
import com.shyamnatesan.page.Page;

public class Btree {
    public Page root;
    private final BufferManager bufferManager;

    public Btree() {
        this.bufferManager = new BufferManager();
        this.root = new LeafPage();
    }


    public String insert(int key, String value) {
        return this.root.insert(key, value, this);
    }

}
