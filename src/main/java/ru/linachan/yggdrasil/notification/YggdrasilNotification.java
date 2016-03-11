package ru.linachan.yggdrasil.notification;

public class YggdrasilNotification {

    private String header;
    private String source;
    private String message;

    public YggdrasilNotification(String src, String hdr, String msg) {
        header = hdr;
        source = src;
        message = msg;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String hdr) {
        header = hdr;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String src) {
        source = src;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        message = msg;
    }
}
