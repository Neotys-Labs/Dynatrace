package com.neotys.dynatrace.configuration;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.*;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.text.html.Option;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;

public class DynatraceConfigurationAPI {
    private static final String DYNATRACE_EVENTS_API_URL = "service/requestAttributes";


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



    public void setDynatraceTags(Optional<String> tags) throws Exception {
        List<String> serviceListId;
        List<String> dependenListId;
        AtomicReference<List<String>> processgroupListids=new AtomicReference<>();;
        AtomicReference<List<String>> hostListid = new AtomicReference<>();;
        Map<String, String> header = new HashMap<>();

        DynatraceContext dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceAccountID, tags, header);

        serviceListId=DynatraceUtils.getApplicationEntityIds(context,dynatraceContext,proxyName,traceMode);
        dependenListId=new ArrayList<>();
        serviceListId.stream().forEach(serviceid-> {
            try {
                dependenListId.addAll(DynatraceUtils.getListDependentServicesFromServiceID(context,dynatraceContext,serviceid,proxyName,traceMode,false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //search for dependencies
        int size=dependenListId.size();
        List<String> tmp=new ArrayList<>();
        tmp.addAll(dependenListId);
        while(size>0)
        {
            List<String> secondleveldependenListId=new ArrayList<>();
            tmp.stream().forEach(serviceid-> {
                try {
                    secondleveldependenListId.addAll(DynatraceUtils.getListDependentServicesFromServiceID(context,dynatraceContext,serviceid,proxyName,traceMode,false));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            size=secondleveldependenListId.size();
            tmp=new ArrayList<>();
            tmp.addAll(secondleveldependenListId);
            dependenListId.addAll(secondleveldependenListId);
        }
        serviceListId=Stream.concat(serviceListId.stream(), dependenListId.stream()).distinct()
                .collect(Collectors.toList());

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

    public void createRequestNamingRules(String type) throws Exception {
        DynatraceContext dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceAccountID, Optional.absent(), this.headers);

        HashMap<String,String> requestAttributesID=getRequestAttributesids(type);

        if(!NeoLoadRequestNaming.isNeoLoadNamingRuleExists(context,dynatraceContext,proxyName,traceMode,requestAttributesID,type))
        {
            NeoLoadRequestNaming.createNeoLoadNamingRule(context, dynatraceContext, proxyName, traceMode,NeoLoadRequestNaming.WEB_SERVICE,type);
            NeoLoadRequestNaming.createNeoLoadNamingRule(context, dynatraceContext, proxyName, traceMode,NeoLoadRequestNaming.WEB_REQUEST,type);

        }
    }

    public  HashMap<String,String> getRequestAttributesids(String type) throws Exception {
        List<String> keys=new ArrayList<>();
        if(type.equalsIgnoreCase(NeoLoadRequestAttributes.NEW))
        {
            keys.add(NeoLoadRequestAttributes.NEOLOAD_NEW_SCENARIO_REQUEST_ATTRIBUTE);
            keys.add(NeoLoadRequestAttributes.NEOLOAD_NEW_TRANSACTION_REQUEST_ATTRIBUTE);
        }
        else
        {
            keys.add(NeoLoadRequestAttributes.NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE);
            keys.add(NeoLoadRequestAttributes.NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE);
        }
        return getRequestAttributeType(keys);
    }

    public  HashMap<String,String> getRequestAttributeType(List<String> key) throws Exception
    {
        final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatraceManagedHostname, dynatraceAccountID) + DYNATRACE_EVENTS_API_URL;
        final Map<String, String> parameters = new HashMap<>();


        sendTokenIngetParam(parameters);

        try{
            final Optional<Proxy> proxy = getProxy(proxyName, url);


            httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, headers, parameters, proxy);

            if (traceMode) {
                context.getLogger().info("Dynatrace service, get request attributes ids:\n" + httpGenerator.getRequest());
            }
            HttpResponse httpResponse = httpGenerator.execute();
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (HttpResponseUtils.isSuccessHttpCode(statusCode))
            {
                JSONObject jsonobj = HttpResponseUtils.getJsonResponse(httpResponse);
                if (jsonobj != null)
                {
                    JSONArray jsonArray=jsonobj.getJSONArray("values");
                    if (traceMode) {
                        context.getLogger().info("json array : " + jsonArray.toString() + " and list of keys "+ key.toString());
                    }
                    return  NeoLoadRequestAttributes.getNeoLoadRequestAttributeEqualToKey(jsonArray,key);

                }
                else
                {
                    return null;
                }


            }
            else
            {
                context.getLogger().error("Dynatrace apî send bad response .statuscode :"+statusCode);
                return null;
            }

        } finally{
            httpGenerator.closeHttpClient();
        }

     }
        private boolean isNeoLoadRequestAttributesExists(String type) throws Exception {
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
                   return  NeoLoadRequestAttributes.isNeoLoadRequestAttributesExists(jsonArray,type);

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

    public void generateRequestAttributes(String type) throws Exception {
        if(!isNeoLoadRequestAttributesExists(type))
        {
            createRequestAttributes(type);
        }
    }
   private static String newRequesAttributPayload(final String parametername,final String headersuffix,final String headername) {
       return String.format(REQUEST_ATTRIBUTE_PAYLOAD, parametername,headersuffix,headername);

   }
   private void createRequestAttributes(String type)
   {
       Map<String,String> attributeshashmap=NeoLoadRequestAttributes.generateHasMap(type);
       attributeshashmap.forEach((k,v)-> {
           try {
               createRequestAttribute(k,v,type);
           } catch (Exception e) {
               context.getLogger().error("Technical Error ",e);
           }
       });
    }

    private void createRequestAttribute(String parametername,String headersuffix,String type) throws Exception {
        String payload;
        if(type.equalsIgnoreCase(NeoLoadRequestAttributes.NEW))
            payload=newRequesAttributPayload(parametername,headersuffix,NeoLoadRequestAttributes.NEOLOAD_HTTP_HEADER_NAME_NEW);
        else
            payload=newRequesAttributPayload(parametername,headersuffix,NeoLoadRequestAttributes.NEOLOAD_HTTP_HEADER_NAME);

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
