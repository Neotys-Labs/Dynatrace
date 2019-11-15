package com.neotys.dynatrace.configuration;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.*;
import com.neotys.dynatrace.common.topology.DynatraceTopologyWalker;
import com.neotys.extensions.action.engine.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static com.neotys.dynatrace.common.Constants.*;

public class DynatraceConfigurationAPI {

    private final Context context;
    private final DynatraceContext dynatracecontext;
    private boolean traceMode;

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
        this.context = context;
        this.traceMode = traceMode;
    }
    
    public void setDynatraceTags(Optional<String> tags) throws Exception {
        DynatraceTopologyWalker dtw=new DynatraceTopologyWalker(context,dynatracecontext,traceMode);
        dtw.executeDiscovery();
        
        dtw.getDiscoveredData().getServices().parallelStream().forEach(serviceid->{
            try {
                DynatraceUtils.updateTagOnServiceID(context,dynatracecontext,traceMode,serviceid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
        });
/* TODO : request for missing EP in Dynatrace slack channels
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
        if(!NeoLoadRequestNaming.existsNeoLoadNamingRule(context,dynatracecontext,traceMode,type)) {
            NeoLoadRequestNaming.createNeoLoadNamingRule(context, dynatracecontext, traceMode,type);
        }
    }

     private boolean isNeoLoadRequestAttributesExists(String type) throws Exception {
//         Map<String, String> header = new HashMap<>();
//         DynatraceContext dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceAccountID, proxyName, Optional.absent(), header);
         final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();

         JSONObject jsonobj=DynatraceUtils.executeDynatraceAPIGetObjectRequest(context, dynatracecontext, Api.CFG, DTAPI_CFG_EP_REQUEST_ATTRIBUTE, parameters, traceMode);
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
        
        DynatraceUtils.executeDynatraceAPIPostObjectRequest(context, dynatracecontext, Api.CFG, DTAPI_CFG_EP_REQUEST_ATTRIBUTE, payload, traceMode);
    }

}
