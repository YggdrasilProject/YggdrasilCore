package ru.linachan.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.tcpserver.TCPService;
import ru.linachan.webservice.utils.RouteNotFound;
import ru.linachan.yggdrasil.YggdrasilCore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class WebService implements TCPService {

    protected YggdrasilCore core;
    protected Map<Pattern, Class<? extends WebServiceRoute>> routes = new HashMap<>();

    private static Logger logger = LoggerFactory.getLogger(WebService.class);

    @Override
    public void handleConnection(YggdrasilCore core, InputStream in, OutputStream out) {
        try {
            WebServiceRequest request = WebServiceRequest.readFromSocket(in);
            WebServiceResponse response = (request != null) ? handleRequest(request) : null;
            WebServiceResponse.writeToSocket(response, out);
        } catch (IOException e) {
            logger.error("Unable to process client request", e);
        }
    }

    private WebServiceResponse handleRequest(WebServiceRequest request) {
        WebServiceRoute route = route(request.getUri());
        route.setUp(core);
        return route.handle(request);
    }

    public WebServiceRoute route(String uri) {
        try {
            for (Pattern pattern : routes.keySet()) {
                if (pattern.matcher(uri).matches()) {
                    WebServiceRoute route =  routes.get(pattern).newInstance();
                    route.setPattern(pattern);
                    return route;
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Unable to instantiate router", e);
        }

        return new RouteNotFound();
    }

    public void addRoute(String uriRegEx, Class<? extends WebServiceRoute> route) {
        routes.put(Pattern.compile(uriRegEx), route);
    }
}
