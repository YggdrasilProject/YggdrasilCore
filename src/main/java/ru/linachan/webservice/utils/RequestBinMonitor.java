package ru.linachan.webservice.utils;

import org.apache.velocity.VelocityContext;
import ru.linachan.webservice.WebServiceHTTPCode;
import ru.linachan.webservice.WebServiceRequest;
import ru.linachan.webservice.WebServiceResponse;
import ru.linachan.webservice.WebServiceRoute;
import ru.linachan.yggdrasil.queue.YggdrasilQueue;

import java.util.ArrayList;
import java.util.List;

public class RequestBinMonitor extends WebServiceRoute {

    @Override
    protected WebServiceResponse OPTIONS(WebServiceRequest request) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected WebServiceResponse GET(WebServiceRequest request) {
        WebServiceResponse response = new WebServiceResponse(WebServiceHTTPCode.OK);

        VelocityContext ctx = new VelocityContext();

        YggdrasilQueue<WebServiceRequest> requestQueue = (YggdrasilQueue<WebServiceRequest>) core.getQueue("requestBin");
        List<WebServiceRequest> requests = (requestQueue != null) ? requestQueue.list() : new ArrayList<>();

        ctx.internalPut("requests", requests);

        response.renderTemplate("/templates/requestBinMonitor.vm", ctx);

        return response;
    }

    @Override
    protected WebServiceResponse POST(WebServiceRequest request) {
        return null;
    }

    @Override
    protected WebServiceResponse PUT(WebServiceRequest request) {
        return null;
    }

    @Override
    protected WebServiceResponse PATCH(WebServiceRequest request) {
        return null;
    }

    @Override
    protected WebServiceResponse DELETE(WebServiceRequest request) {
        return null;
    }
}
