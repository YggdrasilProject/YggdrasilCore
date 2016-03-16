package ru.linachan.webservice;

import ru.linachan.yggdrasil.YggdrasilCore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class WebServiceRoute {

    protected YggdrasilCore core;
    private Pattern uriPattern;
    private WebServiceRequest request;

    public void setPattern(Pattern pattern) {
        uriPattern = pattern;
    }

    public void setUp(YggdrasilCore yggdrasilCore) {
        core = yggdrasilCore;
    }

    protected String getArg(String groupName) {
        Matcher matcher = uriPattern.matcher(request.getUri());
        if (matcher.matches()) {
            try {
                return matcher.group(groupName);
            } catch (IllegalArgumentException | IllegalStateException e) {
                return null;
            }
        }

        return null;
    }

    public WebServiceResponse handle(WebServiceRequest requestObject) {
        WebServiceResponse response = null;
        request = requestObject;

        switch (requestObject.getMethod()) {
            case "OPTIONS":
                response = OPTIONS(requestObject);
                break;
            case "HEAD":
                response = HEAD(requestObject);
                break;
            case "GET":
                response = GET(requestObject);
                break;
            case "POST":
                response = POST(requestObject);
                break;
            case "PUT":
                response = PUT(requestObject);
                break;
            case "PATCH":
                response = PATCH(requestObject);
                break;
            case "DELETE":
                response = DELETE(requestObject);
                break;
        }

        return (response != null) ? response : new WebServiceResponse(WebServiceHTTPCode.METHOD_NOT_ALLOWED);
    }

    protected WebServiceResponse HEAD(WebServiceRequest request) {
        WebServiceResponse response = GET(request);
        response.headersOnly(true);
        return response;
    }

    protected abstract WebServiceResponse OPTIONS(WebServiceRequest request);

    protected abstract WebServiceResponse GET(WebServiceRequest request);

    protected abstract WebServiceResponse POST(WebServiceRequest request);

    protected abstract WebServiceResponse PUT(WebServiceRequest request);

    protected abstract WebServiceResponse PATCH(WebServiceRequest request);

    protected abstract WebServiceResponse DELETE(WebServiceRequest request);

}
