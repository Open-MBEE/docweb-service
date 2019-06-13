package gov.nasa.jpl.mbee.controllers;

import com.google.gson.internal.LinkedTreeMap;
import gov.nasa.jpl.mbee.domains.Presentation;
import gov.nasa.jpl.mbee.domains.PresentationElement;
import gov.nasa.jpl.mbee.services.Utils;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.Put;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmbee.mms.client.ApiClient;
import org.openmbee.mms.client.api.ElementApi;
import org.openmbee.mms.client.model.Element;

/**
 * @author shakeh
 */
@Controller("/projects/{projectId}/refs/{refId}/views/{viewId}")
public class ViewElementsController {

    @Value("${mms.url}")
    String url;

    private static Logger logger = LogManager.getLogger(PresentationController.class);


    @Get("/presentations")
    public MutableHttpResponse<Object> getPresentationElements(@Header("Authorization")
        Optional<String> auth, String projectId, String refId, String viewId) {

//    Get all presentation elements within view
        ApiClient client;
        ElementApi apiInstance;
        Map<String, Object> response = new HashMap<>();

        try {
            client = Utils.createClient(null, auth, url);
            apiInstance = new ElementApi();
            apiInstance.setApiClient(client);
            Element view = Utils.getElement(apiInstance, projectId, refId, viewId, null);

//    TODO make util method -  Get child pe ids
            Set<String> peIds = new HashSet<String>();
            LinkedTreeMap contents = (LinkedTreeMap) view.get("_contents");
            ArrayList<Map> pes = (ArrayList) contents.get("operand");
            for (Map pe: pes) {
                peIds.add((String) pe.get("instanceId"));
            }
            List<Element> pedata = Utils.getElements(apiInstance, projectId, refId, peIds, null);

            List<PresentationElement> responseList = new ArrayList<>();
            PresentationElement responsePE;
            for (Element element: pedata) {
                responsePE = Utils.buildResponsePe(element);
                responseList.add(responsePE);
            }
//            response.put("view", view);
            response.put("elements", responseList);
        } catch (Exception e) {
            logger.error("Failed: ", e);
            return HttpResponse.badRequest();
        }
        response.put("status", "ok");
        return HttpResponse.ok(response);
    }


    //  TODO need /index?
    @Put("/presentations/{presentationId}")
    public HttpResponse<?> putPresentationElement(@Body Presentation request,
        @Header("Authorization") Optional<String> auth,
        String projectId, String refId, String presentationId) {

        ApiClient client;
        ElementApi apiInstance;
        Map<String, Object> response = new HashMap<>();

        try {
            client = Utils.createClient(null, auth, url);
            apiInstance = new ElementApi();
            apiInstance.setApiClient(client);

            //   put presentation element in view with id "xxx" at end or index?
        } catch (Exception e) {
            logger.error("Failed: ", e);
            return HttpResponse.badRequest();
        }
        response.put("status", "ok");
        return HttpResponse.ok(response);
    }

    @Patch("/presentations/{presentationId}")
    public HttpResponse<?> patchPresentationElement(@Body Presentation request,
        @Header("Authorization") Optional<String> auth,
        String projectId, String refId, String presentationId) {

        ApiClient client;
        ElementApi apiInstance;
        Map<String, Object> response = new HashMap<>();

        try {
            client = Utils.createClient(null, auth, url);
            apiInstance = new ElementApi();
            apiInstance.setApiClient(client);

            //    updates index ofPresentation Element in view
        } catch (Exception e) {
            logger.error("Failed: ", e);
            return HttpResponse.badRequest();
        }
        response.put("status", "ok");
        return HttpResponse.ok(response);
    }

}