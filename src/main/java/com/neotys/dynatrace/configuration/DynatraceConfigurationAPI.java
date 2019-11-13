package com.neotys.dynatrace.configuration;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.*;
import com.neotys.dynatrace.common.topology.DynatraceTopologyWalker;
import com.neotys.extensions.action.engine.Context;
//import com.neotys.extensions.action.engine.Proxy;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

//import java.net.MalformedURLException;
//import java.net.URL;
import java.util.*;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

//import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
//import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;

public class DynatraceConfigurationAPI {
    private static final String DYNATRACE_REQUEST_ATTRIBUTE_API_URL = "service/requestAttributes";

//    private final Map<String, String> headers;
//    private final String dynatraceApiKey;
//    private final String dynatraceAccountID;
//    private final Optional<String> dynatraceManagedHostname;
//    private final Optional<String> proxyName;
    private final Context context;
    private final DynatraceContext dynatracecontext;
    private boolean traceMode;
//    private HTTPGenerator httpGenerator;

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

    public DynatraceConfigurationAPI(String dynatraceApiKey, String dynatraceAccountID, Optional<String> dynatraceManagedHostname, Optional<String> proxyName, Optional<String> tags,Context context, boolean traceMode) {
    	dynatracecontext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceAccountID, proxyName,tags, new HashMap<>());
//        this.dynatraceApiKey = dynatraceApiKey;
//        this.dynatraceAccountID = dynatraceAccountID;
//        this.dynatraceManagedHostname = dynatraceManagedHostname;
//        this.proxyName = proxyName;
        this.context = context;
        this.traceMode = traceMode;
//        this.headers = new HashMap<>();
//        generateHeaders(headers);
    }
/*    
    private void sendTokenIngetParam(final MultivaluedMap<String, String> params) {
        params.add("Api-Token", dynatraceApiKey);

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
*/
    
    public void setDynatraceTags(Optional<String> tags) throws Exception {
//        Map<String, String> header = new HashMap<>();
//        DynatraceContext dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceAccountID, proxyName, tags, header);

        DynatraceTopologyWalker dtw=new DynatraceTopologyWalker(context,dynatracecontext,traceMode);
        dtw.executeDiscovery();
        
        dtw.getDiscoveredData().getServices().parallelStream().forEach(serviceid->{
            try {
                DynatraceUtils.updateTagOnServiceID(context,dynatracecontext,traceMode,serviceid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
        });
/*
        dtw.getDiscoveredData().getProcessGroupInstances().parallelStream().forEach(pgiid->{
            try {
                DynatraceUtils.updateTagOnPgInstanceID(context,dynatracecontext,traceMode,pgiid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
        });
*/        
        dtw.getDiscoveredData().getProcessGroups().parallelStream().forEach(pgid->{
            try {
                DynatraceUtils.updateTagOnProcessGroupID(context,dynatracecontext,traceMode,pgid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
        });
        dtw.getDiscoveredData().getHosts().parallelStream().forEach(hostid->{
            try {
                DynatraceUtils.updateTagOnHostID(context,dynatracecontext,traceMode,hostid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void createRequestNamingRules(String type) throws Exception {
        //DynatraceContext dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceAccountID, proxyName, Optional.absent(), this.headers);

        //HashMap<String,String> requestAttributesID=getRequestAttributesids(type);

        if(!NeoLoadRequestNaming.existsNeoLoadNamingRule(context,dynatracecontext,traceMode/*,requestAttributesID*/,type)) {
            NeoLoadRequestNaming.createNeoLoadNamingRule(context, dynatracecontext,/* proxyName,*/ traceMode,type);
  //          NeoLoadRequestNaming.createNeoLoadNamingRule(context, dynatraceContext, proxyName, traceMode,type);
        }
    }

/*    
	private HashMap<String, String> getRequestAttributesids(String type) throws Exception {
		List<String> keys = new ArrayList<>();
		if (type.equalsIgnoreCase(NeoLoadRequestAttributes.NEW)) {
			keys.add(NeoLoadRequestAttributes.NEOLOAD_NEW_SCENARIO_REQUEST_ATTRIBUTE);
			keys.add(NeoLoadRequestAttributes.NEOLOAD_NEW_TRANSACTION_REQUEST_ATTRIBUTE);
		} else {
			keys.add(NeoLoadRequestAttributes.NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE);
			keys.add(NeoLoadRequestAttributes.NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE);
		}
		return getRequestAttributeType(keys);
	}

    public  HashMap<String,String> getRequestAttributeType(List<String> key) throws Exception {
        Map<String, String> header = new HashMap<>();
        DynatraceContext dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceAccountID, proxyName, Optional.absent(), header);
        final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        
        JSONObject jsonobj = DynatraceUtils.executeDynatraceAPIGetObjectRequest(context, dynatraceContext, Api.CFG, DYNATRACE_REQUEST_ATTRIBUTE_API_URL, parameters, traceMode);
        if (jsonobj != null) {
            JSONArray jsonArray=jsonobj.getJSONArray("values");
            if (traceMode) {
                context.getLogger().info("json array : " + jsonArray.toString() + " and list of keys "+ key.toString());
            }
            return  NeoLoadRequestAttributes.getNeoLoadRequestAttributeEqualToKey(jsonArray,key);
        }
        
        return null;        
     }
*/    
     private boolean isNeoLoadRequestAttributesExists(String type) throws Exception {
//         Map<String, String> header = new HashMap<>();
//         DynatraceContext dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceAccountID, proxyName, Optional.absent(), header);
         final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();

         JSONObject jsonobj=DynatraceUtils.executeDynatraceAPIGetObjectRequest(context, dynatracecontext, Api.CFG, DYNATRACE_REQUEST_ATTRIBUTE_API_URL, parameters, traceMode);
         if (jsonobj != null) {
             JSONArray jsonArray=jsonobj.getJSONArray("values");
             return  NeoLoadRequestAttributes.isNeoLoadRequestAttributesExists(jsonArray,type);
         }
        return false;
    }

    public void generateRequestAttributes(String type) throws Exception {
        if(!isNeoLoadRequestAttributesExists(type)) {
            createRequestAttributes(type);
        }
    }
    
   private static String newRequesAttributPayload(final String parametername,final String headersuffix,final String headername) {
       return String.format(REQUEST_ATTRIBUTE_PAYLOAD, parametername,headersuffix,headername);
   }
   
	private void createRequestAttributes(String type) {
		Map<String, String> attributeshashmap = NeoLoadRequestAttributes.generateHashMap(type);
		attributeshashmap.forEach((k, v) -> {
			try {
				createRequestAttribute(k, v, type);
			} catch (Exception e) {
				context.getLogger().error("Technical Error ", e);
			}
		});
	}

    private void createRequestAttribute(String parametername,String headersuffix,String type) throws Exception {
        String payload;
        if(type.equalsIgnoreCase(NeoLoadRequestAttributes.NEW))
            payload=newRequesAttributPayload(parametername,headersuffix,NeoLoadRequestAttributes.NEOLOAD_HTTP_HEADER_NAME_NEW);
        else
            payload=newRequesAttributPayload(parametername,headersuffix,NeoLoadRequestAttributes.NEOLOAD_HTTP_HEADER_NAME);
        
        DynatraceUtils.executeDynatraceAPIPostObjectRequest(context, dynatracecontext, Api.CFG, DYNATRACE_REQUEST_ATTRIBUTE_API_URL, payload, traceMode);
/*
        final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatraceManagedHostname, dynatraceAccountID) + DYNATRACE_REQUEST_ATTRIBUTE_API_URL;
        final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();

        sendTokenIngetParam(parameters);
        try {
            final Optional<Proxy> proxy = getProxy(proxyName, url);


            httpGenerator = new HTTPGenerator(HTTP_POST_METHOD, url, headers, parameters, proxy,payload);

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
*/
    }

}
