package ru.linachan.webservice.utils;

import ru.linachan.webservice.WebServiceHTTPCode;
import ru.linachan.webservice.WebServiceRequest;
import ru.linachan.webservice.WebServiceResponse;
import ru.linachan.webservice.WebServiceRoute;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class StaticResourceRoute extends WebServiceRoute {

    private URL resource;

    public WebServiceResponse GET(WebServiceRequest request) {
        try {

            String contentType = ContentType.guessContentType(resource.getFile());
            WebServiceResponse response = new WebServiceResponse(WebServiceHTTPCode.OK);

            response.setContentType(contentType);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream inputStream = resource.openStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            response.binaryResponse(buffer.toByteArray());

            return response;
        } catch (IOException e) {
            return renderException(e);
        }
    }


    public void setResource(URL resource) {
        this.resource = resource;
    }
}
