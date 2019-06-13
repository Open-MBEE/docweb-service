package gov.nasa.jpl.mbee.services;

import com.google.gson.internal.LinkedTreeMap;
import gov.nasa.jpl.mbee.domains.PresentationElement;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    public static String Equation_ID = "17_0_5_1_407019f_1431905053808_352752_11992";
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

    public static ApiClient createClient(String ticket, Optional<String> auth, String server) {
        ApiClient client = new ApiClient();
        client.setConnectTimeout(300000);
        client.setReadTimeout(600000);
        client.setWriteTimeout(600000);
        Map<String, String> basic = getBasicAuth(auth.orElse(null));
        if (ticket != null) {
            client.setApiKey(ticket);
        } else if (basic != null) {
            client.setPassword(basic.get("password"));
            client.setUsername(basic.get("username"));
        }
        client.setBasePath(server + "/alfresco/service");
        return client;
    }

    public static Element getElement(ElementApi apiInstance, String projectId, String refId, String id,
        String commitId) throws ApiException {

        return apiInstance.getElement(projectId,refId,id,2, true, commitId).getElements().get(0);
    }

    public static List<Element> getElements(ElementApi apiInstance, String projectId, String refId, Set<String> ids, String commitId) throws ApiException {
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
            Object jsonString = instanceSpec.get("value");

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