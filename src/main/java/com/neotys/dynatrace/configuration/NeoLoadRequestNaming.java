package com.neotys.dynatrace.configuration;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.DynatraceContext;
import com.neotys.dynatrace.common.DynatraceUtils;
import com.neotys.dynatrace.common.HTTPGenerator;
import com.neotys.dynatrace.common.HttpResponseUtils;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;

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
    private final static String requestNamingRuleRegexp="\\{RequestAttribute:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\\}_\\{RequestAttribute:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\\}:\\{URL:Path\\}";


    private final static String requestNEWNamingRule="{RequestAttribute:NeoLoad_ScenarioName}_{RequestAttribute:NeoLoad_Transaction}:{URL:Path}";
    private final static String requestNEWNamingRuleRegexp="\\{RequestAttribute:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\\}_\\{RequestAttribute:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\\}:\\{URL:Path\\}";


    private final static String DYNATRACE_NAMING_URL="service/requestNaming";
    private final static String NeoLoad_REQUEST_ATTRIBUTE="NEOLOAD_Transaction";
    private final static String NeoLoad_NEW_REQUEST_ATTRIBUTE="NeoLoad_Transaction";
    public final static String WEB_REQUEST="WEB_REQUEST";
    public final static String WEB_SERVICE="WEB_SERVICE";

    final static String requestnaming="{RequestAttribute:%s}_{RequestAttribute:%s}:{URL:Path}";

    final static String requestNaminJson="{\n" +
            "  \"enabled\": true,\n" +
            "  \"serviceType\": \"%s\",\n" +
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
            "  ],\n" +
            "  \"skipPersonalDataMasking\": false\n" +
            "}";

    private static  boolean isaNeoLaodRequestNamingRule(String rule,String type)
    {
        Pattern requestnamingPatern;
         if(type.equalsIgnoreCase(NeoLoadRequestAttributes.NEW))
             requestnamingPatern=Pattern.compile(requestNEWNamingRuleRegexp);
         else
            requestnamingPatern=Pattern.compile(requestNamingRuleRegexp);
         return requestnamingPatern.matcher(rule).matches();
    }

    private static String generatePayLoad(String requestype,String requestattributetype)
    {

        if(requestattributetype.equalsIgnoreCase(NeoLoadRequestAttributes.NEW))
        {
            return String.format(requestNaminJson,requestype,requestNEWNamingRule,NeoLoad_NEW_REQUEST_ATTRIBUTE);

        }
        else
        {
            return String.format(requestNaminJson,requestype,requestNamingRule,NeoLoad_REQUEST_ATTRIBUTE);

        }
    }

    public static void createNeoLoadNamingRule(final Context context, final DynatraceContext dynatraceContext, final Optional<String> proxyName, final boolean traceMode,final String type,String requestattributetype) throws Exception {
        final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatraceContext.getDynatraceManagedHostname(), dynatraceContext.getDynatraceAccountID()) + DYNATRACE_NAMING_URL;
        final Map<String, String> parameters = new HashMap<>();
        HTTPGenerator httpGenerator = null;

        parameters.put("Api-Token", dynatraceContext.getApiKey());
        try{
            final Optional<Proxy> proxy = DynatraceUtils.getProxy(context,proxyName, url);

            String jsonpayload=generatePayLoad(type,requestattributetype);
            httpGenerator = HTTPGenerator.newJsonHttpGenerator(HTTP_POST_METHOD,url,dynatraceContext.getHeaders(),parameters,proxy,jsonpayload);

            if (traceMode) {
                context.getLogger().info("Dynatrace requestnaming, post request naming rule:\n" + httpGenerator.getRequest() + "payload : "+jsonpayload);
            }
            HttpResponse httpResponse = httpGenerator.execute();
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode== HttpStatus.SC_CREATED)
            {
                if(traceMode)
                    context.getLogger().info("Request Naming rule properly created");

            }
            else
            {
                context.getLogger().error("Dynatrace apî send bad response .statuscode :"+statusCode);
            }

        } finally{
            if(httpGenerator!=null)
                httpGenerator.closeHttpClient();
        }
    }

    public static String getRequetNamingPatern(String type) throws Exception {

            if(type.equalsIgnoreCase(NeoLoadRequestAttributes.NEW))
            {
                    return String.format(requestnaming,NeoLoadRequestAttributes.NEOLOAD_NEW_SCENARIO_REQUEST_ATTRIBUTE,NeoLoadRequestAttributes.NEOLOAD_NEW_TRANSACTION_REQUEST_ATTRIBUTE);
            }
            else
            {
                   return String.format(requestnaming,NeoLoadRequestAttributes.NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE,NeoLoadRequestAttributes.NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE);

            }


    }

    public static boolean isNeoLoadNamingRuleExists(final Context context, final DynatraceContext dynatraceContext, final Optional<String> proxyName, final boolean traceMode,String requestattributetype) throws Exception {
        String pattern=getRequetNamingPatern(requestattributetype);

        final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatraceContext.getDynatraceManagedHostname(), dynatraceContext.getDynatraceAccountID()) + DYNATRACE_NAMING_URL;
        final Map<String, String> parameters = new HashMap<>();
        boolean result = false;
        HTTPGenerator httpGenerator = null;

        if(traceMode)
        {
            context.getLogger().info("Patter found + "+pattern);
        }
        if(pattern==null)
            return false;

        parameters.put("Api-Token", dynatraceContext.getApiKey());
        try {
            final Optional<Proxy> proxy = DynatraceUtils.getProxy(context, proxyName, url);


            httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, dynatraceContext.getHeaders(), parameters, proxy);

            if (traceMode) {
                context.getLogger().info("Dynatrace requestnaming, check if exsits post request naming rule:\n" + httpGenerator.getRequest());
            }
            HttpResponse httpResponse = httpGenerator.execute();
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (HttpResponseUtils.isSuccessHttpCode(statusCode))
            {
                JSONObject jsonobj = HttpResponseUtils.getJsonResponse(httpResponse);
                if (jsonobj != null) {
                    JSONArray jsonArray = jsonobj.getJSONArray("values");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject requestnaming = jsonArray.getJSONObject(i);

                         if (pattern.equalsIgnoreCase(requestnaming.getString("name")))
                            return true;
                    }

                }

                return result;


            } else {
                context.getLogger().error("Dynatrace apî send bad response .statuscode :" + statusCode);
                return result;
            }

        } finally {
            httpGenerator.closeHttpClient();
        }
    }

}
