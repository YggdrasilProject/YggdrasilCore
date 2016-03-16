package ru.linachan.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.tcpserver.TCPService;
import ru.linachan.webservice.utils.RouteNotFound;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.Entry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WebService implements TCPService {

    protected YggdrasilCore core;
    protected List<Entry<Pattern, Class<? extends WebServiceRoute>>> routes = new ArrayList<>();

    private static Logger logger = LoggerFactory.getLogger(WebService.class);

    @Override
    public void handleConnection(YggdrasilCore core, InputStream in, OutputStream out, InetAddress clientAddress) {
        this.core = core;

        try {
            WebServiceRequest request = WebServiceRequest.readFromSocket(in, clientAddress);
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
            for (Entry<Pattern, Class<? extends WebServiceRoute>> routeData : routes) {
                if (routeData.getKey().matcher(uri).matches()) {
                    WebServiceRoute route = routeData.getValue().newInstance();
                    route.setPattern(routeData.getKey());
                    return route;
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Unable to instantiate router", e);
        }

        return new RouteNotFound();
    }

    public void addRoute(String uriRegEx, Class<? extends WebServiceRoute> route) {
        routes.add(new Entry<>(Pattern.compile(uriRegEx), route));
    }
}
