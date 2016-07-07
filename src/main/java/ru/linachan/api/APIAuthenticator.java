package ru.linachan.api;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import ru.linachan.yggdrasil.YggdrasilCore;

public class APIAuthenticator extends Authenticator {

    private YggdrasilCore core;

    public APIAuthenticator(YggdrasilCore yggdrasilCore) {
        core = yggdrasilCore;
    }

    @Override
    public Result authenticate(HttpExchange httpExchange) {
        return null;
    }
}
