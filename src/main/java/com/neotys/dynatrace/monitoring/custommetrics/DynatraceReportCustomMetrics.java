package com.neotys.dynatrace.monitoring.custommetrics;


import com.google.common.base.Optional;
import com.neotys.ascode.swagger.client.api.ResultsApi;
import com.neotys.ascode.swagger.client.model.TestStatistics;
import com.neotys.dynatrace.common.DynatraceContext;
import com.neotys.dynatrace.common.DynatraceUtils;
import com.neotys.extensions.action.engine.Context;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import static com.neotys.dynatrace.common.Constants.*;


public class DynatraceReportCustomMetrics implements DynatraceMonitoringApi {

    private static final String NL_PICTURE_URL = "http://www.neotys.com/wp-content/uploads/2017/07/Neotys-Emblem-Primary.png";
    private static final String NEOLOAD_TYPE = "NeoLoad";

    private Context context;
    private DynatraceContext dynatracecontext;
    private ResultsApi nlWebResult;
    private String testName;
    private final String testId;
    private String scenarioName;
    private boolean timeSeriesConfigured = false;
    private boolean traceMode;
        
    public DynatraceReportCustomMetrics(final String dynatraceApiKey,
                                        final String dynatraceAccountId,
                                        final ResultsApi nlWebResult,
                                        final Context context,
                                        final Optional<String> dynatraceManagedHostName,
                                        final Optional<String> proxyName, final boolean traceMode) {
    	
    	this.dynatracecontext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostName, dynatraceAccountId, proxyName, Optional.absent());
    	
        this.context = context;
        this.testId = context.getTestId();
        this.testName = context.getTestName();
        this.nlWebResult = nlWebResult;
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
    
    @Override
    public void registerCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws Exception {
        final String timeSeriesName = dynatraceCustomMetric.getDimensions().get(0);
        final String bodyJson = "{\"displayName\":\"" + dynatraceCustomMetric.getDisplayName() + "\","
                + "\"unit\":\"" + dynatraceCustomMetric.getUnit() + "\","
                + "\"dimensions\": [\"Neoload\"],"
                + "\"types\":[\"" + dynatraceCustomMetric.getTypes().get(0) + "\"]}";

        
        DynatraceUtils.executeDynatraceAPIPutRequest(context, dynatracecontext, DTAPI_ENV1_EP_TIMESERIES_CUSTOM + timeSeriesName, bodyJson, traceMode);
        // if no exception is raised, assume success
        dynatraceCustomMetric.setCreated(true);        
    }


    @Override
    public void reportCustomMetrics(final List<DynatraceCustomMetric> dynatraceCustomMetrics) throws Exception {
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
        	
        	DynatraceUtils.executeDynatraceAPIPostObjectRequest(context, dynatracecontext, DTAPI_ENV1_EP_CUSTOMDEVICE + "/NeoLoadData", bodyJson, traceMode);
        }
    }

    @Override
    public boolean hasCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws Exception {
        final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        final String timeSeriesName = dynatraceCustomMetric.getDimensions().get(0);

        try {
        	DynatraceUtils.executeDynatraceAPIGetObjectRequest(context, dynatracecontext, DTAPI_ENV1_EP_TIMESERIES_CUSTOM+timeSeriesName, parameters, traceMode);
        } catch (Exception e) {
        	return false;
        }
        return true;
    }


    public void setContext(final Context context) {
        this.context = context;
    }
}

