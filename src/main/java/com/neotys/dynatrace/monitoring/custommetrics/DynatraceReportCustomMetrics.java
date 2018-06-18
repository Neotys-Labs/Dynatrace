package com.neotys.dynatrace.monitoring.custommetrics;


import com.google.common.base.Optional;
import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.dynatrace.common.DynatraceUtils;
import com.neotys.dynatrace.common.HTTPGenerator;
import com.neotys.dynatrace.common.HttpResponseUtils;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.model.TestStatistics;
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

import static com.neotys.dynatrace.common.HTTPGenerator.*;


public class DynatraceReportCustomMetrics implements DynatraceMonitoringApi {

    private static final String DYNATRACE_TIME_SERIES_CREATION = "timeseries/custom";
    private static final String NL_TIMESERIES_PREFIX = "neoload.";
    private static final String DYNATRACE_NEW_DATA = "entity/infrastructure/custom/";
    private static final String DYNATRACE_TIME_SERIES = "timeseries";
    private static final String NL_PICTURE_URL = "http://www.neotys.com/wp-content/uploads/2017/07/Neotys-Emblem-Primary.png";
    private static final String NEOLOAD_TYPE = "NeoLoad";
    private static final String API_TOKEN = "Api-Token";

    private static final int MIN_DYNATRACE_DURATION = 30;

    private final Optional<String> proxyName;

    private Context context;

    private ResultsApi nlWebResult;

    private String componentIpAdresse;
    private int componentPort;
    private String dynatraceApiKey;

    private String dynatraceAccountId;
    private String testName;
    private final String testId;
    private String scenarioName;
    private Optional<String> dynatraceManagedHostName;
    private String dataExchangeApiUrl;
    private boolean timeSeriesConfigured = false;
    private boolean traceMode;
    public DynatraceReportCustomMetrics(final String dynatraceApiKey,
                                        final String dynatraceAccountId,
                                        final ResultsApi nlWebResult,
                                        final Context context,
                                        final String dataExchangeApiUrl,
                                        final Optional<String> dynatraceManagedHostName,
                                        final Optional<String> proxyName, final boolean traceMode) {
        this.proxyName = proxyName;
        this.dynatraceApiKey = dynatraceApiKey;
        this.context = context;
        this.testId = context.getTestId();
        this.testName = context.getTestName();
        this.nlWebResult = nlWebResult;
        this.dynatraceManagedHostName = dynatraceManagedHostName;
        this.dynatraceAccountId = dynatraceAccountId;
        this.scenarioName = context.getScenarioName();
        this.dataExchangeApiUrl = dataExchangeApiUrl;
        this.traceMode = traceMode;

        initComponentAdresse();
    }

    private void initComponentAdresse() {
        URI uri = URI.create(dataExchangeApiUrl);
        componentIpAdresse = uri.getHost();
        if ("localhost".equalsIgnoreCase(componentIpAdresse)) {
            componentIpAdresse = "127.0.0.1";
        }
        componentPort = uri.getPort();
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
                //Check if metric are created
                if (!hasCustomMetric(NeoLoadMetrics.getTimeseriesToSend().get(NeoLoadMetrics.REQUEST_COUNT))) {
                    for (DynatraceCustomMetric dynatraceTimeseries : NeoLoadMetrics.getTimeseriesToSend().values()) {
                        //Create Metric
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

    private long getUtcDate() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return now.toInstant().toEpochMilli() - 200000;
    }

    private Optional<Proxy> getProxy(final Optional<String> proxyName, final String url) throws MalformedURLException {
        if (proxyName.isPresent()) {
            return Optional.fromNullable(context.getProxyByName(proxyName.get(), new URL(url)));
        }
        return Optional.absent();
    }

    @Override
    public void registerCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws Exception {
        final Map<String, String> head = new HashMap<>();
        final Map<String, String> parameters = new HashMap<>();
        final String timeSeriesName = dynatraceCustomMetric.getDimensions().get(0);
        final String url = DynatraceUtils.getDynatraceApiUrl(dynatraceManagedHostName, dynatraceAccountId) + DYNATRACE_TIME_SERIES_CREATION + ":" + timeSeriesName;
        parameters.put(API_TOKEN, dynatraceApiKey);

        final String bodyJson = "{\"displayName\":\"" + dynatraceCustomMetric.getDisplayName() + "\","
                + "\"unit\":\"" + dynatraceCustomMetric.getUnit() + "\","
                + "\"dimensions\": [\"Neoload\"],"
                + "\"types\":[\"" + dynatraceCustomMetric.getTypes().get(0) + "\"]}";

        final Optional<Proxy> proxy = getProxy(proxyName, url);
        final HTTPGenerator insightHttp = HTTPGenerator.newJsonHttpGenerator(HTTP_PUT_METHOD, url, head, parameters, proxy, bodyJson);

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
    }


    @Override
    public void reportCustomMetrics(final List<DynatraceCustomMetric> dynatraceCustomMetrics) throws Exception {
        final Map<String, String> head = new HashMap<>();
        final Map<String, String> parameters = new HashMap<>();
        HTTPGenerator insightHttp;

        parameters.put(API_TOKEN, dynatraceApiKey);

        String url = DynatraceUtils.getDynatraceApiUrl(dynatraceManagedHostName, dynatraceAccountId) + DYNATRACE_NEW_DATA + "NeoLoadData";

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        long time = now.toInstant().toEpochMilli();


        String bodyJson = "{\"displayName\" : \"NeoLoad Data\","
                + "\"ipAddresses\" : [\"" + componentIpAdresse + "\"],"
                + "\"listenPorts\" : [\"" + componentPort + "\"],"
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

            final Optional<Proxy> proxy = getProxy(proxyName, url);
            insightHttp = HTTPGenerator.newJsonHttpGenerator(HTTP_POST_METHOD, url, head, parameters, proxy, bodyJson);

            HttpResponse httpResponse;
            try {
                if(traceMode){
                    context.getLogger().info("Dynatrace service, report custom metric:\n" + insightHttp.getRequest() + "\n" + bodyJson);
                }

                httpResponse = insightHttp.execute();
            } finally {
                insightHttp.closeHttpClient();
            }

            if (httpResponse != null && !HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode())) {
                final String stringResponse = HttpResponseUtils.getStringResponse(httpResponse);
                throw new DynatraceException(httpResponse.getStatusLine().getReasonPhrase() + " - "+ url + " - "+ bodyJson + " - " + stringResponse);
            }
        }
    }

    @Override
    public boolean hasCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws Exception {
        final String url = DynatraceUtils.getDynatraceApiUrl(dynatraceManagedHostName, dynatraceAccountId) + DYNATRACE_TIME_SERIES;
        final Map<String, String> header = new HashMap<>();
        final Map<String, String> parameters = new HashMap<>();
        final String timeSeriesName = dynatraceCustomMetric.getDimensions().get(0);
        parameters.put(API_TOKEN, dynatraceApiKey);
        parameters.put("timeseriesId", NL_TIMESERIES_PREFIX + ":" + timeSeriesName);
        parameters.put("startTimestamp", String.valueOf(getUtcDate()));
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        parameters.put("endTimestamp", String.valueOf(now.toInstant().toEpochMilli()));

        final Optional<Proxy> proxy = getProxy(proxyName, url);
        HTTPGenerator httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, header, parameters, proxy);

        HttpResponse httpResponse;
        try {
            if(traceMode){
                context.getLogger().info("Dynatrace service, has custom metric:\n" + httpGenerator.getRequest());
            }
            httpResponse = httpGenerator.execute();

            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                context.getLogger().error(httpResponse.toString());
            }
        } finally {
            httpGenerator.closeHttpClient();
        }

        return HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode());
    }


    public void setContext(final Context context) {
        this.context = context;
    }
}

