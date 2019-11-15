package com.neotys.dynatrace.configuration;

import com.neotys.dynatrace.common.DynatraceContext;
import com.neotys.dynatrace.common.DynatraceUtils;
import com.neotys.extensions.action.engine.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static com.neotys.dynatrace.common.Constants.*;

public class NeoLoadRequestNaming {
    private final static String requestNamingRule="{RequestAttribute:NEOLOAD_ScenarioName}_{RequestAttribute:NEOLOAD_Transaction}:{URL:Path}";
    private final static String requestNEWNamingRule="{RequestAttribute:NeoLoad_ScenarioName}_{RequestAttribute:NeoLoad_Transaction}:{URL:Path}";

    private final static String NeoLoad_REQUEST_ATTRIBUTE="NEOLOAD_Transaction";
    private final static String NeoLoad_NEW_REQUEST_ATTRIBUTE="NeoLoad_Transaction";
    public final static String WEB_REQUEST="WEB_REQUEST";
    public final static String WEB_SERVICE="WEB_SERVICE";

    //final static String requestnaming="{RequestAttribute:%s}_{RequestAttribute:%s}:{URL:Path}";

    final static String requestNamingJson="{\n" +
            "  \"enabled\": true,\n" +
            "  \"namingPattern\": \"%s\",\n" +
            "  \"conditions\": [\n" +
            "    {\n" +
            "      \"attribute\": \"SERVICE_REQUEST_ATTRIBUTE\",\n" +
            "      \"comparisonInfo\": {\n" +
            "        \"type\": \"STRING_REQUEST_ATTRIBUTE\",\n" +
            "        \"comparison\": \"EXISTS\",\n" +
            "        \"requestAttribute\": \"%s\",\n" +
            "        \"negate\": false\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private static String generatePayLoad(String requestattributetype) {

        if (requestattributetype.equalsIgnoreCase(NeoLoadRequestAttributes.NEW)) {
            return String.format(requestNamingJson,requestNEWNamingRule,NeoLoad_NEW_REQUEST_ATTRIBUTE);
        } else {
            return String.format(requestNamingJson,requestNamingRule,NeoLoad_REQUEST_ATTRIBUTE);
        }
    }

    public static void createNeoLoadNamingRule(final Context context, final DynatraceContext dynatraceContext, final boolean traceMode, String requestattributetype) throws Exception {
    	
        String jsonpayload=generatePayLoad(requestattributetype);
    	DynatraceUtils.executeDynatraceAPIPostObjectRequest(context, dynatraceContext, DTAPI_CFG_EP_REQUEST_NAMING, jsonpayload, traceMode);
    }

    public static boolean existsNeoLoadNamingRule(final Context context, final DynatraceContext dynatracecontext, final boolean tracemode, String requestattributetype) throws Exception {
        final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        JSONObject jsonobjectresponse=DynatraceUtils.executeDynatraceAPIGetObjectRequest(context, dynatracecontext, DTAPI_CFG_EP_REQUEST_NAMING, parameters, tracemode);
        
        if (jsonobjectresponse != null) {
            JSONArray jsonArray = jsonobjectresponse.getJSONArray("values");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject requestnaming = jsonArray.getJSONObject(i);

                 if (requestNEWNamingRule.equalsIgnoreCase(requestnaming.getString("name")))
                    return true;
            }
        }
        return false;
    }

}
