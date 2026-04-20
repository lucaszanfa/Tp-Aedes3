package DAO;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ExtensibleHashIndex {

    private static final int INITIAL_GLOBAL_DEPTH = 1;
    private static final int DIRECTORY_HEADER_BYTES = 8;
    private static final int BUCKET_HEADER_BYTES = 8;
    private static final int ENTRY_BYTES = 13;

    private final String directoryPath;
    private final String bucketPath;
    private final int bucketCapacity;

    public ExtensibleHashIndex(String directoryPath, String bucketPath, int bucketCapacity) throws IOException {
        this.directoryPath = directoryPath;
        this.bucketPath = bucketPath;
        this.bucketCapacity = bucketCapacity;
        initializeFiles();
    }

    public synchronized void clear() throws IOException {
        File directoryFile = new File(directoryPath);
        File bucketFile = new File(bucketPath);

        ensureParent(directoryFile);
        ensureParent(bucketFile);

        try (RandomAccessFile directory = new RandomAccessFile(directoryFile, "rw");
             RandomAccessFile buckets = new RandomAccessFile(bucketFile, "rw")) {
            directory.setLength(0);
            buckets.setLength(0);

            long bucketZero = allocateBucket(buckets, INITIAL_GLOBAL_DEPTH);
            long bucketOne = allocateBucket(buckets, INITIAL_GLOBAL_DEPTH);

            directory.seek(0);
            directory.writeInt(INITIAL_GLOBAL_DEPTH);
            directory.writeInt(bucketCapacity);
            directory.writeLong(bucketZero);
            directory.writeLong(bucketOne);
        }
    }

    public synchronized Long get(int key) throws IOException {
        try (RandomAccessFile directory = new RandomAccessFile(directoryPath, "r");
             RandomAccessFile buckets = new RandomAccessFile(bucketPath, "r")) {
            int globalDepth = directory.readInt();
            directory.readInt();
            long bucketAddress = readBucketAddress(directory, directoryIndex(key, globalDepth));
            Bucket bucket = readBucket(buckets, bucketAddress);
            int entryIndex = bucket.findEntryIndex(key);
            return entryIndex >= 0 ? bucket.values[entryIndex] : null;
        }
    }

    public synchronized void put(int key, long value) throws IOException {
        while (true) {
            try (RandomAccessFile directory = new RandomAccessFile(directoryPath, "rw");
                 RandomAccessFile buckets = new RandomAccessFile(bucketPath, "rw")) {
                int globalDepth = directory.readInt();
                directory.readInt();
                int index = directoryIndex(key, globalDepth);
                long bucketAddress = readBucketAddress(directory, index);
                Bucket bucket = readBucket(buckets, bucketAddress);

                int existing = bucket.findEntryIndex(key);
                if (existing >= 0) {
                    bucket.values[existing] = value;
                    writeBucket(buckets, bucketAddress, bucket);
                    return;
                }

                if (bucket.count < bucketCapacity) {
                    bucket.insert(key, value);
                    writeBucket(buckets, bucketAddress, bucket);
                    return;
                }

                splitBucket(directory, buckets, globalDepth, bucketAddress);
            }
        }
    }

    public synchronized boolean remove(int key) throws IOException {
        try (RandomAccessFile directory = new RandomAccessFile(directoryPath, "rw");
             RandomAccessFile buckets = new RandomAccessFile(bucketPath, "rw")) {
            int globalDepth = directory.readInt();
            directory.readInt();
            long bucketAddress = readBucketAddress(directory, directoryIndex(key, globalDepth));
            Bucket bucket = readBucket(buckets, bucketAddress);
            if (!bucket.remove(key)) {
                return false;
            }
            writeBucket(buckets, bucketAddress, bucket);
            return true;
        }
    }

    private void initializeFiles() throws IOException {
        File directoryFile = new File(directoryPath);
        File bucketFile = new File(bucketPath);
        ensureParent(directoryFile);
        ensureParent(bucketFile);

        if (!directoryFile.exists() || !bucketFile.exists() || directoryFile.length() < DIRECTORY_HEADER_BYTES) {
            clear();
            return;
        }

        try (RandomAccessFile directory = new RandomAccessFile(directoryFile, "r")) {
            int globalDepth = directory.readInt();
            int storedCapacity = directory.readInt();
            long expectedDirectoryLength = DIRECTORY_HEADER_BYTES + ((long) (1 << globalDepth) * Long.BYTES);
            if (globalDepth < 1 || storedCapacity != bucketCapacity || directory.length() < expectedDirectoryLength) {
                clear();
            }
        }
    }

    private void splitBucket(RandomAccessFile directory, RandomAccessFile buckets, int globalDepth, long bucketAddress) throws IOException {
        Bucket original = readBucket(buckets, bucketAddress);
        int previousLocalDepth = original.localDepth;

        if (previousLocalDepth == globalDepth) {
            doubleDirectory(directory, globalDepth);
            globalDepth++;
        }

        Bucket leftBucket = new Bucket(previousLocalDepth + 1, bucketCapacity);
        Bucket rightBucket = new Bucket(previousLocalDepth + 1, bucketCapacity);
        long newBucketAddress = allocateBucket(buckets, previousLocalDepth + 1);

        int directorySize = 1 << globalDepth;
        int splitBit = 1 << previousLocalDepth;

        for (int i = 0; i < directorySize; i++) {
            long address = readBucketAddress(directory, i);
            if (address == bucketAddress) {
                writeBucketAddress(directory, i, (i & splitBit) == 0 ? bucketAddress : newBucketAddress);
            }
        }

        for (int i = 0; i < original.count; i++) {
            int key = original.keys[i];
            long value = original.values[i];
            int targetIndex = directoryIndex(key, globalDepth);
            if ((targetIndex & splitBit) == 0) {
                leftBucket.insert(key, value);
            } else {
                rightBucket.insert(key, value);
            }
        }

        writeBucket(buckets, bucketAddress, leftBucket);
        writeBucket(buckets, newBucketAddress, rightBucket);
    }

    private void doubleDirectory(RandomAccessFile directory, int globalDepth) throws IOException {
        int directorySize = 1 << globalDepth;
        long[] addresses = new long[directorySize];
        for (int i = 0; i < directorySize; i++) {
            addresses[i] = readBucketAddress(directory, i);
        }

        directory.seek(0);
        directory.writeInt(globalDepth + 1);
        directory.writeInt(bucketCapacity);
        for (long address : addresses) {
            directory.writeLong(address);
        }
        for (long address : addresses) {
            directory.writeLong(address);
        }
    }

    private long allocateBucket(RandomAccessFile buckets, int localDepth) throws IOException {
        long address = buckets.length();
        Bucket bucket = new Bucket(localDepth, bucketCapacity);
        writeBucket(buckets, address, bucket);
        return address;
    }

    private Bucket readBucket(RandomAccessFile buckets, long address) throws IOException {
        buckets.seek(address);
        Bucket bucket = new Bucket(buckets.readInt(), bucketCapacity);
        bucket.count = buckets.readInt();
        for (int i = 0; i < bucketCapacity; i++) {
            boolean active = buckets.readBoolean();
            int key = buckets.readInt();
            long value = buckets.readLong();
            if (active) {
                bucket.keys[bucket.count == 0 ? 0 : i] = key;
                bucket.values[bucket.count == 0 ? 0 : i] = value;
            }
        }

        Bucket normalized = new Bucket(bucket.localDepth, bucketCapacity);
        buckets.seek(address + BUCKET_HEADER_BYTES);
        for (int i = 0; i < bucketCapacity; i++) {
            boolean active = buckets.readBoolean();
            int key = buckets.readInt();
            long value = buckets.readLong();
            if (active) {
                normalized.insert(key, value);
            }
        }
        return normalized;
    }

    private void writeBucket(RandomAccessFile buckets, long address, Bucket bucket) throws IOException {
        buckets.seek(address);
        buckets.writeInt(bucket.localDepth);
        buckets.writeInt(bucket.count);
        for (int i = 0; i < bucketCapacity; i++) {
            boolean active = i < bucket.count;
            buckets.writeBoolean(active);
            buckets.writeInt(active ? bucket.keys[i] : 0);
            buckets.writeLong(active ? bucket.values[i] : 0L);
        }
    }

    private long readBucketAddress(RandomAccessFile directory, int directoryIndex) throws IOException {
        directory.seek(DIRECTORY_HEADER_BYTES + ((long) directoryIndex * Long.BYTES));
        return directory.readLong();
    }

    private void writeBucketAddress(RandomAccessFile directory, int directoryIndex, long address) throws IOException {
        directory.seek(DIRECTORY_HEADER_BYTES + ((long) directoryIndex * Long.BYTES));
        directory.writeLong(address);
    }

    private int directoryIndex(int key, int globalDepth) {
        return key & ((1 << globalDepth) - 1);
    }

    private void ensureParent(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    private static final class Bucket {
        private final int localDepth;
        private final int[] keys;
        private final long[] values;
        private int count;

        private Bucket(int localDepth, int capacity) {
            this.localDepth = localDepth;
            this.keys = new int[capacity];
            this.values = new long[capacity];
            this.count = 0;
        }

        private int findEntryIndex(int key) {
            for (int i = 0; i < count; i++) {
                if (keys[i] == key) {
                    return i;
                }
            }
            return -1;
        }

        private void insert(int key, long value) {
            keys[count] = key;
            values[count] = value;
            count++;
        }

        private boolean remove(int key) {
            int index = findEntryIndex(key);
            if (index < 0) {
                return false;
            }
            for (int i = index; i < count - 1; i++) {
                keys[i] = keys[i + 1];
                values[i] = values[i + 1];
            }
            count--;
            return true;
        }
    }
}
