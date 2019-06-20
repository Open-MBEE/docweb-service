package gov.nasa.jpl.mbee.controllers;

import gov.nasa.jpl.mbee.domains.Presentation;
import gov.nasa.jpl.mbee.domains.PresentationElement;
import gov.nasa.jpl.mbee.services.Utils;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmbee.mms.client.ApiClient;
import org.openmbee.mms.client.api.ElementApi;
import org.openmbee.mms.client.model.Element;

/**
 * @author shakeh
 */
@Controller("/projects/{projectId}/refs/{refId}")
public class PresentationController {

    @Value("${mms.url}")
    String url;

    private static Logger logger = LogManager.getLogger(PresentationController.class);


    @Get("/presentation/{presentationId}")
    public HttpResponse<?> getPresentationElement(@Header("Authorization") Optional<String> auth,
        String projectId, String refId, String presentationId,
            @QueryValue("alf_ticket") Optional<String> ticket) {

        ApiClient client;
        ElementApi apiInstance;
        Map<String, Object> response = new HashMap<>();

        try {
            client = Utils.createClient(ticket, auth, url);
            apiInstance = new ElementApi();
            apiInstance.setApiClient(client);
            Element pe = Utils.getElement(apiInstance, projectId, refId, presentationId, null);
            PresentationElement responsePE = Utils.buildResponsePe(pe);
            response.put("element", responsePE);
        } catch (Exception e) {
            logger.error("Failed: ", e);
            return HttpResponse.badRequest();
        }
        response.put("status", "ok");
        return HttpResponse.ok(response);
    }


    @Post("/presentations/{presentationId}")
    public HttpResponse<?> postPresentationElement(@Body Presentation request,
        @Header("Authorization") Optional<String> auth,
        String projectId, String refId, String presentationId) {

        ApiClient client;
        ElementApi apiInstance;
        Map<String, Object> response = new HashMap<>();

        try {
            client = Utils.createClient(null, auth, url);
            apiInstance = new ElementApi();
            apiInstance.setApiClient(client);

            //    updates Presentation Element with new model
        } catch (Exception e) {
            logger.error("Failed: ", e);
            return HttpResponse.badRequest();
        }
        response.put("status", "ok");
        return HttpResponse.ok(response);
    }

}
