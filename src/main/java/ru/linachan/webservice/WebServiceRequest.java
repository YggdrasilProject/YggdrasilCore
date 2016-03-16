package ru.linachan.webservice;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.linachan.webservice.utils.InputReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServiceRequest {

    private static final Pattern HTTP_STARTING_LINE = Pattern.compile(
        "^(?<method>OPTIONS|HEAD|GET|POST|PUT|PATCH|DELETE)\\s(?<uri>[^\\s]+)\\sHTTP/(?<version>[0-9\\.]+)$"
    );

    private static final Pattern HTTP_REQUEST_HEADER = Pattern.compile(
        "^(?<header>[^:]+):\\s*(?<value>.*)$"
    );

    private static final Pattern HTTP_COOKIE = Pattern.compile(
        "^Cookie:\\s*(?<cookie>[^=]+)=(?<value>[^;]+)(;\\s*(?<params>.*))?$"
    );

    private final String method;
    private final String version;
    private final String uri;
    private final InetAddress remoteAddress;

    private Map<String, String> headers;
    private Map<String, String> cookies;

    private byte[] rawData;

    private static Logger logger = LoggerFactory.getLogger(WebServiceRequest.class);

    public static WebServiceRequest readFromSocket(InputStream in, InetAddress clientAddress) throws IOException {
        InputReader requestReader = new InputReader(in);

        String startingString = requestReader.readLine();
        if (startingString != null) {
            Matcher startingLine = HTTP_STARTING_LINE.matcher(startingString);
            if (startingLine.matches()) {
                WebServiceRequest request = new WebServiceRequest(
                    startingLine.group("method"),
                    startingLine.group("uri"),
                    startingLine.group("version"),
                    clientAddress
                );

                while (true) {
                    String raw_header = requestReader.readLine();
                    if (raw_header == null || raw_header.trim().length() == 0) {
                        break;
                    } else {
                        Matcher headerLine = HTTP_REQUEST_HEADER.matcher(raw_header);
                        Matcher cookieLine = HTTP_COOKIE.matcher(raw_header);
                        if (cookieLine.matches()) {
                            request.setCookie(
                                cookieLine.group("cookie"),
                                cookieLine.group("value")
                            );
                        } else if (headerLine.matches()) {
                            request.setHeader(
                                headerLine.group("header"),
                                headerLine.group("value")
                            );
                        }
                    }
                }

                if (request.getHeader("Content-Length") != null) {
                    byte[] rawData = requestReader.read(Integer.parseInt(request.getHeader("Content-Length")));

                    request.setRawData(rawData);
                }

                return request;
            }
        }

        return null;
    }

    private WebServiceRequest(String httpMethod, String requestUri, String httpVersion, InetAddress clientAddress) {
        method = httpMethod;
        uri = requestUri;
        version = httpVersion;
        remoteAddress = clientAddress;

        headers = new HashMap<>();
        cookies = new HashMap<>();
    }

    private void setHeader(String header, String value) {
        headers.put(header.toLowerCase(), value);
    }

    private void setCookie(String cookie, String value) {
        cookies.put(cookie, value);
    }

    private void setRawData(byte[] rawDataBytes) {
        rawData = rawDataBytes;
    }

    public String getMethod() {
        return method;
    }

    public String getVersion() {
        return version;
    }

    public String getFullUri() {
        return uri;
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    public String getUri() {
        if (uri.contains("?")) {
            return uri.split("\\?")[0];
        }
        return uri;
    }

    public String getHeader(String header) {
        return headers.containsKey(header.toLowerCase()) ? headers.get(header.toLowerCase()) : null;
    }

    public String getCookie(String cookie) {
        return cookies.containsKey(cookie) ? cookies.get(cookie) : null;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public JSONObject getJSONData() throws ParseException {
        JSONParser parser = new JSONParser();
        if (rawData != null) {
            return (JSONObject) parser.parse(new String(rawData));
        }
        return null;
    }

    public Map<String, String> getURIParams() throws UnsupportedEncodingException {
        Map<String, String> uriParams = new HashMap<>();

        if (uri.contains("?")) {
            String rawUriParams = URLDecoder.decode(uri.split("\\?")[1], "UTF-8");
            uriParams = parseQueryString(rawUriParams);
        }

        return uriParams;
    }

    public Map<String, String> getRequestParams() throws UnsupportedEncodingException {
        Map<String, String> requestParams = new HashMap<>();

        if (method.equals("POST")||method.equals("PUT")||method.equals("PATCH")) {
            String rawRequestParams = URLDecoder.decode(new String(rawData), "UTF-8");
            requestParams = parseQueryString(rawRequestParams);
        }

        return requestParams;
    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> queryStringParams = new HashMap<>();

        for (String rawQueryParam : queryString.split("&")) {
            if (rawQueryParam.contains("=")) {
                String[] requestParam = rawQueryParam.split("=");
                queryStringParams.put(requestParam[0], requestParam[1]);
            } else {
                queryStringParams.put(rawQueryParam, null);
            }
        }

        return queryStringParams;
    }

    public String toString() {
        return String.format("%s %s HTTP/%s", method, uri, version);
    }
}
