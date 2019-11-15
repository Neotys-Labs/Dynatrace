package com.neotys.dynatrace.anomalieDetection;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.*;
import com.neotys.dynatrace.common.tag.DynatraceTaggingUtils;
import com.neotys.extensions.action.engine.Context;
import org.json.JSONObject;
import java.util.*;

import static com.neotys.dynatrace.common.Constants.*;

public class NeoLoadAnomalieDetectionApi {

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


    private final Context context;
    private final DynatraceContext dynatracecontext; 
    private boolean traceMode;

    public NeoLoadAnomalieDetectionApi(String dynatraceApiKey, String dynatraceAccountID, Optional<String> dynatraceManagedHostname, Optional<String> proxyName, Context context, boolean traceMode) {
    	
    	this.dynatracecontext=new DynatraceContext(dynatraceApiKey,dynatraceManagedHostname,dynatraceAccountID,proxyName,Optional.absent());
        this.context = context;
        this.traceMode = traceMode;
    }

    public String createAnomalie(String dynatracemetricname,String operator,String typeofAlert,String value,Optional<String> tags) throws Exception {
        String jsonpayload=String.format(JSONPAYLOAD,dynatracemetricname,dynatracemetricname,dynatracemetricname,dynatracemetricname,typeofAlert,operator,value);
        String tagfilter=DynatraceTaggingUtils.convertIntoDynatraceContextTag(tags);
        if(tagfilter!=null) {
            String payload=jsonpayload+tagfilter+ENDPAYLOAD;
                    	
            JSONObject jsonObject= DynatraceUtils.executeDynatraceAPIPostObjectRequest(context,dynatracecontext,DTAPI_CFG_EP_ANOMALIE_METRIC,payload,traceMode);
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

	public void deleteAnomalieDetectionfromIds(List<String> anomalieIdlist) {
		anomalieIdlist.stream().forEach(id -> {
			try {
		    	DynatraceUtils.executeDynatraceAPIDeleteRequest(context, dynatracecontext, DTAPI_CFG_EP_ANOMALIE_METRIC + "/" +id, traceMode);
			} catch (Exception e) {
				context.getLogger().error("Error while deleting anomalie id :" + id, e);
			}
		});
	}
}