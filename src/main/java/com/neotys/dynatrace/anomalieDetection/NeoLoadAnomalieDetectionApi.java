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

public class NeoLoadAnomalieDetectionApi {


    private final static String DYNATRACE_ANOMALIE_URL = "anomalyDetection/metricEvents/";

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


    private final Map<String, String> headers;
    private final String dynatraceApiKey;
    private final String dynatraceAccountID;
    private final Optional<String> dynatraceManagedHostname;
    private final Optional<String> proxyName;
    private final Context context;
    private boolean traceMode;
    private HTTPGenerator httpGenerator;

    public NeoLoadAnomalieDetectionApi(String dynatraceApiKey, String dynatraceAccountID, Optional<String> dynatraceManagedHostname, Optional<String> proxyName, Context context, boolean traceMode) {
        this.dynatraceApiKey = dynatraceApiKey;
        this.dynatraceAccountID = dynatraceAccountID;
        this.dynatraceManagedHostname = dynatraceManagedHostname;
        this.proxyName = proxyName;
        this.context = context;
        this.traceMode = traceMode;
        this.headers = new HashMap<>();
        DynatraceUtils.generateHeaders(headers);
    }

    private Optional<String> getDynatracetag(Optional<String> tag)
    {
        Optional<String> result;
        if(tag.isPresent()) {
            result = Optional.of(tag.get().replaceAll(":", ":NeoLoad-"));
            if(!result.get().contains(":"))
                result=Optional.of("NeoLoad-"+tag.get());

            if(result.get().startsWith("["))
            {
                String[] tagcontext=result.get().split("]");
                if(tagcontext.length>1)
                {
                    result=Optional.of(tagcontext[1]);
                }
            }

            if(result.get().contains(":"))
            {
                String[] tagkey=result.get().split(":");
                if(tagkey.length>1)
                {
                    result=Optional.of(tagkey[1]);
                }
            }
        }
        else
            result=Optional.absent();

        return result;
    }
    public String createAnomalie(String dynatracemetricname,String operator,String typeofAlert,String value,Optional<String> tags) throws Exception {
        final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatraceManagedHostname, dynatraceAccountID) + DYNATRACE_ANOMALIE_URL;
        final Map<String, String> parameters = new HashMap<>();

        parameters.put("Api-Token", dynatraceApiKey);

        try
        {
            final Optional<Proxy> proxy = DynatraceUtils.getProxy(context, proxyName, url);
            String jsonpayload=String.format(JSONPAYLOAD,dynatracemetricname,dynatracemetricname,dynatracemetricname,dynatracemetricname,typeofAlert,operator,value);
            //-----add tags---------------
            String tagfilter=generateTagFilterString(getDynatracetag(tags));
            if(tagfilter!=null)
            {
                String payload=jsonpayload+tagfilter+ENDPAYLOAD;
                httpGenerator=HTTPGenerator.newJsonHttpGenerator(HTTPGenerator.HTTP_POST_METHOD,url,headers,parameters,proxy,payload);
                if (traceMode) {
                    context.getLogger().info("Dynatrace anomalie detection, post anomalie detection:\n" + httpGenerator.getRequest());
                }
                HttpResponse httpResponse = httpGenerator.execute();
                final int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode== HttpStatus.SC_CREATED)
                {
                    if(traceMode)
                        context.getLogger().info("Anomalie Detection  properly created");

                    JSONObject jsonObject= HttpResponseUtils.getJsonResponse(httpResponse);
                    if(jsonObject!=null)
                    {
                        String anomalieid=jsonObject.getString("id");
                        return anomalieid;
                    }
                    else {
                        context.getLogger().error("Unable to parse the anoamlie response API "+ payload);
                        throw new DynatraceException("Unable to parse Anomalie api Response : "+httpResponse.toString());

                    }
                }
                else
                {
                    context.getLogger().error("Unable to create Anomalie Detection "+ payload);
                    throw new DynatraceException("Unable to create Anomalie Detection");

                }
            }
        }
        finally{
            if(httpGenerator!=null)
                httpGenerator.closeHttpClient();


        }
        return null;
    }

    private void deleteAnoalieDetectionFromId(String id) throws Exception {
        final String url = DynatraceUtils.getDynatraceConfigApiUrl(dynatraceManagedHostname, dynatraceAccountID) + DYNATRACE_ANOMALIE_URL  +id;
        final Map<String, String> parameters = new HashMap<>();

        parameters.put("Api-Token", dynatraceApiKey);

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
    }

    public void deleteAnomalieDetectionfromIds(List<String> anomalieIdlist)
    {
        anomalieIdlist.stream().forEach(id->{
                try
                    {

                        deleteAnoalieDetectionFromId(id);
                    }
                    catch(Exception e)
                    {
                        context.getLogger().error("Error during deleting anomalie id :"+id, e);
                    }
        });
    }

    private String generateTagFilterString(Optional<String> tag)
    {
       return DynatraceTaggingUtils.convertIntoDynatraceContextTag(tag);
    }


}