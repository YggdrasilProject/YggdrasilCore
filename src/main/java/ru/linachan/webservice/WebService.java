package ru.linachan.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.tcpserver.TCPService;
import ru.linachan.webservice.utils.RouteNotFound;
import ru.linachan.webservice.utils.StaticDirectoryRoute;
import ru.linachan.webservice.utils.StaticFileRoute;
import ru.linachan.webservice.utils.StaticResourceRoute;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.common.Entry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WebService implements TCPService {

    protected YggdrasilCore core;
    protected List<Entry<Pattern, Class<? extends WebServiceRoute>>> routes = new ArrayList<>();

    protected List<Entry<Pattern, File>> staticRoutes = new ArrayList<>();
    protected List<Entry<Pattern, URL>> resourceRoutes = new ArrayList<>();

    private static Logger logger = LoggerFactory.getLogger(WebService.class);

    public WebService() {
        addStaticResourceRoute("^/favicon.ico$", "/favicon.ico");
        addStaticResourceRoute("^/default_logo.png$", "/logo.png");
    }

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

        for (Entry<Pattern, URL> resourceRoute : resourceRoutes) {
            if (resourceRoute.getKey().matcher(uri).matches()) {
                StaticResourceRoute route = new StaticResourceRoute();
                route.setPattern(resourceRoute.getKey());
                route.setResource(resourceRoute.getValue());
                return route;
            }
        }

        for (Entry<Pattern, File> staticRoute : staticRoutes) {
            if (staticRoute.getKey().matcher(uri).matches()) {
                if (staticRoute.getValue().isDirectory()) {
                    StaticDirectoryRoute route = new StaticDirectoryRoute();
                    route.setPattern(staticRoute.getKey());
                    route.setDirectory(staticRoute.getValue());
                    return route;
                } else if(staticRoute.getValue().isFile()) {
                    StaticFileRoute route = new StaticFileRoute();
                    route.setPattern(staticRoute.getKey());
                    route.setFile(staticRoute.getValue());
                    return route;
                }
            }
        }

        return new RouteNotFound();
    }

    public void addRoute(String uriRegEx, Class<? extends WebServiceRoute> route) {
        routes.add(new Entry<>(Pattern.compile(uriRegEx), route));
    }

    public void addStaticRoute(String uriRegEx, File staticFile) {
        staticRoutes.add(new Entry<>(Pattern.compile(uriRegEx), staticFile));
    }

    public void addStaticResourceRoute(String uriRegEx, String resourcePath) {
        resourceRoutes.add(new Entry<>(Pattern.compile(uriRegEx), getClass().getResource(resourcePath)));
    }
}
