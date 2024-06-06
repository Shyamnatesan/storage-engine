package com.shyamnatesan.storageManager;

import com.shyamnatesan.btree.Btree;
import com.shyamnatesan.buffer.BufferManager;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class StorageManager {

    private static Map<String, FileManager> datafiles;
    private final BufferManager bufferManager;


    public StorageManager() {
        datafiles = new HashMap<>();
        this.bufferManager = new BufferManager();
    }

    public FileManager createFile(String fileName) {
        File fileData = new File("c://Users/shyam/Desktop/PersistentIndex/src/main/java/com/shyamnatesan/files/" + fileName + ".dat");
        RandomAccessFile file;
        try {
            file = new RandomAccessFile(fileData, "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Btree btree = new Btree(file);

        FileManager fileManager = new FileManager(fileName, file, btree, this.bufferManager);
        datafiles.put(fileName, fileManager);

        return fileManager;
    }
}
