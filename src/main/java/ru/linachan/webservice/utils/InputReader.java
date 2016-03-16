package ru.linachan.webservice.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class InputReader {

    private InputStream inputStream;

    public InputReader(InputStream inputStreamObject) {
        inputStream = inputStreamObject;
    }

    public byte[] read(int bytes) throws IOException {
        byte[] byteArray = new byte[bytes];
        inputStream.read(byteArray);
        return byteArray;
    }

    public String readLine() throws IOException {
        StringBuilder string = new StringBuilder();

        byte[] buffer = new byte[1];
        while (inputStream.read(buffer) != 0) {
            if (Arrays.equals(buffer, "\r".getBytes())) {
                inputStream.skip(1);
                break;
            } else if (Arrays.equals(buffer, "\n".getBytes())) {
                break;
            } else {
                string.append(new String(buffer));
            }
        }

        return string.toString();
    }

    public int available() throws IOException {
        return inputStream.available();
    }
}
