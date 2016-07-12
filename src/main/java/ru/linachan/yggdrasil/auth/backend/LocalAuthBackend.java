package ru.linachan.yggdrasil.auth.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.auth.YggdrasilAuthBackend;
import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;
import ru.linachan.yggdrasil.storage.YggdrasilStorageFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class LocalAuthBackend implements YggdrasilAuthBackend {

    private YggdrasilStorageFile authData;

    private static Logger logger = LoggerFactory.getLogger(LocalAuthBackend.class);

    @Override
    public void onBackendInit() {
        try {
            authData = core.getStorage().createStorage(
                "authStorage", new File("authData.yds"), "Yggdrasil".getBytes(), false
            );
        } catch (IOException e) {
            logger.error("Unable to initialize storage: {}", e.getMessage());
        }
    }

    @Override
    public YggdrasilAuthUser registerUser(String userName) {
        if (authData == null)
            return null;

        if (authData.hasKey(userName))
            return null;

        YggdrasilAuthUser authUser = new YggdrasilAuthUser(userName);

        try {
            authData.putObject(userName, authUser);
        } catch (IOException e) {
            logger.error("Unable to register user: {}", e.getMessage());
            return null;
        }

        return authUser;
    }

    @Override
    public YggdrasilAuthUser getUser(String userName) {
        if (authData == null)
            return null;

        if (!authData.hasKey(userName))
            return null;

        YggdrasilAuthUser authUser = null;

        try {
            authUser = authData.getObject(userName, YggdrasilAuthUser.class);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Unable to read user data: {}", e.getMessage());
        }

        return authUser;
    }

    @Override
    public boolean updateUser(YggdrasilAuthUser user) {
        if (authData == null)
            return false;

        if (!authData.hasKey(user.getUserName()))
            return false;

        try {
            authData.putObject(user.getUserName(), user);
            return true;
        } catch (IOException e) {
            logger.error("Unable to update user data: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<YggdrasilAuthUser> listUsers() {
        return authData.listKeys().stream().map(userName -> {
            YggdrasilAuthUser userData = null;
            try {
                userData = authData.getObject(userName, YggdrasilAuthUser.class);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return userData;
        }).collect(Collectors.toList());
    }

    @Override
    public void shutdown() {
        try {
            authData.writeStorage();
        } catch (InterruptedException | IOException e) {
            logger.error("Unable to write storage file: {}", e.getMessage());
        }
    }
}
