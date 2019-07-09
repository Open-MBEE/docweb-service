package org.openmbee.docweb.controllers;

import org.openmbee.docweb.domains.Presentation;
import org.openmbee.docweb.domains.PresentationElement;
import org.openmbee.docweb.services.Utils;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmbee.mms.client.ApiClient;
import org.openmbee.mms.client.ApiException;
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
            @QueryValue("alf_ticket") Optional<String> ticket,
            String projectId, String refId, String presentationId) throws ApiException {

        Map<String, Object> response = new HashMap<>();
        ApiClient client = Utils.createClient(ticket, auth, url);
        ElementApi apiInstance = new ElementApi();
        apiInstance.setApiClient(client);
        Element pe = Utils.getElement(apiInstance, projectId, refId, presentationId, null);
        List<PresentationElement> responsePE = new ArrayList<>();
        responsePE.add(Utils.buildResponsePe(pe));
        response.put("elements", responsePE);

        return HttpResponse.ok(response);
    }


    @Post("/presentations/{presentationId}")
    public HttpResponse<?> postPresentationElement(@Body Presentation request,
            @Header("Authorization") Optional<String> auth,
            @QueryValue("alf_ticket") Optional<String> ticket,
            String projectId, String refId, String presentationId) throws ApiException {

        ApiClient client = Utils.createClient(ticket, auth, url);
        ElementApi apiInstance = new ElementApi();
        apiInstance.setApiClient(client);

        //    updates Presentation Element with new model
        PresentationElement pe = request.getElements().get(0);
        Elements post = new Elements();
        pe.setId(presentationId);
        Element e = Utils.createInstanceFromPe(pe, projectId);
        if (pe.getName() == null) {
            e.remove("name");
        }
        if (pe.getContent() == null) {
            e.remove("documentation");
        }
        if (pe.getType() == null) {
            e.remove("specification");
            e.remove("classifierIds");
        }
        post.addElementsItem(e);
        RejectableElements re = apiInstance.postElements(projectId, refId, post);

        return HttpResponse.ok(request);
    }
}
