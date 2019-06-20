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
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmbee.mms.client.ApiClient;
import org.openmbee.mms.client.api.ElementApi;
import org.openmbee.mms.client.model.Element;
import org.openmbee.mms.client.model.Elements;
import org.openmbee.mms.client.model.RejectableElements;

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

        Map<String, Object> response = new HashMap<>();
        try {
            ApiClient client = Utils.createClient(ticket, auth, url);
            ElementApi apiInstance = new ElementApi();
            apiInstance.setApiClient(client);
            Element pe = Utils.getElement(apiInstance, projectId, refId, presentationId, null);
            PresentationElement responsePE = Utils.buildResponsePe(pe);
            response.put("element", responsePE);
            response.put("status", "ok");
        } catch (Exception e) {
            logger.error("Failed: ", e);
            return HttpResponse.badRequest();
        }
        return HttpResponse.ok(response);
    }


    @Post("/presentations/{presentationId}")
    public HttpResponse<?> postPresentationElement(@Body Presentation request,
        @Header("Authorization") Optional<String> auth,
        @QueryValue("alf_ticket") Optional<String> ticket,
        String projectId, String refId, String presentationId) {

        try {
            ApiClient client = Utils.createClient(ticket, auth, url);
            ElementApi apiInstance = new ElementApi();
            apiInstance.setApiClient(client);

            PresentationElement pe = request.getElements().get(0);
            Elements post = new Elements();
            Element e = new Element();
            e.put("id", presentationId);
            if (pe.getName() != null) {
                e.put("name", pe.getName());
            }
            if (pe.getContent() != null) {
                e.put("documentation", pe.getContent());
            }
            if (pe.getType() != null) {
                Map<String, Object> valueSpec = new HashMap<>();
                if ("Section".equals(pe.getType())) {
                    valueSpec.put("type", "Expression");
                    valueSpec.put("operand", new ArrayList());
                } else {
                    valueSpec.put("type", "LiteralString");
                    valueSpec.put("value", Utils.createSpecForPe(pe, presentationId));
                }
                e.put("specification", valueSpec);
            }
            post.addElementsItem(e) ;
            //    updates Presentation Element with new model
            RejectableElements re = apiInstance.postElements(projectId, refId, post);
        } catch (Exception e) {
            logger.error("Failed: ", e);
            return HttpResponse.badRequest();
        }
        return HttpResponse.ok(request);
    }

}
