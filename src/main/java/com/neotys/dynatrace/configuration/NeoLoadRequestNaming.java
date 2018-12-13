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

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;

public class NeoLoadRequestNaming {


    private final static String requestNamingRule="{RequestAttribute:NeoLoad_ScenarioName}_{RequestAttribute:NeoLoad_Transaction}_{URL:Path}";
    private final static String DYNATRACE_NAMING_URL="requestNaming";
    private final static String NeoLoad_REQUEST_ATTRIBUTE="NeoLoad_Transaction";
    public final static String WEB_REQUEST="WEB_REQUEST";
    public final static String WEB_SERVICE="WEB_SERVICE";

    public final static String requestNaminJson="{\n" +
            "  \"enabled\": true,\n" +
            "  \"serviceType\": \"%s\",\n" +
            "  \"namingPattern\": \""+requestNamingRule+"\",\n" +
            "  \"conditions\": [\n" +
            "    {\n" +
            "      \"attribute\": \"SERVICE_REQUEST_ATTRIBUTE\",\n" +
            "      \"comparisonInfo\": {\n" +
            "        \"type\": \"STRING_REQUEST_ATTRIBUTE\",\n" +
            "        \"comparison\": \"EXISTS\",\n" +
            "        \"requestAttribute\": \""+NeoLoad_REQUEST_ATTRIBUTE+"\",\n" +
            "        \"negate\": false\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"skipPersonalDataMasking\": false\n" +
            "}";

    public static void createNeoLoadNamingRule(final Context context, final DynatraceContext dynatraceContext, final Optional<String> proxyName, final boolean traceMode,final String type) throws Exception {
        final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatraceContext.getDynatraceManagedHostname(), dynatraceContext.getDynatraceAccountID()) + DYNATRACE_NAMING_URL;
        final Map<String, String> parameters = new HashMap<>();
        HTTPGenerator httpGenerator = null;

        parameters.put("Api-Token", dynatraceContext.getApiKey());
        try{
            final Optional<Proxy> proxy = DynatraceUtils.getProxy(context,proxyName, url);

            String jsonpayload=String.format(requestNaminJson,type);
            httpGenerator = HTTPGenerator.newJsonHttpGenerator(HTTP_POST_METHOD,url,dynatraceContext.getHeaders(),parameters,proxy,requestNaminJson);

            if (traceMode) {
                context.getLogger().info("Dynatrace requestnaming, post request naming rule:\n" + httpGenerator.getRequest());
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

    public static boolean isNeoLoadNamingRuleExists(final Context context, final DynatraceContext dynatraceContext, final Optional<String> proxyName, final boolean traceMode) throws Exception {
        final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatraceContext.getDynatraceManagedHostname(), dynatraceContext.getDynatraceAccountID()) + DYNATRACE_NAMING_URL;
        final Map<String, String> parameters = new HashMap<>();
        boolean result = false;
        HTTPGenerator httpGenerator = null;


        parameters.put("Api-Token", dynatraceContext.getApiKey());
        try {
            final Optional<Proxy> proxy = DynatraceUtils.getProxy(context, proxyName, url);


            httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, dynatraceContext.getHeaders(), parameters, proxy);

            if (traceMode) {
                context.getLogger().info("Dynatrace requestnaming, post request naming rule:\n" + httpGenerator.getRequest());
            }
            HttpResponse httpResponse = httpGenerator.execute();
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (HttpResponseUtils.isSuccessHttpCode(statusCode))
            {
                JSONObject jsonobj = HttpResponseUtils.getJsonResponse(httpResponse);
                if (jsonobj != null) {
                    JSONArray jsonArray = jsonobj.getJSONArray("requestNamings");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject requestnaming = jsonArray.getJSONObject(i);
                        if (requestnaming.getString("namingPattern").equalsIgnoreCase(requestNamingRule))
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
