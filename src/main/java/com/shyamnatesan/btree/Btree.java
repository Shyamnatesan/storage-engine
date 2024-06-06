package com.shyamnatesan.btree;

import com.shyamnatesan.page.LeafPage;
import com.shyamnatesan.page.Page;

import java.io.*;

public class Btree {
    public Page root;

    public Btree(RandomAccessFile file) {
        this.root = new LeafPage(file);
    }


    public String insert(int key, String value) {
        return this.root.insert(key, value, this);
    }

}
