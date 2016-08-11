package ru.linachan.yggdrasil.storage;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class YggdrasilStorageFile {

    private final Map<String, byte[]> storageMap;
    private final Semaphore storageLock;
    private final File storageFile;

    private byte[] storageKey;

    private final byte[] MAGIC_HEADER = "YDSv1.0".getBytes();

    public YggdrasilStorageFile(File storageFileObject) {
        storageFile = storageFileObject;

        storageLock = new Semaphore(1);
        storageMap = new HashMap<>();
        storageKey = MAGIC_HEADER;

        readStorage();
    }

    public YggdrasilStorageFile(File storageFileObject, byte[] storageKeyBytes) {
        storageFile = storageFileObject;

        storageLock = new Semaphore(1);
        storageMap = new HashMap<>();
        storageKey = storageKeyBytes;

        readStorage();
    }

    public void setStorageKey(byte[] newStorageKey) {
        storageKey = newStorageKey;
    }

    private byte[] encode(byte[] inputData) {
        byte[] result = new byte[inputData.length];

        for (int elementID = 0; elementID < inputData.length; elementID++) {
            result[elementID] = (byte) (inputData[elementID] ^ storageKey[elementID % storageKey.length]);
        }

        return result;
    }

    private Integer calculateStorageSize() {
        Integer totalSize = 0;
        if (storageMap != null) {
            for (String key : storageMap.keySet()) {
                totalSize += 8 + key.getBytes().length + storageMap.get(key).length;
            }
        }
        return totalSize;
    }

    public boolean readStorage() {
        try {
            while (storageLock.availablePermits() == 0) Thread.sleep(100);
        } catch (InterruptedException ignored) {}

        storageMap.clear();

        try {
            GZIPInputStream storageReader = new GZIPInputStream(
                    new FileInputStream(storageFile)
            );

            byte[] RAW_MAGIC_HEADER = new byte[MAGIC_HEADER.length];

            if (storageReader.read(RAW_MAGIC_HEADER) != MAGIC_HEADER.length)
                return false;

            if (!Arrays.equals(MAGIC_HEADER, RAW_MAGIC_HEADER))
                return false;

            byte[] storageLengthByte = new byte[4];

            if (storageReader.read(storageLengthByte) != 4)
                return false;

            Integer storageLength = ByteBuffer.wrap(storageLengthByte).getInt();

            byte[] storageArray = new byte[storageLength];

            if (storageReader.read(storageArray) != storageLength)
                return false;

            ByteBuffer storageBuffer = ByteBuffer.wrap(storageArray);
            Integer storageBytesRead = 0;

            while (storageBytesRead < storageLength) {
                int keyLength = storageBuffer.getInt();
                int valueLength = storageBuffer.getInt();

                byte[] keyArray = new byte[keyLength];
                byte[] valueArray = new byte[valueLength];

                storageBuffer.get(keyArray);
                storageBuffer.get(valueArray);

                String keyName = new String(encode(keyArray));
                byte[] valueData = encode(valueArray);

                storageMap.put(keyName, valueData);
                storageBytesRead += 8 + keyLength + valueLength;
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void writeStorage() throws InterruptedException, IOException {
        storageLock.acquire();

        GZIPOutputStream storageWriter = new GZIPOutputStream(
                new FileOutputStream(storageFile)
        );

        storageWriter.write(MAGIC_HEADER);

        Integer storageSize = calculateStorageSize();
        ByteBuffer storageBuffer = ByteBuffer.allocate(storageSize + 4);

        storageBuffer.putInt(storageSize);

        for (String keyName : storageMap.keySet()) {
            byte[] key = keyName.getBytes();
            byte[] value = storageMap.get(keyName);

            storageBuffer.putInt(key.length);
            storageBuffer.putInt(value.length);

            storageBuffer.put(encode(key));
            storageBuffer.put(encode(value));
        }

        storageWriter.write(storageBuffer.array());
        storageWriter.flush();
        storageWriter.close();

        storageLock.release();
    }

    public void clearStorage() throws IOException, InterruptedException {
        storageMap.clear();
        writeStorage();
    }

    public boolean hasKey(String key) {
        return storageMap.containsKey(key);
    }

    public ByteBuffer getKey(String key) {
        return hasKey(key) ? ByteBuffer.wrap(storageMap.get(key)) : null;
    }

    public void putKey(String key, ByteBuffer value) {
        storageMap.put(key, value.array());
    }

    public void deleteKey(String key) {
        if (hasKey(key)) {
            storageMap.remove(key);
        }
    }

    public Set<String> listKeys() {
        return storageMap.keySet();
    }

    public byte[] getBytes(String key) {
        return hasKey(key) ? getKey(key).array() : null;
    }

    public void putBytes(String key, byte[] value) {
        putKey(key, ByteBuffer.wrap(value));
    }

    public Integer getInt(String key) {
        return hasKey(key) ? getKey(key).getInt() : null;
    }

    public void putInt(String key, Integer value) {
        putKey(key, ByteBuffer.allocate(4).putInt(value));
    }

    public Long getLong(String key) {
        return hasKey(key) ? getKey(key).getLong() : null;
    }

    public void putLong(String key, Long value) {
        putKey(key, ByteBuffer.allocate(8).putLong(value));
    }

    public Float getFloat(String key) {
        return hasKey(key) ? getKey(key).getFloat() : null;
    }

    public void putFloat(String key, Float value) {
        putKey(key, ByteBuffer.allocate(4).putFloat(value));
    }

    public Double getDouble(String key) {
        return hasKey(key) ? getKey(key).getDouble() : null;
    }

    public void putDouble(String key, Double value) {
        putKey(key, ByteBuffer.allocate(8).putDouble(value));
    }

    public Byte getByte(String key) {
        return hasKey(key) ? getKey(key).get() : null;
    }

    public void putByte(String key, Byte value) {
        putKey(key, ByteBuffer.allocate(1).put(value));
    }

    public String getString(String key) {
        return hasKey(key) ? new String(getKey(key).array()) : null;
    }

    public void putString(String key, String value) {
        putKey(key, ByteBuffer.wrap(value.getBytes()));
    }

    public Boolean getBool(String key) {
        return hasKey(key) && getKey(key).get() != 0;
    }

    public void putBool(String key, Boolean value) {
        putKey(key, ByteBuffer.allocate(1).put((byte) (value ? 1 : 0)));
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(String key, Class<T> objectClass) throws IOException, ClassNotFoundException {
        if (hasKey(key)) {
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(getBytes(key)));
            return (T) inputStream.readObject();
        }
        return null;
    }

    public <T> void putObject(String key, T object) throws IOException {
        ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(outputArray);
        outputStream.writeObject(object);
        putBytes(key, outputArray.toByteArray());
    }
}
