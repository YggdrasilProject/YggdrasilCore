package ru.linachan.yggdrasil.common.nio;

public interface MessageLength {

    int byteLength();

    long maxLength();

    long bytesToLength(byte[] bytes);

    byte[] lengthToBytes(long length);

}