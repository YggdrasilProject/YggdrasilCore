package ru.linachan.webservice.utils;

import ru.linachan.webservice.WebServiceRequest;
import ru.linachan.webservice.WebServiceResponse;
import ru.linachan.webservice.WebServiceRoute;

public class RouteNotFound extends WebServiceRoute {

    @Override
    protected WebServiceResponse HEAD(WebServiceRequest request) {
        return null;
    }

    @Override
    protected WebServiceResponse OPTIONS(WebServiceRequest request) {
        return null;
    }

    @Override
    protected WebServiceResponse GET(WebServiceRequest request) {
        return null;
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
