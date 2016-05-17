package ru.linachan.webservice.utils;

import ru.linachan.webservice.WebServiceHTTPCode;
import ru.linachan.webservice.WebServiceRequest;
import ru.linachan.webservice.WebServiceResponse;
import ru.linachan.webservice.WebServiceRoute;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class StaticFileRoute extends WebServiceRoute {

    private File staticFile;

    public void setFile(File file) {
        staticFile = file;
    }

    public WebServiceResponse GET(WebServiceRequest request) {
        try {
            String contentType = Files.probeContentType(staticFile.toPath());
            WebServiceResponse response = new WebServiceResponse(WebServiceHTTPCode.OK);

            response.setContentType(contentType);
            response.binaryResponse(Files.readAllBytes(staticFile.toPath()));

            return response;
        } catch (IOException e) {
            return renderException(e);
        }
    }
}
