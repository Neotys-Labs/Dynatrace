package com.neotys.dynatrace.configuration;

import com.neotys.dynatrace.common.Api;
import com.neotys.dynatrace.common.DynatraceContext;
import com.neotys.dynatrace.common.DynatraceUtils;
import com.neotys.extensions.action.engine.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;


public class NeoLoadRequestNaming {
//{
//  "values": [
//    {
//      "id": "85ad3653-3371-451f-9876-1532f0945503",
//      "name": "{RequestAttribute:43c92a98-67c8-465f-b80b-b5ae38632367}_{RequestAttribute:0c8d4694-257b-4dcd-90c4-873392b699f0}:{URL:Path}"
//    },
//    {
//      "id": "bd30bbc9-2123-4abc-a880-5cfa014c6506",
//      "name": "{RequestAttribute:43c92a98-67c8-465f-b80b-b5ae38632367}_{RequestAttribute:0c8d4694-257b-4dcd-90c4-873392b699f0}:{URL:Path}"
//    }
//  ]
//}

    private final static String requestNamingRule="{RequestAttribute:NEOLOAD_ScenarioName}_{RequestAttribute:NEOLOAD_Transaction}:{URL:Path}";
//    private final static String requestNamingRuleRegexp="\\{RequestAttribute:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\\}_\\{RequestAttribute:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\\}:\\{URL:Path\\}";


    private final static String requestNEWNamingRule="{RequestAttribute:NeoLoad_ScenarioName}_{RequestAttribute:NeoLoad_Transaction}:{URL:Path}";
//    private final static String requestNEWNamingRuleRegexp="\\{RequestAttribute:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\\}_\\{RequestAttribute:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\\}:\\{URL:Path\\}";


    private final static String DYNATRACE_NAMING_URL="service/requestNaming";
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
/*
    private static  boolean isaNeoLaodRequestNamingRule(String rule,String type) {
        Pattern requestnamingpattern;
         if(type.equalsIgnoreCase(NeoLoadRequestAttributes.NEW))
             requestnamingpattern=Pattern.compile(requestNEWNamingRuleRegexp);
         else
            requestnamingpattern=Pattern.compile(requestNamingRuleRegexp);
         return requestnamingpattern.matcher(rule).matches();
    }
*/
    private static String generatePayLoad(String requestattributetype) {

        if (requestattributetype.equalsIgnoreCase(NeoLoadRequestAttributes.NEW)) {
            return String.format(requestNamingJson,requestNEWNamingRule,NeoLoad_NEW_REQUEST_ATTRIBUTE);
        } else {
            return String.format(requestNamingJson,requestNamingRule,NeoLoad_REQUEST_ATTRIBUTE);
        }
    }

    public static void createNeoLoadNamingRule(final Context context, final DynatraceContext dynatraceContext, final boolean traceMode, String requestattributetype) throws Exception {
    	
        String jsonpayload=generatePayLoad(requestattributetype);
    	DynatraceUtils.executeDynatraceAPIPostObjectRequest(context, dynatraceContext, Api.CFG, DYNATRACE_NAMING_URL, jsonpayload, traceMode);
    }

    public static boolean existsNeoLoadNamingRule(final Context context, final DynatraceContext dynatracecontext, final boolean tracemode, String requestattributetype) throws Exception {
        final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        JSONObject jsonobjectresponse=DynatraceUtils.executeDynatraceAPIGetObjectRequest(context, dynatracecontext, Api.CFG, DYNATRACE_NAMING_URL, parameters, tracemode);
        
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
