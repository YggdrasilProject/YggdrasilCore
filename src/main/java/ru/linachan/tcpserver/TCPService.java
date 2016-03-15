package ru.linachan.tcpserver;

import ru.linachan.yggdrasil.YggdrasilCore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TCPService {
    void handleConnection(YggdrasilCore core, InputStream in, OutputStream out) throws IOException;
}
