package ru.linachan.webservice;

import org.apache.velocity.VelocityContext;
import ru.linachan.webservice.utils.AuthRequired;
import ru.linachan.yggdrasil.YggdrasilCore;
import ru.linachan.yggdrasil.auth.YggdrasilAuthUser;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
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
        WebServiceResponse response;
        String requestMethod = requestObject.getMethod().equals("HEAD") ? "GET" : requestObject.getMethod();
        request = requestObject;

        try {
            Method handler = getClass().getDeclaredMethod(requestMethod, WebServiceRequest.class);

            boolean authRequired = false;
            boolean authenticated = false;

            String authRealm = null;

            if (handler.isAnnotationPresent(AuthRequired.class)) {
                String authHeader = requestObject.getHeader("Authorization");
                authRequired = true;

                authRealm = handler.getAnnotation(AuthRequired.class).value();

                if (authHeader != null) {
                    String[] authHeaderInfo = authHeader.split(" ");
                    if ((authHeaderInfo.length > 1)&&(authHeaderInfo[0].equals("Basic"))) {
                        String basicAuthString = new String(Base64.getDecoder().decode(authHeaderInfo[1]));
                        if (basicAuthString.contains(":")) {
                            String[] basicAuthCredentials = basicAuthString.split(":");
                            YggdrasilAuthUser authUser = core.getAuthManager().getUser(basicAuthCredentials[0]);
                            if (authUser != null) {
                                authenticated = authUser.getAttribute("passWord").equals(basicAuthCredentials[1]);
                            }
                        }
                    }
                }
            }

            if (!authRequired||authenticated) {
                response = (WebServiceResponse) handler.invoke(this, requestObject);
            } else {
                response = new WebServiceResponse(WebServiceHTTPCode.UNAUTHORIZED);
            }

            response = (response != null) ? response : new WebServiceResponse(WebServiceHTTPCode.METHOD_NOT_ALLOWED);

            if (authRequired) {
                response.setHeader("WWW-Authenticate", String.format("Basic realm=\"%s\"", authRealm));
            }

            if (request.getMethod().equals("HEAD")) {
                response.headersOnly(true);
            }

        } catch (NoSuchMethodException e) {
            response = new WebServiceResponse(WebServiceHTTPCode.METHOD_NOT_ALLOWED);
        } catch (InvocationTargetException | IllegalAccessException e) {
            response = renderException(e);
        }

        return response;
    }

    public WebServiceResponse renderException(Throwable exception) {
        WebServiceResponse response = new WebServiceResponse(WebServiceHTTPCode.SERVER_ERROR);

        VelocityContext context = new VelocityContext();
        context.put("trace", exception.getStackTrace());
        context.put("exception", exception.getClass().getSimpleName());
        context.put("message", (exception.getMessage() != null) ? exception.getMessage() : "null");

        response.renderTemplate("templates/500.vm", context);

        return response;
    }
}
