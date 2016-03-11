package ru.linachan.yggdrasil.shell;

import org.apache.sshd.common.forward.TcpipForwarder;
import org.apache.sshd.common.forward.TcpipForwarderFactory;
import org.apache.sshd.common.session.ConnectionService;
import ru.linachan.yggdrasil.YggdrasilCore;

public class YggdrasilShellTCPIPFactory implements TcpipForwarderFactory {

    private YggdrasilCore core;

    public YggdrasilShellTCPIPFactory(YggdrasilCore yggdrasilCore) {
        core = yggdrasilCore;
    }

    @Override
    public TcpipForwarder create(ConnectionService connectionService) {
        return new YggdrasilShellTCPIP(core, connectionService);
    }
}
