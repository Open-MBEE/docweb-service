package org.openmbee.docweb.exceptions;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmbee.mms.client.ApiException;

@Controller //seems error only works if declared in a controller?
public class ErrorHandler {

    private static Logger logger = LogManager.getLogger(ErrorHandler.class);

    @Error(global = true)
    public HttpResponse error(HttpRequest request, ApiException e) {
        Map json = new HashMap();
        int code = e.getCode();
        logger.info(code);
        if (code == 400 || code == 500) {
            logger.info(e.getResponseBody());
        }
        json.put("code", code);
        json.put("message", e.getResponseBody());
        switch (e.getCode()) {
            case 400:
                return HttpResponse.badRequest(json);
            case 401:
                return HttpResponse.unauthorized().body(json);
            case 403:
                return HttpResponse.status(HttpStatus.FORBIDDEN).body(json);
            case 404:
                return HttpResponse.notFound(json);
            case 500:
                return HttpResponse.serverError(json);
        }
        return HttpResponse.serverError();
    }
}
