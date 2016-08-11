package ru.linachan.yggdrasil.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class YggdrasilStorage {

    private final Map<String, YggdrasilStorageFile> storageInfo;

    private final byte[] MAGIC_HEADER = "YDSv1.0".getBytes();

    private static final Logger logger = LoggerFactory.getLogger(YggdrasilStorage.class);

    public YggdrasilStorage() {
        storageInfo = new HashMap<>();
    }

    private boolean validateStorage(File storageFile) {
        if (!(storageFile.exists()&&storageFile.isFile()))
            return false;

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

            return storageReader.read(storageArray) == storageLength;
        } catch (IOException e) {
            logger.error("Unable to validate storage file", e);
            return false;
        }
    }

    private void initializeStorage(File storageFile) throws IOException {
        if (storageFile.exists()&&!storageFile.isFile())
            throw new IOException("Unable to initialize storage");

        GZIPOutputStream storageWriter = new GZIPOutputStream(
            new FileOutputStream(storageFile)
        );

        storageWriter.write(MAGIC_HEADER);
        storageWriter.write(ByteBuffer.allocate(4).putInt(0).array());

        storageWriter.flush();
        storageWriter.close();
    }

    public YggdrasilStorageFile createStorage(String storageName, File storageFile, byte[] storageKey, boolean reInitialize) throws IOException {
        if (storageFile.exists()) {
            boolean isValidStorage = validateStorage(storageFile);

            if (!isValidStorage||reInitialize) {
                initializeStorage(storageFile);
            }
        } else {
            initializeStorage(storageFile);
        }

        YggdrasilStorageFile storageObject;

        if (storageKey != null) {
            storageObject = new YggdrasilStorageFile(storageFile, storageKey);
        } else {
            storageObject = new YggdrasilStorageFile(storageFile);
        }

        storageInfo.put(storageName, storageObject);

        return storageObject;
    }

    public YggdrasilStorageFile getStorage(String storageName) {
        return (storageInfo.containsKey(storageName)) ? storageInfo.get(storageName) : null;
    }

    public void shutdown() {
        for (String storageName: storageInfo.keySet()) {
            try {
                storageInfo.get(storageName).writeStorage();
            } catch (InterruptedException | IOException e) {
                logger.error("Unable to shutdown storage properly", e);
            }
        }
    }
}
