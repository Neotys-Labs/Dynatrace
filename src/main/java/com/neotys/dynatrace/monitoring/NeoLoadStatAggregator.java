package com.neotys.dynatrace.monitoring;


import com.google.common.base.Optional;
import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.dynatrace.common.HTTPGenerator;
import com.neotys.dynatrace.common.HttpResponseUtils;
import com.neotys.dynatrace.monitoring.neoloadmetrics.DynatraceCustomMetric;
import com.neotys.dynatrace.monitoring.neoloadmetrics.NeoLoadDynatraceCustomMetrics;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.model.TestStatistics;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static com.neotys.dynatrace.common.HTTPGenerator.*;


public class NeoLoadStatAggregator extends TimerTask implements DynatraceMonitoringApi {

    private static final String DYNATRACE_URL = ".live.dynatrace.com/api/v1/";
    private static final String DYNATRACE_TIME_SERIES_CREATION = "timeseries/custom";
    private static final String NL_TIMESERIES_PREFIX = "neoload.";
    private static final String DYNATRACE_NEW_DATA = "entity/infrastructure/custom/";
    private static final String DYNATRACE_TIME_SERIES = "timeseries";
    private static final String HTTPS = "https://";
    private static final String NEOLOAD_SAAS_NEOTYS_COM = "neoload.saas.neotys.com";
    private static final String NEOLOAD_URL_LAST = "/#!result/overview/?benchId=";
    private static final String NL_PICTURE_URL = "http://www.neotys.com/wp-content/uploads/2017/07/Neotys-Emblem-Primary.png";
    private static final String NEOLOAD_TYPE = "NeoLoad";

    private static final int MIN_DYNATRACE_DURATION = 30;

    private final Optional<String> proxyName;
    private final Context context;

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
    private long lastDuration = 0;

    public NeoLoadStatAggregator(final String dynatraceApiKey,
                                 final String dynatraceAccountId,
                                 final ResultsApi nlWebResult,
                                 final Context context,
                                 final String dataExchangeApiUrl,
                                 final Optional<String> dynatraceManagedHostName,
                                 final Optional<String> proxyName) {
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

    private void runTask() throws Exception {
        TestStatistics statsResult;
        long utc = System.currentTimeMillis() / 1000;

        if (lastDuration == 0 || (utc - lastDuration) >= MIN_DYNATRACE_DURATION) {
            //Get stats from nlweb
            statsResult = nlWebResult.getTestStatistics(testId);
            if (statsResult != null) {
                lastDuration = System.currentTimeMillis() / 1000;

                //Update metrics to send
                NeoLoadDynatraceCustomMetrics.updateTimeseriesToSend(statsResult);

                if (!timeSeriesConfigured) {
                    //Check if metric are created
                    if (!hasCustomMetric(NeoLoadDynatraceCustomMetrics.getTimeseriesToSend().get(NeoLoadDynatraceCustomMetrics.REQUEST_COUNT))) {
                        for (DynatraceCustomMetric dynatraceTimeseries : NeoLoadDynatraceCustomMetrics.getTimeseriesToSend().values()) {
                            //Create Metric
                            registerCustomMetric(dynatraceTimeseries);
                        }
                    }
                    timeSeriesConfigured = true;
                }

                //Report activity
                reportCustomMetrics(new ArrayList(NeoLoadDynatraceCustomMetrics.getTimeseriesToSend().values()));
            } else {
                context.getLogger().debug("No stats found in NeoLoad web API.");
            }
        }
    }


    private long getUtcDate() {
        long timeInMillisSinceEpoch123 = System.currentTimeMillis();
        timeInMillisSinceEpoch123 -= 200000;
        return timeInMillisSinceEpoch123;
    }

    private String getApiUrl() {
        if (dynatraceManagedHostName.isPresent()) {
            return HTTPS + dynatraceManagedHostName.get() + "/api/v1/";
        } else {
            return HTTPS + dynatraceAccountId + DYNATRACE_URL;
        }
    }

    private String getNlUrl() {
        // TODO get nl web front URL from context.
        return HTTPS + NEOLOAD_SAAS_NEOTYS_COM + NEOLOAD_URL_LAST;
    }


    @Override
    public void run() {
        try {
            runTask();
        } catch (final Exception e) {
            context.getLogger().error("Error while sending stats to Dynatrace", e);
        }
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
        final String url = getApiUrl() + DYNATRACE_TIME_SERIES_CREATION + ":" + timeSeriesName;
        parameters.put("Api-Token", dynatraceApiKey);

        final String jsonString = "{\"displayName\":\"" + dynatraceCustomMetric.getDisplayName() + "\","
                + "\"unit\":\"" + dynatraceCustomMetric.getUnit() + "\","
                + "\"dimensions\": [\"Neoload\"],"
                + "\"types\":[\"" + dynatraceCustomMetric.getTypes().get(0) + "\"]}";

        final Optional<Proxy> proxy = getProxy(proxyName, url);
        final HTTPGenerator insightHttp = HTTPGenerator.newJsonHttpGenerator(HTTP_PUT_METHOD, url, head, parameters, proxy, jsonString);

        context.getLogger().debug("dynatrace register custom metric JSON content : " + jsonString);

        try {
            context.getLogger().debug("Dynatrace service : register custom metric");
            int httpCode = insightHttp.executeAndGetResponseCode();
            if (httpCode == HttpStatus.SC_CREATED) {
                dynatraceCustomMetric.setCreated(true);
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

        parameters.put("Api-Token", dynatraceApiKey);

        String url = getApiUrl() + DYNATRACE_NEW_DATA + "NeoLoadData";
        long time = System.currentTimeMillis();

        String jsonString = "{\"displayName\" : \"NeoLoad Data\","
                + "\"ipAddresses\" : [\"" + componentIpAdresse + "\"],"
                + "\"listenPorts\" : [\"" + componentPort + "\"],"
                + "\"type\" : \"" + NEOLOAD_TYPE + "\","
                + "\"favicon\" : \"" + NL_PICTURE_URL + "\","
                + "\"configUrl\" : \"" + getNlUrl() + testId + "\","
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


                jsonString += conStr + ",";
                hasMetrics = true;
            }
        }

        if (",".equalsIgnoreCase(jsonString.substring(jsonString.length() - 1))) {
            jsonString = jsonString.substring(0, jsonString.length() - 1);
        }

        jsonString += "]}";

        if (hasMetrics) {

            final Optional<Proxy> proxy = getProxy(proxyName, url);
            insightHttp = HTTPGenerator.newJsonHttpGenerator(HTTP_POST_METHOD, url, head, parameters, proxy, jsonString);

            context.getLogger().debug("dynatrace report custom metric JSON content : " + jsonString);

            StatusLine statusLine;
            try {
                context.getLogger().debug("Dynatrace service : report custom metric");
                statusLine = insightHttp.executeAndGetStatusLine();
            } finally {
                insightHttp.closeHttpClient();
            }

            if (statusLine != null && !HttpResponseUtils.isSuccessHttpCode(statusLine.getStatusCode())) {
                throw new DynatraceException(statusLine.getReasonPhrase());
            }
        }
    }

    @Override
    public boolean hasCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws Exception {
        final String url = getApiUrl() + DYNATRACE_TIME_SERIES;
        final Map<String, String> header = new HashMap<>();
        final Map<String, String> parameters = new HashMap<>();
        final String timeSeriesName = dynatraceCustomMetric.getDimensions().get(0);
        parameters.put("Api-Token", dynatraceApiKey);
        parameters.put("timeseriesId", NL_TIMESERIES_PREFIX + ":" + timeSeriesName);
        parameters.put("startTimestamp", String.valueOf(getUtcDate()));
        parameters.put("endTimestamp", String.valueOf(System.currentTimeMillis()));

        final Optional<Proxy> proxy = getProxy(proxyName, url);
        HTTPGenerator httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, header, parameters, proxy);

        int httpCode;
        try {
            context.getLogger().debug("Dynatrace service : has custom metric");
            httpCode = httpGenerator.executeAndGetResponseCode();
        } finally {
            httpGenerator.closeHttpClient();
        }

        return HttpResponseUtils.isSuccessHttpCode(httpCode);
    }
}

