package ru.linachan.yggdrasil.auth;

import ru.linachan.yggdrasil.YggdrasilCore;

import java.util.List;

public interface YggdrasilAuthBackend {

    YggdrasilCore core = YggdrasilCore.INSTANCE;

    void onBackendInit();
    YggdrasilAuthUser registerUser(String userName);
    YggdrasilAuthUser getUser(String userName);
    boolean updateUser(YggdrasilAuthUser user);
    List<YggdrasilAuthUser> listUsers();
    void shutdown();
}
