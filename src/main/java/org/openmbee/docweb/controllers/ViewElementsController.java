package org.openmbee.docweb.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import java.util.Arrays;
import org.openmbee.docweb.domains.Presentations;
import org.openmbee.docweb.domains.PresentationElement;
import org.openmbee.docweb.services.Utils;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
@Controller("/projects/{projectId}/refs/{refId}/views/{viewId}")
public class ViewElementsController {

    @Value("${mms.url}")
    String url;

    private static Logger logger = LogManager.getLogger(PresentationController.class);

    @Get("/presentations")
    public Presentations getPresentationElements(
            @Parameter(hidden = true) @Header("Authorization") Optional<String> auth,
            @Parameter(hidden = true) @QueryValue("alf_ticket") Optional<String> ticket,
            String projectId, String refId, String viewId) throws ApiException {
        logger.info(url);
//    Get all presentation elements within view
        Presentations response = new Presentations();

        ApiClient client = Utils.createClient(ticket, auth, url);
        ElementApi apiInstance = new ElementApi();
        apiInstance.setApiClient(client);

        Element view = Utils.getElement(apiInstance, projectId, refId, viewId, null);
        List<String> peIds = new ArrayList<>();
        Map contents = (Map) view.get("_contents");
        if (contents.isEmpty()) {
            response.setElements(new ArrayList<>());
        } else {
            List<Map> pes = (List) contents.get("operand");
            for (Map pe : pes) {
                peIds.add((String) pe.get("instanceId"));
            }
            List<Element> peElts = Utils
                    .getElements(apiInstance, projectId, refId, peIds, null);
            PresentationElement[] peSortedList = new PresentationElement[peIds.size()];
            for (Element element : peElts) {
                PresentationElement responsePE = Utils.buildResponsePe(element);
                int index = peIds.indexOf(responsePE.getId());
                peSortedList[index] = responsePE;
            }
//            response.put("view", view);
            response.setElements(Arrays.asList(peSortedList));
        }
        return response;
    }

    @Put("/presentations")
    public Presentations insertPresentationElement(@Body Presentations request,
            @Parameter(hidden = true) @Header("Authorization") Optional<String> auth,
            @Parameter(hidden = true) @QueryValue("alf_ticket") Optional<String> ticket,
            @QueryValue("index") Optional<Integer> index,
            String projectId, String refId, String viewId) throws ApiException {

        ApiClient client = Utils.createClient(ticket, auth, url);
        ElementApi apiInstance = new ElementApi();
        apiInstance.setApiClient(client);

        Element view = Utils.getElement(apiInstance, projectId, refId, viewId, null);
        int i = 0;
        Elements post = new Elements();
        for (PresentationElement req : request.getElements()) {
            Element pe = Utils.createInstanceFromPe(req, projectId);
            post.addElementsItem(pe);
            Map<String, Object> operand = Utils.createOperandForInstance(pe);
            if (view.get("_contents") != null) {
                operand.put("ownerId", ((Map) view.get("_contents")).get("id"));
                List<Map> operands = (List<Map>) ((Map) view.get("_contents")).get("operand");
                if (index.isPresent() && index.get() < operands.size() && index.get() > -1) {
                    operands.add(index.get() + i, operand);
                } else {
                    operands.add(operand);
                }
            }
            req.setId((String) pe.get("id"));
            i++;
        }
        Element postView = new Element();
        postView.put("id", view.get("id"));
        postView.put("_contents", view.get("_contents"));
        post.addElementsItem(postView);
        RejectableElements re = apiInstance.postElements(projectId, refId, post);

        return request;
    }

    @Patch("/presentations/{presentationId}")
    public HttpResponse<?> updatePresentationElement(
            @Parameter(hidden = true) @Header("Authorization") Optional<String> auth,
            @Parameter(hidden = true) @QueryValue("alf_ticket") Optional<String> ticket,
            @QueryValue("index") Integer index,
            String projectId, String refId, String viewId, String presentationId)
            throws ApiException {

        Map<String, Object> response = new HashMap<>();
        ApiClient client = Utils.createClient(ticket, auth, url);
        ElementApi apiInstance = new ElementApi();
        apiInstance.setApiClient(client);

        Elements post = new Elements();
        Element view = Utils.getElement(apiInstance, projectId, refId, viewId, null);
        List<Map> operands = (List<Map>) ((Map) view.get("_contents")).get("operand");

        if (index < operands.size() && index > -1) {
            Map peToAdd = new HashMap();
            for (Iterator<Map> iterator = operands.iterator(); iterator.hasNext(); ) {
                Map operand = iterator.next();
                if (presentationId.equals(operand.get("instanceId"))
                        && operands.indexOf(operand) != index) {
                    peToAdd = operand;
                    iterator.remove();
                    break;
                }
            }
            if (!peToAdd.isEmpty()) {
                operands.add(index, peToAdd);
            }
            Element postView = new Element();
            postView.put("id", view.get("id"));
            postView.put("_contents", view.get("_contents"));
            post.addElementsItem(postView);
            RejectableElements re = apiInstance.postElements(projectId, refId, post);
        }
        return HttpResponse.ok(response);
    }

    @Delete("/presentations/{presentationId}")
    public HttpResponse<?> deletePresentationElementFromView(
            @Parameter(hidden = true) @Header("Authorization") Optional<String> auth,
            @Parameter(hidden = true) @QueryValue("alf_ticket") Optional<String> ticket,
            String projectId, String refId, String viewId, String presentationId)
            throws ApiException {

        Map<String, Object> response = new HashMap<>();
        ApiClient client = Utils.createClient(ticket, auth, url);
        ElementApi apiInstance = new ElementApi();
        apiInstance.setApiClient(client);

        Elements post = new Elements();
        Element view = Utils.getElement(apiInstance, projectId, refId, viewId, null);
        List<Map> operands = (List<Map>) ((Map) view.get("_contents")).get("operand");
        //   find pe in view operand and remove
        //   post view updates
        for (Iterator<Map> iterator = operands.iterator(); iterator.hasNext(); ) {
            Map operand = iterator.next();
            if (presentationId.equals(operand.get("instanceId"))) {
                iterator.remove();
                break;
            }
        }
        Element postView = new Element();
        postView.put("id", view.get("id"));
        postView.put("_contents", view.get("_contents"));
        post.addElementsItem(postView);
        RejectableElements re = apiInstance.postElements(projectId, refId, post);

        return HttpResponse.ok(response);
    }
}