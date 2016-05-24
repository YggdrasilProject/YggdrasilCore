package ru.linachan.yggdrasil.auth;

import ru.linachan.yggdrasil.YggdrasilCore;

import java.util.List;

public abstract class YggdrasilAuthBackend {

    protected YggdrasilCore core;

    public void setUpBackend(YggdrasilCore yggdrasilCore) {
        core = yggdrasilCore;
        onBackendInit();
    }

    protected abstract void onBackendInit();

    protected abstract YggdrasilAuthUser registerUser(String userName);

    protected abstract YggdrasilAuthUser getUser(String userName);

    protected abstract boolean updateUser(YggdrasilAuthUser user);

    protected abstract List<YggdrasilAuthUser> listUsers();

    public abstract void shutdown();
}
