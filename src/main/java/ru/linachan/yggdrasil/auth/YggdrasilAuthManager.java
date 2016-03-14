package ru.linachan.yggdrasil.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.auth.backend.LocalAuthBackend;

public class YggdrasilAuthManager {

    private YggdrasilCore core;
    private Class<? extends YggdrasilAuthBackend> authBackend;

    private static Logger logger = LoggerFactory.getLogger(YggdrasilAuthManager.class);

    public YggdrasilAuthManager(YggdrasilCore yggdrasilCore) {
        core = yggdrasilCore;

        setAuthBackend();
    }

    private void setAuthBackend() {
        String backendClass = core.getConfig().getString(
            "yggdrasil.auth.backend", "ru.linachan.yggdrasil.auth.backend.LocalAuthBackend"
        );

        YggdrasilAuthBackendManager backendManager = core.getManager(YggdrasilAuthBackendManager.class);

        backendManager.list().keySet().stream()
            .filter(authBackendClass -> authBackendClass.getCanonicalName().equals(backendClass))
            .forEach(authBackendClass -> {
                backendManager.enable(authBackendClass);
                authBackend = authBackendClass;
            });

        if (authBackend == null) {
            backendManager.enable(LocalAuthBackend.class);
            authBackend = LocalAuthBackend.class;
        }

        logger.info("Backend configured: {}", authBackend.getSimpleName());
    }

    public YggdrasilAuthUser registerUser(String userName) {
        return core.getManager(YggdrasilAuthBackendManager.class).get(authBackend).registerUser(userName);
    }

    public YggdrasilAuthUser getUser(String userName) {
        return core.getManager(YggdrasilAuthBackendManager.class).get(authBackend).getUser(userName);
    }

    public boolean updateUser(YggdrasilAuthUser user) {
        return core.getManager(YggdrasilAuthBackendManager.class).get(authBackend).updateUser(user);
    }
}
