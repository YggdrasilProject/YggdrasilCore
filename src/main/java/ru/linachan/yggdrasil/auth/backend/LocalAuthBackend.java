package ru.linachan.yggdrasil.auth.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.auth.YggdrasilAuthBackend;
import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;
import ru.linachan.yggdrasil.storage.YggdrasilStorageFile;

import java.io.File;
import java.io.IOException;

public class LocalAuthBackend extends YggdrasilAuthBackend {

    private YggdrasilStorageFile authData;

    private static Logger logger = LoggerFactory.getLogger(LocalAuthBackend.class);

    @Override
    protected void onBackendInit() {
        try {
            authData = core.getStorage().createStorage(
                "authStorage", new File("authData.yds"), "Yggdrasil".getBytes(), false
            );
        } catch (IOException e) {
            logger.error("Unable to initialize storage: {}", e.getMessage());
        }
    }

    @Override
    protected YggdrasilAuthUser registerUser(String userName) {
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
    protected YggdrasilAuthUser getUser(String userName) {
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
    protected boolean updateUser(YggdrasilAuthUser user) {
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
    public void shutdown() {
        try {
            authData.writeStorage();
        } catch (InterruptedException | IOException e) {
            logger.error("Unable to write storage file: {}", e.getMessage());
        }
    }
}
