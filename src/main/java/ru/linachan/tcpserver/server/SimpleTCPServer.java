package ru.linachan.tcpserver.server;

import com.google.common.base.Joiner;
import ru.linachan.tcpserver.TCPService;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.notification.YggdrasilNotification;
import ru.linachan.yggdrasil.notification.YggdrasilNotificationManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleTCPServer implements TCPService {

    @Override
    public void handleConnection(YggdrasilCore core, InputStream in, OutputStream out) throws IOException {
        List<String> requestData = new ArrayList<>();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(in));
        inputReader.lines().forEach(requestData::add);

        core.getManager(YggdrasilNotificationManager.class).sendNotification(new YggdrasilNotification(
            "SimpleTCPServer", "New Incoming Data", Joiner.on("\n").join(requestData)
        ));

        in.close();
        out.close();
    }
}
