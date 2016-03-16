package ru.linachan.webservice;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class WebServiceResponse {

    private WebServiceHTTPCode httpCode;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();
    private byte[] body = null;

    private boolean headersOnly = false;

    public WebServiceResponse(WebServiceHTTPCode statusCode) {
        httpCode = statusCode;

        headers.put("Connection", "close");
    }

    public static void writeToSocket(WebServiceResponse response, OutputStream out) throws IOException {
        if (response == null) {
            response = new WebServiceResponse(WebServiceHTTPCode.BAD_REQUEST);
        }

        BufferedWriter responseWriter = new BufferedWriter(new OutputStreamWriter(out));

        responseWriter.write(String.format("HTTP/1.0 %s\r\n", response.getCode())); responseWriter.flush();

        for (Map.Entry<String, String> header: response.getHeaders().entrySet()) {
            responseWriter.write(String.format("%s: %s\r\n", header.getKey(), header.getValue()));
            responseWriter.flush();
        }

        if ((response.getBody() != null)&&(response.getBody().length > 0)) {
            responseWriter.write(String.format("Content-Length: %s\r\n", response.getBody().length));
            responseWriter.flush();
        }

        for (Map.Entry<String, String> cookie: response.getCookies().entrySet()) {
            responseWriter.write(String.format("Set-Cookie: %s=%s\r\n", cookie.getKey(), cookie.getValue()));
            responseWriter.flush();
        }

        responseWriter.newLine(); responseWriter.flush();

        if (!response.headersOnly()) {
            if ((response.getBody() != null) && (response.getBody().length > 0)) {
                out.write(response.getBody());
                out.flush();
            }
        }

        out.flush();
        out.close();
    }

    private boolean headersOnly() {
        return headersOnly;
    }

    public void headersOnly(boolean headersOnly) {
        this.headersOnly = headersOnly;
    }

    private String getCode() {
        return httpCode.getCode();
    }

    private Map<String, String> getHeaders() {
        return headers;
    }

    private Map<String, String> getCookies() {
        return cookies;
    }

    private byte[] getBody() {
        return body;
    }

    public void binaryResponse(byte[] binaryData) {
        body = binaryData;
    }

    public void htmlResponse(String htmlData) {
        setContentType("text/html");
        body = htmlData != null ? htmlData.getBytes() : new byte[0];
    }

    public void jsonResponse(JSONObject jsonData) {
        setContentType("application/json");
        JSONObject jsonObject = (jsonData != null) ? jsonData : new JSONObject();
        body = jsonObject.toJSONString().getBytes();
    }

    public void renderTemplate(String templateName, VelocityContext context) {
        VelocityEngine templateRenderer = new VelocityEngine();
        templateRenderer.init();

        Template template = templateRenderer.getTemplate(templateName);

        StringWriter renderResult = new StringWriter();
        template.merge(context, renderResult);

        setContentType("text/html");
        body = renderResult.toString().getBytes();
    }

    public void setHeader(String header, String value) {
        headers.put(header, value);
    }

    public void setCookie(String cookie, String value) {
        cookies.put(cookie, value);
    }

    public void setContentType(String contentType) {
        headers.put("Content-Type", contentType);
    }
}
