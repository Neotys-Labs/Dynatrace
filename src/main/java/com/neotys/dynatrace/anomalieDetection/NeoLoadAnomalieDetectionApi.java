package com.neotys.dynatrace.anomalieDetection;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.*;
import com.neotys.dynatrace.common.tag.DynatraceTaggingUtils;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class NeoLoadAnomalieDetectionApi {


    private final static String DYNATRACE_ANOMALIE_URL = "anomalyDetection/metricEvents";

    private final static String JSONPAYLOAD="{\n" +
            "  \"metricId\": \"%s\",\n" +
            "  \"displayName\": \"Neoload Anomalie on %s\",\n" +
            "  \"name\": \"Neoload Anomalie on %s\",\n" +
            "  \"description\": \"Neoload Threshold Validation for %s\",\n" +
            "  \"aggregationType\": \"AVG\",\n" +
            "  \"eventType\": \"%s\",\n" +
            "  \"alertCondition\": \"%s\",\n" +
            "  \"samples\": 3,\n" +
            "  \"violatingSamples\": 1,\n" +
            "  \"dealertingSamples\": 3,\n" +
            "  \"threshold\": %s,\n" +
            "  \"enabled\": true,\n" +
            "  \"tagFilters\": [\n" ;
    private final static String ENDPAYLOAD="]}";


//    private final Map<String, String> headers;
//    private final String dynatraceApiKey;
//    private final String dynatraceAccountID;
//    private final Optional<String> dynatraceManagedHostname;
//    private final Optional<String> proxyName;
    private final Context context;
    private final DynatraceContext dynatracecontext; 
    private boolean traceMode;
    private HTTPGenerator httpGenerator;

    public NeoLoadAnomalieDetectionApi(String dynatraceApiKey, String dynatraceAccountID, Optional<String> dynatraceManagedHostname, Optional<String> proxyName, Context context, boolean traceMode) {
    	
    	dynatracecontext=new DynatraceContext(dynatraceApiKey,dynatraceManagedHostname,dynatraceAccountID,proxyName,Optional.absent(),new HashMap<String,String>());
//        this.dynatraceApiKey = dynatraceApiKey;
//        this.dynatraceAccountID = dynatraceAccountID;
//        this.dynatraceManagedHostname = dynatraceManagedHostname;
//        this.proxyName = proxyName;
        this.context = context;
        this.traceMode = traceMode;
//        this.headers = new HashMap<>();
//        DynatraceUtils.generateHeaders(headers);
    }

    public String createAnomalie(String dynatracemetricname,String operator,String typeofAlert,String value,Optional<String> tags) throws Exception {
        String jsonpayload=String.format(JSONPAYLOAD,dynatracemetricname,dynatracemetricname,dynatracemetricname,dynatracemetricname,typeofAlert,operator,value);
        //String tagfilter=generateTagFilterString(tags);
        String tagfilter=DynatraceTaggingUtils.convertIntoDynatraceContextTag(tags);
        if(tagfilter!=null) {
            String payload=jsonpayload+tagfilter+ENDPAYLOAD;
                    	
            JSONObject jsonObject= DynatraceUtils.executeDynatraceAPIPostObjectRequest(context,dynatracecontext,Api.CFG,DYNATRACE_ANOMALIE_URL,payload,traceMode);
            if(jsonObject!=null) {
                String anomalieid=jsonObject.getString("id");
                return anomalieid;
            } else {
                context.getLogger().error("Unable to create Anomalie Detection "+ payload);
                throw new DynatraceException("Unable to create Anomalie Detection");
            }
        }    	
        return null;
    }

//    private void deleteAnomalieDetectionFromId(String id) throws Exception {
    	
//    	DynatraceUtils.executeDynatraceAPIDeleteRequest(context, dynatracecontext, Api.CFG, DYNATRACE_ANOMALIE_URL + "/" +id, traceMode);
    	
 /*   	
    	
        final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatracecontext.getDynatraceManagedHostname(), dynatraceAccountID) + DYNATRACE_ANOMALIE_URL + "/" +id;
        final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();

        parameters.add("Api-Token", dynatraceApiKey);

        try {
            final Optional<Proxy> proxy = DynatraceUtils.getProxy(context, proxyName, url);
            httpGenerator=HTTPGenerator.deleteHttpGenerator(url,headers,parameters,proxy);
            if (traceMode) {
                context.getLogger().info("Dynatrace anomalie detection, post anomalie detection:\n" + httpGenerator.getRequest());
            }
            HttpResponse httpResponse = httpGenerator.execute();
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode== HttpStatus.SC_NO_CONTENT) {
                if(traceMode)
                    context.getLogger().info("Anomalie Detection  properly deleted");

            }
            else
            {
                context.getLogger().error("Unable to delete the anoamlie detection with the id :"+id);
            }


        }
        finally{
            if(httpGenerator!=null)
                httpGenerator.closeHttpClient();


        }
*/        
//    }

	public void deleteAnomalieDetectionfromIds(List<String> anomalieIdlist) {
		anomalieIdlist.stream().forEach(id -> {
			try {
//				deleteAnomalieDetectionFromId(id);
		    	DynatraceUtils.executeDynatraceAPIDeleteRequest(context, dynatracecontext, Api.CFG, DYNATRACE_ANOMALIE_URL + "/" +id, traceMode);
			} catch (Exception e) {
				context.getLogger().error("Error during deleting anomalie id :" + id, e);
			}
		});
	}
	/*
    private String generateTagFilterString(Optional<String> tag)
    {
       return DynatraceTaggingUtils.convertIntoDynatraceContextTag(tag);
    }
*/

}