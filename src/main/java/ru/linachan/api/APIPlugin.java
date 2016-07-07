package ru.linachan.api;

import com.sun.net.httpserver.*;
import ru.linachan.yggdrasil.plugin.YggdrasilPlugin;
import ru.linachan.yggdrasil.plugin.helpers.Plugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

@Plugin(name = "API", description = "Provides RESTfull API.")
public class APIPlugin extends YggdrasilPlugin implements HttpHandler {

    private HttpServer apiServer;

    @Override
    protected void onInit() {
        String apiHost = core.getConfig().getString("yggdrasil.api.host", "127.0.0.1");
        Integer apiPort = core.getConfig().getInt("yggdrasil.api.port", 41596);

        try {
            apiServer = HttpServer.create(new InetSocketAddress(apiHost, apiPort), 0);

            HttpContext apiContext = apiServer.createContext("/");

            apiContext.setHandler(this);
            apiContext.setAuthenticator(new APIAuthenticator(core));

            apiServer.start();
        } catch (IOException e) {
            logger.error("Unable to start APIServer: {}", e.getMessage());
        }
    }

    @Override
    protected void onShutdown() {
        if (apiServer != null) {
            apiServer.stop(0);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        StringBuilder builder = new StringBuilder();

        builder.append("<h1>URI: ").append(exchange.getRequestURI()).append("</h1>");

        Headers headers = exchange.getRequestHeaders();
        for (String header : headers.keySet()) {
            builder.append("<p>").append(header).append("=")
                    .append(headers.getFirst(header)).append("</p>");
        }

        byte[] bytes = builder.toString().getBytes();
        exchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
