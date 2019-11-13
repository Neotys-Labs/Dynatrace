package com.neotys.dynatrace.monitoring.custommetrics;


import com.google.common.base.Optional;
import com.neotys.ascode.swagger.client.api.ResultsApi;
import com.neotys.ascode.swagger.client.model.TestStatistics;
import com.neotys.dynatrace.common.Api;
import com.neotys.dynatrace.common.DynatraceContext;
import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.dynatrace.common.DynatraceUtils;
import com.neotys.dynatrace.common.HTTPGenerator;
import com.neotys.dynatrace.common.HttpResponseUtils;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static com.neotys.dynatrace.common.HTTPGenerator.*;


public class DynatraceReportCustomMetrics implements DynatraceMonitoringApi {

    private static final String DYNATRACE_TIME_SERIES_CREATION = "timeseries/custom";
    private static final String NL_TIMESERIES_PREFIX = "custom";
    private static final String DYNATRACE_NEW_DATA = "entity/infrastructure/custom/";
    private static final String DYNATRACE_TIME_SERIES = "timeseries";
    private static final String NL_PICTURE_URL = "http://www.neotys.com/wp-content/uploads/2017/07/Neotys-Emblem-Primary.png";
    private static final String NEOLOAD_TYPE = "NeoLoad";
    private static final String API_TOKEN = "Api-Token";

//    private final Optional<String> proxyName;
    private Context context;
    private DynatraceContext dynatracecontext;
    private ResultsApi nlWebResult;
//    private String dynatraceApiKey;
//    private String dynatraceAccountId;
    private String testName;
    private final String testId;
    private String scenarioName;
//    private Optional<String> dynatraceManagedHostName;
    private boolean timeSeriesConfigured = false;
    private boolean traceMode;
    
    
    public DynatraceReportCustomMetrics(final String dynatraceApiKey,
                                        final String dynatraceAccountId,
                                        final ResultsApi nlWebResult,
                                        final Context context,
                                        final Optional<String> dynatraceManagedHostName,
                                        final Optional<String> proxyName, final boolean traceMode) {
    	
    	this.dynatracecontext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostName, dynatraceAccountId, proxyName, Optional.absent(), new HashMap<String,String>());
    	
//        this.proxyName = proxyName;
//        this.dynatraceApiKey = dynatraceApiKey;
        this.context = context;
        this.testId = context.getTestId();
        this.testName = context.getTestName();
        this.nlWebResult = nlWebResult;
//        this.dynatraceManagedHostName = dynatraceManagedHostName;
//        this.dynatraceAccountId = dynatraceAccountId;
        this.scenarioName = context.getScenarioName();
        this.traceMode = traceMode;
    }

    public void run() {
        try {
            runTask();
        } catch (final Exception e) {
            context.getLogger().error("Error while sending stats to Dynatrace", e);
        }
    }

    private void runTask() throws Exception {
        TestStatistics statsResult;
        //Get stats from nlweb

        statsResult = nlWebResult.getTestStatistics(testId);
        if (statsResult != null) {
            //Update metrics to send
            NeoLoadMetrics.updateTimeseriesToSend(statsResult);

            if (!timeSeriesConfigured) {
                for (DynatraceCustomMetric dynatraceTimeseries : NeoLoadMetrics.getTimeseriesToSend().values()) {
                    if (hasCustomMetric(dynatraceTimeseries)) {
                    	dynatraceTimeseries.setCreated(true);
                    } else {
                        registerCustomMetric(dynatraceTimeseries);
                    }
                }            	
                timeSeriesConfigured = true;
            }

            //Report activity
            reportCustomMetrics(new ArrayList(NeoLoadMetrics.getTimeseriesToSend().values()));
        } else {
            if(traceMode){
                context.getLogger().info("No stats found in NeoLoad web API.");
            }
        }
    }
/*
    private long getUtcDate() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return now.toInstant().toEpochMilli() - 200000;
    }
*/
/*    
    private Optional<Proxy> getProxy(final Optional<String> proxyName, final String url) throws MalformedURLException {
        if (proxyName.isPresent()) {
            return Optional.fromNullable(context.getProxyByName(proxyName.get(), new URL(url)));
        }
        return Optional.absent();
    }
*/
    
    @Override
    public void registerCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws Exception {
/*    	
        final Map<String, String> head = new HashMap<>();
        final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        final String url = DynatraceUtils.getDynatraceEnv1ApiUrl(dynatraceManagedHostName, dynatraceAccountId) + DYNATRACE_TIME_SERIES_CREATION + ":" + timeSeriesName;
        parameters.add(API_TOKEN, dynatraceApiKey);
*/
        final String timeSeriesName = dynatraceCustomMetric.getDimensions().get(0);
        final String bodyJson = "{\"displayName\":\"" + dynatraceCustomMetric.getDisplayName() + "\","
                + "\"unit\":\"" + dynatraceCustomMetric.getUnit() + "\","
                + "\"dimensions\": [\"Neoload\"],"
                + "\"types\":[\"" + dynatraceCustomMetric.getTypes().get(0) + "\"]}";

        
        DynatraceUtils.executeDynatraceAPIPutRequest(context, dynatracecontext, Api.ENV1, DYNATRACE_TIME_SERIES_CREATION + ":" + timeSeriesName, bodyJson, traceMode);
        // if no exception is raised, assume success
        dynatraceCustomMetric.setCreated(true);
        
/*        
        final Optional<Proxy> proxy = getProxy(proxyName, url);
        final HTTPGenerator insightHttp = new HTTPGenerator(HTTP_PUT_METHOD, url, head, parameters, proxy, bodyJson);

        try {
            if(traceMode){
                context.getLogger().info("Dynatrace service, register custom metric:\n" + insightHttp.getRequest() + "\n" + bodyJson);
            }

            HttpResponse httpResponse = insightHttp.execute();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                dynatraceCustomMetric.setCreated(true);
            }else {
                context.getLogger().error(httpResponse.toString());
            }
        } finally {
            insightHttp.closeHttpClient();
        }
*/        
    }


    @Override
    public void reportCustomMetrics(final List<DynatraceCustomMetric> dynatraceCustomMetrics) throws Exception {
/*  
    	final Map<String, String> head = new HashMap<>();
        final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        HTTPGenerator insightHttp;

        parameters.add(API_TOKEN, dynatraceApiKey);

        String url = DynatraceUtils.getDynatraceEnv1ApiUrl(dynatraceManagedHostName, dynatraceAccountId) + DYNATRACE_NEW_DATA + "NeoLoadData";
*/
    	
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        long time = now.toInstant().toEpochMilli();


        String bodyJson = "{\"displayName\" : \"NeoLoad Data\","
                + "\"type\" : \"" + NEOLOAD_TYPE + "\","
                + "\"favicon\" : \"" + NL_PICTURE_URL + "\","
                + "\"configUrl\" : \"" + context.getWebPlatformRunningTestUrl() + "\","
                + "\"tags\": [\"Loadtest\", \"NeoLoad\"],"
                + "\"properties\" : { \"TestName\" : \"" + testName + "\" ,\"ScenarioName\" : \"" + scenarioName + "\"  },"
                + "\"series\" : [";


        boolean hasMetrics = false;
        for (DynatraceCustomMetric dynatraceCustomMetric : dynatraceCustomMetrics) {
            if (dynatraceCustomMetric.isCreated() && dynatraceCustomMetric.isValued()) {
                String conStr = "{"
                        + "\"timeseriesId\" : \"custom:" + dynatraceCustomMetric.getDimensions().get(0) + "\","
                        + "\"dimensions\" : { \"Neoload\" : \"" + dynatraceCustomMetric.getDisplayName() + "\"  },"
                        + "\"dataPoints\" : [ [" + String.valueOf(time) + "  , " + dynatraceCustomMetric.getValue() + " ] ]"
                        + "}";


                bodyJson += conStr + ",";
                hasMetrics = true;
            }
        }

        if (",".equalsIgnoreCase(bodyJson.substring(bodyJson.length() - 1))) {
            bodyJson = bodyJson.substring(0, bodyJson.length() - 1);
        }

        bodyJson += "]}";

        if (hasMetrics) {
        	
        	DynatraceUtils.executeDynatraceAPIPostObjectRequest(context, dynatracecontext, Api.ENV1, DYNATRACE_NEW_DATA + "NeoLoadData", bodyJson, traceMode);
/*
            final Optional<Proxy> proxy = getProxy(proxyName, url);
            insightHttp = new HTTPGenerator(HTTP_POST_METHOD, url, head, parameters, proxy, bodyJson);

            HttpResponse httpResponse;
            try {
                if(traceMode){
                    context.getLogger().info("Dynatrace service, report custom metric:\n" + insightHttp.getRequest() + "\n" + bodyJson);
                }

                httpResponse = insightHttp.execute();

                String stringResponse = HttpResponseUtils.getStringResponse(httpResponse);
                if (!HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode())) {
                    throw new DynatraceException(httpResponse.getStatusLine().getReasonPhrase() + " - "+ url + " - "+ bodyJson + " - " + stringResponse);
                }
            } finally {
                insightHttp.closeHttpClient();
            }
*/            
        }
    }

    @Override
    public boolean hasCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws Exception {
/*    	
        final String url = DynatraceUtils.getDynatraceEnv1ApiUrl(dynatraceManagedHostName, dynatraceAccountId) + DYNATRACE_TIME_SERIES;
        final Map<String, String> header = new HashMap<>();
*/
        final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        final String timeSeriesName = dynatraceCustomMetric.getDimensions().get(0);
//        parameters.add(API_TOKEN, dynatraceApiKey);
//        parameters.add("timeseriesId", NL_TIMESERIES_PREFIX + ":" + timeSeriesName);
//        parameters.add("startTimestamp", String.valueOf(getUtcDate()));
//        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
//        parameters.add("endTimestamp", String.valueOf(now.toInstant().toEpochMilli()));

        try {
        	DynatraceUtils.executeDynatraceAPIGetObjectRequest(context, dynatracecontext, Api.ENV1, DYNATRACE_TIME_SERIES+"/custom:"+timeSeriesName, parameters, traceMode);
        } catch (Exception e) {
        	return false;
        }
 /*       
        final Optional<Proxy> proxy = getProxy(proxyName, url);
        HTTPGenerator httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, header, parameters, proxy);

        HttpResponse httpResponse;
        try {
            if(traceMode){
                context.getLogger().info("Dynatrace service, has custom metric:\n" + httpGenerator.getRequest());
            }
            httpResponse = httpGenerator.execute();

	        int statusCode = httpResponse.getStatusLine().getStatusCode();
	        if (statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_NOT_FOUND) {
                context.getLogger().error("Unexpected error while getting timeseries - " + httpResponse.toString());
            }
        } finally {
            httpGenerator.closeHttpClient();
        }
*/
//      # assume true if no Exception was thrown
//        return HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode());
        return true;
    }


    public void setContext(final Context context) {
        this.context = context;
    }
}

