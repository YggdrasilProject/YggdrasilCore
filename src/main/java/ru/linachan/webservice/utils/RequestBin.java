package ru.linachan.webservice.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.webservice.WebServiceHTTPCode;
import ru.linachan.webservice.WebServiceRequest;
import ru.linachan.webservice.WebServiceResponse;
import ru.linachan.webservice.WebServiceRoute;
import ru.linachan.yggdrasil.queue.YggdrasilQueue;

public class RequestBin extends WebServiceRoute {

    private static Logger logger = LoggerFactory.getLogger(RequestBin.class);

    @Override
    protected WebServiceResponse OPTIONS(WebServiceRequest request) {
        logRequest(request);
        return new WebServiceResponse(WebServiceHTTPCode.OK);
    }

    @Override
    protected WebServiceResponse GET(WebServiceRequest request) {
        logRequest(request);
        return new WebServiceResponse(WebServiceHTTPCode.OK);
    }

    @Override
    protected WebServiceResponse POST(WebServiceRequest request) {
        logRequest(request);
        return new WebServiceResponse(WebServiceHTTPCode.OK);
    }

    @Override
    protected WebServiceResponse PUT(WebServiceRequest request) {
        logRequest(request);
        return new WebServiceResponse(WebServiceHTTPCode.OK);
    }

    @Override
    protected WebServiceResponse PATCH(WebServiceRequest request) {
        logRequest(request);
        return new WebServiceResponse(WebServiceHTTPCode.OK);
    }

    @Override
    protected WebServiceResponse DELETE(WebServiceRequest request) {
        logRequest(request);
        return new WebServiceResponse(WebServiceHTTPCode.OK);
    }

    @SuppressWarnings("unchecked")
    private void logRequest(WebServiceRequest request) {
        ((YggdrasilQueue<WebServiceRequest>) core.getQueue("requestBin")).push(request);
    }
}
