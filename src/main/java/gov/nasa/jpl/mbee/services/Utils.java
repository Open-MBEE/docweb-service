package gov.nasa.jpl.mbee.services;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import gov.nasa.jpl.mbee.domains.PresentationElement;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.openmbee.mms.client.ApiClient;
import org.openmbee.mms.client.ApiException;
import org.openmbee.mms.client.api.ElementApi;
import org.openmbee.mms.client.model.Element;
import org.openmbee.mms.client.model.Elements;
import org.openmbee.mms.client.model.RejectableElements;

/**
 * @author shakeh
 */
public class Utils {

    public static String Section_ID = "_18_0_2_407019f_1435683487667_494971_14412";
    public static String Paragraph_ID = "_17_0_5_1_407019f_1431903758416_800749_12055";
    public static String Image_ID = "_17_0_5_1_407019f_1431903748021_2367_12034";
    public static String Table_ID = "_17_0_5_1_407019f_1431903724067_825986_11992";
    public static String Equation_ID = "_17_0_5_1_407019f_1431905053808_352752_11992";
    public static String List_ID = "_17_0_5_1_407019f_1431903739087_549326_12013";

    public static Map<String, String> getBasicAuth(String authorization) {
        Map<String, String> result = null;
        try {
            //final String authorization = httpRequest.getHeader("Authorization");
            if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
                // Authorization: Basic base64credentials
                String base64Credentials = authorization.substring("Basic".length()).trim();
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                // credentials = username:password
                final String[] values = credentials.split(":", 2);
                result = new HashMap<>();
                result.put("username", values[0]);
                result.put("password", values[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ApiClient createClient(Optional<String> ticket, Optional<String> auth, String server) {
        ApiClient client = new ApiClient();
        client.setConnectTimeout(300000);
        client.setReadTimeout(600000);
        client.setWriteTimeout(600000);
        Map<String, String> basic = getBasicAuth(auth.orElse(null));
        if (ticket.isPresent()) {
            client.setApiKey(ticket.get());
        } else if (basic != null) {
            client.setPassword(basic.get("password"));
            client.setUsername(basic.get("username"));
        }
        client.setBasePath(server + "/alfresco/service");
        return client;
    }

    public static Element getElement(ElementApi apiInstance, String projectId, String refId, String id,
        String commitId) throws ApiException {

        return apiInstance.getElement(projectId, refId, id, null, null, commitId).getElements().get(0);
    }

    public static List<Element> getElements(ElementApi apiInstance, String projectId, String refId, List<String> ids, String commitId) throws ApiException {
        Elements body = new Elements();
        for (String id: ids) {
            Element a = new Element();
            a.put("id", id);
            body.addElementsItem(a);
        }
        try {
            RejectableElements e =  apiInstance.getElementsInBatch(projectId, refId, body, null, null, commitId);
            return e.getElements();
        } catch (ApiException e) {
            if (e.getCode() != 404) {
                throw e;
            }
            return new ArrayList<Element>();
        }
    }

    public static boolean isPeByType(HashMap<String, ArrayList> instance, String typeID) {
        if (instance.get("classifierIds") != null) {
            return (instance.get("classifierIds").size() > 0) && (instance.get("classifierIds").get(0).equals(typeID));
        }
        return false;
    }

    public static String getPresentationElementType(Element instance) {
        LinkedTreeMap instanceSpec = (LinkedTreeMap) instance.get("specification");
        if (instanceSpec == null) {
            return null;
        }

        String instanceType = (String) instanceSpec.get("type");
        if ( instanceType.equals("LiteralString") ) { // If it is a Opaque List, Paragraph, Table, Image, List:
            if (isPeByType((HashMap) instance, Paragraph_ID)) {
                return "Paragraph";
            } else if (isPeByType((HashMap)instance, Image_ID)) {
                return "Image";
            } else if (isPeByType((HashMap)instance, Table_ID)) {
                return "Table";
            } else if (isPeByType((HashMap)instance, Equation_ID)) {
                return "Equation";
            } else if (isPeByType((HashMap)instance, List_ID)){
                return "List";
            }
        } else if ( instanceType.equals("Expression") ) { // If it is a Opaque Section, or a Expression:
            if (isPeByType( (HashMap) instance, Section_ID)) {
                return "Section"; // set const?
            } else {
                return "Generated"; //What should it do?
            }
        }
        return instanceType;
    }


    public static PresentationElement buildResponsePe(Element pe) {
        PresentationElement responsePE = new PresentationElement();
        responsePE.setId((String) pe.get("id"));
        responsePE.setName((String) pe.get("name"));
        responsePE.setContent((String) pe.get("documentation"));
        String peType = Utils.getPresentationElementType(pe);
        responsePE.setType(peType);
        return responsePE;
    }

    public static Element createInstanceFromPe(PresentationElement pe, String projectId) {
        String id = pe.getId() == null ? "_hidden_" + createId() + "_pei" : pe.getId();
        pe.setId(id);
        PresentationElement newPE = new PresentationElement();
        newPE.setType(pe.getType() == null ? "Paragraph" : pe.getType());
        newPE.setName(pe.getName() == null ? "" : pe.getName());
        newPE.setContent(pe.getContent() == null ? "" : pe.getContent());
        newPE.setId(id);

        Map<String, Object> valueSpec = createValueSpec();
        valueSpec.put("ownerId", id);
        if ("Section".equals(newPE.getType())) {
            valueSpec.put("type", "Expression");
            valueSpec.put("operand", new ArrayList());
        } else {
            valueSpec.put("type", "LiteralString");
            valueSpec.put("value", createSpecForPe(newPE, id));
        }

        List<String> classifierIds = new ArrayList<>();
        classifierIds.add(getClassifierId(newPE));

        Element e = new Element();
        e.put("appliedStereotypeInstanceId", null);
        e.put("classifierIds", classifierIds);
        e.put("clientDependencyIds", new ArrayList());
        e.put("deploymentIds", new ArrayList());
        e.put("documentation", newPE.getContent());
        e.put("mdExtensionIds", new ArrayList());
        e.put("name", newPE.getName());
        e.put("nameExpression", null);
        e.put("ownerId", "view_instances_bin_" + projectId);
        e.put("slotIds", new ArrayList());
        e.put("specification", valueSpec);
        e.put("stereotypedElementId", null);
        e.put("supplierDependencyIds", new ArrayList());
        e.put("syncElementId", null);
        e.put("templateParameterId", null);
        e.put("type", "InstanceSpecification");
        e.put("visibility", "public");
        e.put("_appliedStereotypeIds", new ArrayList());
        e.put("id", id);

        return e;
    }

    public static String createSpecForPe(PresentationElement pe, String instanceId) {
        Map<String, Object> spec = new HashMap<>();
        spec.put("type", getJsonType(pe));
        spec.put("sourceType", "reference");
        spec.put("source", instanceId);
        spec.put("sourceProperty", "documentation");
        Gson gson = new Gson();
        return gson.toJson(spec);
    }

    public static String getJsonType(PresentationElement pe) {
        String t = pe.getType();
        if ("Paragraph".equals(t) || "Comment".equals(t) || "Equation".equals(t)) {
            return t;
        }
        return t + "T";
    }

    public static String getClassifierId(PresentationElement pe) {
        String t = pe.getType();
        switch (t) {
            case "Image":
                return Image_ID;
            case "Table":
                return Table_ID;
            case "Equation":
                return Equation_ID;
            case "List":
                return List_ID;
            case "Section":
                return Section_ID;
            case "Paragraph":
            case "Comment":
            default:
                return Paragraph_ID;
        }
    }

    public static Map<String, Object> createOperandForInstance(Element instance) {
        Map<String, Object> e = createValueSpec();
        e.put("type", "InstanceValue");
        e.put("instanceId", instance.get("id"));
        return e;
    }

    public static Map<String, Object> createExpression() {
        Map<String, Object> e = createValueSpec();
        e.put("type", "Expression");
        e.put("operand", new ArrayList());
        return e;
    }

    public static Map<String, Object> createValueSpec() {
        Map<String, Object> e = new HashMap<>();
        e.put("appliedStereotypeInstanceId", null);
        e.put("clientDependencyIds", new ArrayList());
        e.put("documentation", "");
        e.put("mdExtensionIds", new ArrayList());
        e.put("name", "");
        e.put("nameExpression", null);
        e.put("supplierDependencyIds", new ArrayList());
        e.put("syncElementId", null);
        e.put("templateParameterId", null);
        e.put("typeId", null);
        e.put("visibility", "public");
        e.put("_appliedStereotypeIds", new ArrayList());
        e.put("id", createId());
        return e;
    }

    public static String createId() {
        return "MMS_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString();
    }

//  PE template
// INSTANCE_ELEMENT_TEMPLATE = {
//    appliedStereotypeInstanceId: null,
//  classifierIds: [],
//  clientDependencyIds: [],
//  deploymentIds: [],
//  documentation: '',
//  mdExtensionsIds: [],
//  name: '',
//  nameExpression: null,
//  ownerId: null,
//  slotIds: [],
//  specification: null,
//  stereotypedElementId: null,
//  supplierDependencyIds: [],
//  syncElementId: null,
//  templateParameterId: null,
//  type: "InstanceSpecification",
//  visibility: "public",
//  _appliedStereotypeIds: [],
//};
}