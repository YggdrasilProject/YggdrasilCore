package ru.linachan.webservice;

public enum WebServiceHTTPCode {
    OK ("200 OK"),
    CREATED ("201 Created"),
    BAD_REQUEST ("400 Bad Request"),
    UNAUTHORIZED ("401 Unauthorized"),
    FORBIDDEN ("403 Forbidden"),
    NOT_FOUND ("404 Not Found"),
    METHOD_NOT_ALLOWED ("405 Method Not Allowed"),
    CONFLICT ("409 Conflict"),
    UNPROCESSABLE ("422 Unprocessable"),
    SERVER_ERROR ("500 Server Error");

    private String code;

    WebServiceHTTPCode(String codeString) {
        code = codeString;
    }

    public String getCode() {
        return code;
    }
}
