package ru.linachan.tcpserver;

import ru.linachan.yggdrasil.YggdrasilCore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

public interface TCPService {
    void handleConnection(YggdrasilCore core, InputStream in, OutputStream out, InetAddress clientAddress) throws IOException;
}
