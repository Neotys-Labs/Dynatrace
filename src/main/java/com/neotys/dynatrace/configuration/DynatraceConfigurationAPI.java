package com.neotys.dynatrace.configuration;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.*;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;

public class DynatraceConfigurationAPI {
    private static final String DYNATRACE_EVENTS_API_URL = "requestAttributes";
    private static final String MESSAGE_NL_TEST = "Start/Stop NeoLoad Test";


    private final Map<String, String> headers;
    private final String dynatraceApiKey;
    private final String dynatraceAccountID;
    private final Optional<String> dynatraceManagedHostname;
    private final Optional<String> proxyName;
    private final Context context;
    private boolean traceMode;
    private HTTPGenerator httpGenerator;

    private static final String REQUEST_ATTRIBUTE_PAYLOAD="{\n" +
            "  \"name\": \"%s\",\n" +
            "  \"enabled\": true,\n" +
            "  \"dataType\": \"STRING\",\n" +
            "  \"dataSources\": [\n" +
            "    {\n" +
            "      \"enabled\": true,\n" +
            "      \"source\": \"REQUEST_HEADER\",\n" +
            "      \"valueProcessing\": { \n" +
            "                               \"valueCondition\": {\n"+
            "                                \"operator\": \"BEGINS_WITH\",\n"+
            "                                \"negate\": true,\n"+
            "                               \"value\": \"FW\"},\n"+
            "       \"splitAt\": \"\",\n"+
            "       \"trim\": false,\n"+
            "       \"extractSubstring\": {\n"+
            "                               \"position\": \"BETWEEN\",\n"+
            "                                \"delimiter\": \"%s=\",\n"+
            "                                \"endDelimiter\": \";\"}},\n"+
            "  \"parameterName\": \"%s\",\n"+
            "  \"capturingAndStorageLocation\": \"CAPTURE_AND_STORE_ON_SERVER\"\n"+
            "      }\n"+
            "  ],\n" +
            "  \"normalization\": \"TO_LOWER_CASE\",\n" +
            "  \"aggregation\": \"ALL_DISTINCT_VALUES\",\n" +
            "  \"confidential\": false,\n" +
            "  \"skipPersonalDataMasking\": false\n" +
            "}";

    public DynatraceConfigurationAPI(String dynatraceApiKey, String dynatraceAccountID, Optional<String> dynatraceManagedHostname, Optional<String> proxyName, Context context, boolean traceMode) {
        this.dynatraceApiKey = dynatraceApiKey;
        this.dynatraceAccountID = dynatraceAccountID;
        this.dynatraceManagedHostname = dynatraceManagedHostname;
        this.proxyName = proxyName;
        this.context = context;
        this.traceMode = traceMode;
        this.headers = new HashMap<>();
        generateHeaders(headers);
    }
    private void sendTokenIngetParam(final Map<String, String> param) {
        param.put("Api-Token", dynatraceApiKey);

    }

    private void generateHeaders(final Map<String,String> header)
    {
        header.put("Accept","application/json; charset=utf-8");
        header.put("Content-Type","application/json");
    }

    private Optional<Proxy> getProxy(final Optional<String> proxyName, final String url) throws MalformedURLException {
        if (proxyName.isPresent()) {
            return Optional.fromNullable(context.getProxyByName(proxyName.get(), new URL(url)));
        }
        return Optional.absent();
    }

    public void setDynatraceTags(String applicationName,Optional<String> tags) throws Exception {
        List<String> serviceListId;
        AtomicReference<List<String>> processgroupListids=new AtomicReference<>();;
        AtomicReference<List<String>> hostListid = new AtomicReference<>();;
        Map<String, String> header = new HashMap<>();

        DynatraceContext dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceAccountID, tags, header);

        serviceListId=DynatraceUtils.getListServicesFromApplicationName(context,dynatraceContext,applicationName,proxyName,traceMode,false);
        serviceListId.stream().forEach(serviceid->{
            try {
                processgroupListids.set(DynatraceUtils.getListProcessGroupIDfromServiceId(context, dynatraceContext, serviceid, proxyName, traceMode,false));
                DynatraceUtils.updateTagOnserviceID(context,dynatraceContext,proxyName,traceMode,serviceid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            processgroupListids.get().stream().forEach(processgroupid->{
                try {
                    hostListid.set(DynatraceUtils.getHostIdFromProcessGroupID(context, dynatraceContext, processgroupid, proxyName, traceMode));
                    DynatraceUtils.updateTagOnProcessGroupID(context,dynatraceContext,proxyName,traceMode,processgroupid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                hostListid.get().stream().forEach(hostid->{
                    try {
                        DynatraceUtils.updateTagOnHostID(context,dynatraceContext,proxyName,traceMode,hostid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            });

        });
    }

    private boolean isNeoLoadRequestAttributesExists() throws Exception {
       boolean result=false;
       final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatraceManagedHostname, dynatraceAccountID) + DYNATRACE_EVENTS_API_URL;
       final Map<String, String> parameters = new HashMap<>();

       sendTokenIngetParam(parameters);

        try{
           final Optional<Proxy> proxy = getProxy(proxyName, url);


           httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, headers, parameters, proxy);

           if (traceMode) {
               context.getLogger().info("Dynatrace service, get request attributes:\n" + httpGenerator.getRequest());
           }
           HttpResponse httpResponse = httpGenerator.execute();
           final int statusCode = httpResponse.getStatusLine().getStatusCode();
           if (HttpResponseUtils.isSuccessHttpCode(statusCode))
           {
               JSONObject jsonobj = HttpResponseUtils.getJsonResponse(httpResponse);
               if (jsonobj != null)
               {
                   JSONArray jsonArray=jsonobj.getJSONArray("values");
                   return  NeoLoadRequestAttributes.isNeoLoadRequestAttributesExists(jsonArray);

               }
               else
               {
                   return result;
               }


           }
           else
           {
               context.getLogger().error("Dynatrace apî send bad response .statuscode :"+statusCode);
               return result;
           }

        } finally{
               httpGenerator.closeHttpClient();
           }


       }

    public void generateRequestAttributes() throws Exception {
        if(!isNeoLoadRequestAttributesExists())
        {
            createRequestAttributes();
        }
    }
   private static String newRequesAttributPayload(final String parametername,final String headersuffix,final String headername) {
       return String.format(REQUEST_ATTRIBUTE_PAYLOAD, parametername,headersuffix,headername);

   }
   private void createRequestAttributes()
   {
       Map<String,String> attributeshashmap=NeoLoadRequestAttributes.generateHasMap();
       attributeshashmap.forEach((k,v)-> {
           try {
               createRequestAttribute(k,v);
           } catch (Exception e) {
               context.getLogger().error("Technical Error ",e);
           }
       });
    }

    private void createRequestAttribute(String parametername,String headersuffix) throws Exception {
        String payload=newRequesAttributPayload(parametername,headersuffix,NeoLoadRequestAttributes.NEOLOAD_HTTP_HEADER_NAME);
        final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatraceManagedHostname, dynatraceAccountID) + DYNATRACE_EVENTS_API_URL;
        final Map<String, String> parameters = new HashMap<>();

        sendTokenIngetParam(parameters);
        try {
            final Optional<Proxy> proxy = getProxy(proxyName, url);


            httpGenerator = HTTPGenerator.newJsonHttpGenerator(HTTP_POST_METHOD, url, headers, parameters, proxy,payload);

            if (traceMode) {
                context.getLogger().info("Dynatrace service, post request attributes:\n" + httpGenerator.getRequest());
            }
            HttpResponse httpResponse = httpGenerator.execute();
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode== HttpStatus.SC_CREATED)
            {
                if(traceMode)
                    context.getLogger().info("Request attributes properly created");
            }
            else
            {
                    context.getLogger().error("Unable to create request attributes "+ payload);
            }


        }
        finally{
            httpGenerator.closeHttpClient();
        }
    }

}
