package ru.linachan.webservice.utils;

import ru.linachan.webservice.WebServiceHTTPCode;
import ru.linachan.webservice.WebServiceRequest;
import ru.linachan.webservice.WebServiceResponse;
import ru.linachan.webservice.WebServiceRoute;

public class RouteNotFound extends WebServiceRoute {

    public WebServiceResponse HEAD(WebServiceRequest request) {
        return new WebServiceResponse(WebServiceHTTPCode.NOT_FOUND);
    }

    public WebServiceResponse OPTIONS(WebServiceRequest request) {
        return new WebServiceResponse(WebServiceHTTPCode.NOT_FOUND);
    }

    public WebServiceResponse GET(WebServiceRequest request) {
        return new WebServiceResponse(WebServiceHTTPCode.NOT_FOUND);
    }

    public WebServiceResponse POST(WebServiceRequest request) {
        return new WebServiceResponse(WebServiceHTTPCode.NOT_FOUND);
    }

    public WebServiceResponse PUT(WebServiceRequest request) {
        return new WebServiceResponse(WebServiceHTTPCode.NOT_FOUND);
    }

    public WebServiceResponse PATCH(WebServiceRequest request) {
        return new WebServiceResponse(WebServiceHTTPCode.NOT_FOUND);
    }

    public WebServiceResponse DELETE(WebServiceRequest request) {
        return new WebServiceResponse(WebServiceHTTPCode.NOT_FOUND);
    }
}
