package com.shyamnatesan.storageManager;

import com.shyamnatesan.btree.Btree;
import com.shyamnatesan.buffer.BufferManager;

import java.io.*;

public class FileManager {

    private final String fileName;
    private final RandomAccessFile file;
    private final Btree btree;
    private final BufferManager bufferManager;


    public FileManager(String fileName, RandomAccessFile file, Btree btree, BufferManager bufferManager) {
        this.fileName = fileName;
        this.file = file;
        this.btree = btree;
        this.bufferManager = bufferManager;
    }

    public String Insert(int key, String value) {
        return this.btree.insert(key, value);
    }

    public Btree getBtree() {
        return this.btree;
    }

    public String Search(int key) {
        return btree.search(key);
    }
}
