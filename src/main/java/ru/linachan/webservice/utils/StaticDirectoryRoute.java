package ru.linachan.webservice.utils;

import ru.linachan.webservice.WebServiceRequest;
import ru.linachan.webservice.WebServiceResponse;
import ru.linachan.webservice.WebServiceRoute;

import java.io.File;

public class StaticDirectoryRoute extends WebServiceRoute {

    private File staticDirectory;

    public void setDirectory(File directory) {
        staticDirectory = directory;
    }

    public WebServiceResponse GET(WebServiceRequest request) {
        return null;
    }
}
