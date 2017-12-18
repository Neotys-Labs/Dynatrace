package com.neotys.dynatrace.monitoring;


import com.google.common.base.Optional;
import com.neotys.dynatrace.common.HTTPGenerator;
import io.swagger.client.ApiException;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.model.ElementValues;
import io.swagger.client.model.TestStatistics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_PUT_METHOD;


public class NeoLoadStatAggregator extends TimerTask {

    private final String DYNATRACE_API_URL = "events/";
    private static final String DYNATRACE_URL = ".live.dynatrace.com/api/v1/";
    private static final String DYNATRACE_APPLICATION = "entity/services";
    private static final String DYNATRACE_TIME_SERIES_CREATION = "timeseries/custom";
    private static final String NL_TIMESERIES_PREFIX = "neoload.";
    private static final String DYNATRACE_NEW_DATA = "entity/infrastructure/custom/";
    private static final String DYNATRACE_TIME_SERIES = "timeseries";
    private static final String HTTPS = "https://";
    private static final String NEOLOAD_SAAS_NEOTYS_COM = "neoload.saas.neotys.com";
    private static final String NEOLOAD_URL_LAST = "/#!result/overview/?benchId=";
    private static final String DYNATRACE_PROTOCOL = "https://";
    private static final String NEOLOADL_GUID = "com.neotys.NeoLoad.plugin";
    private static final String VERSION = "1.0.0";
    private static final String NL_PICTURE_URL = "http://www.neotys.com/wp-content/uploads/2017/07/Neotys-Emblem-Primary.png";
    private static final String NLTYPE = "NeoLoad";

    private static final int BAD_REQUEST = 400;
    private static final int UNAUTHORIZED = 403;
    private static final int NOT_FOUND = 404;
    private static final int METHOD_NOT_ALLOWED = 405;
    private static final int REQUEST_ENTITY_TOO_LARGE = 413;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAIBLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;
    private static final int HTTP_RESPONSE = 200;
    private static final int HTTP_RESPONSE_CREATED = 201;
    private static final int HTTP_RESPONSE_ALREADY = 200;
    private static final int MIN_NEW_RELIC_DURATION = 30;

    private NLGlobalStat neoLoadStat;
    private HTTPGenerator httpGenerator;
    private ResultsApi nlWebResult;

    private HashMap<String, String> headerMap = null;
    private String componentsName;
    private String dynatraceApiKey;
    private String dynatraceAccountId;
    private String testName;
    private final String testId;
    private String applicationEntityId;
    private String nlScenarioName;
    private String dynatraceManagedHostName;
    private String nlControllerHost;
    private boolean timeSeriesConfigured = false;
    private String nlInstance;

    private void initHttpClient() {
        headerMap = new HashMap<String, String>();
        //	headerMap.put("X-License-Key", NewRElicLicenseKey);
        //headerMap.put("Content-Type", "application/json");
        //headerMap.put("Accept","application/json");

    }

    public void addTokenIngetParam(final HashMap<String, String> param) {
        param.put("Api-Token", dynatraceApiKey);
    }

    public NeoLoadStatAggregator(final String dynatraceApiKey, final String componentName,
                                 final String dynatraceAccountId, final ResultsApi nlWebResult,
                                 final String testId, final NLGlobalStat neoLoadStat, final String scenarioName,
                                 final String testName, final String nlControllerHost, final Optional<String> dynatraceManagedHostName,
                                 final Optional<String> nlInstance) {
        componentsName = "Statistics";
        this.dynatraceApiKey = dynatraceApiKey;
        this.neoLoadStat = neoLoadStat;
        this.testId = testId;
        this.testName = testName;
        this.nlInstance = nlInstance.get();
        this.nlWebResult = nlWebResult;
        this.dynatraceManagedHostName = dynatraceManagedHostName.get();
        this.dynatraceAccountId = dynatraceAccountId;
        nlScenarioName = scenarioName;
        this.nlControllerHost = nlControllerHost;
        initHttpClient();
    }

    private void sendStatsToDynatrace() throws ApiException, DynatraceStatException, IOException, URISyntaxException {
        TestStatistics statsResult;
        long utc;
        long lastDuration;

        utc = System.currentTimeMillis() / 1000;
        lastDuration = neoLoadStat.getLasduration();

        if (lastDuration == 0 || (utc - lastDuration) >= MIN_NEW_RELIC_DURATION) {
            statsResult = nlWebResult.getTestStatistics(testId);
            if (statsResult != null) {
                lastDuration = sendData(statsResult, lastDuration);
                neoLoadStat.setLasduration(lastDuration);
            } else {
                System.out.println("stats est null");
            }
        }
    }

    private void updateRequestStats() throws ApiException {
        ElementValues requestValues = nlWebResult.getTestElementsValues(testId, "all-requests");
        neoLoadStat.UpdateRequestStat(requestValues, "REQUEST");

        ElementValues transactionsValues = nlWebResult.getTestElementsValues(testId, "all-transactions");
        neoLoadStat.UpdateRequestStat(transactionsValues, "TRANSACTION");
    }

    public long sendData(final TestStatistics stat, final long lasDuration)
            throws DynatraceStatException, IOException, ApiException, URISyntaxException {
        List<String[]> data;
        long utc;
        utc = System.currentTimeMillis() / 1000;

        if (neoLoadStat == null)
            neoLoadStat = new NLGlobalStat(stat);
        else {
            neoLoadStat.UpdateStat(stat);
        }
        updateRequestStats();
        data = neoLoadStat.GetNLData();

        if (!timeSeriesConfigured) {
            if (!isNlDataExists(data.get(0)[1])) {
                for (String[] metric : data)
                    createNlTimeSeries(metric[1], metric[0], NLTYPE, metric[2], neoLoadStat);

                timeSeriesConfigured = true;
            }
        }
        sendMetricToTimeSeriestApi(data, NLTYPE);

        return utc;
    }

    private long getUtcDate() {
        long timeInMillisSinceEpoch123 = System.currentTimeMillis();
        timeInMillisSinceEpoch123 -= 200000;
        return timeInMillisSinceEpoch123;
    }

    private boolean isNlDataExists(final String timeSeries) throws IOException, URISyntaxException {
        boolean results;
        int httpCode;
        String url = getApiUrl() + DYNATRACE_TIME_SERIES;
        HashMap<String, String> Parameters = new HashMap<String, String>();
        addTokenIngetParam(Parameters);
        String displayname = null;
        Parameters.put("timeseriesId", NL_TIMESERIES_PREFIX + ":" + timeSeries);
        Parameters.put("startTimestamp", String.valueOf(getUtcDate()));
        Parameters.put("endTimestamp", String.valueOf(System.currentTimeMillis()));

        httpGenerator = new HTTPGenerator(url, HTTP_GET_METHOD, headerMap, Parameters);

        httpCode = httpGenerator.executeAndGetResponseCode();
        httpGenerator.closeHttpClient();

        return httpCode == HTTP_RESPONSE;
    }


    ///---------to update after the feedback from ANdy
    private void createNlTimeSeries(final String timeSeriesName, final String displayName,
                                    final String type, final String unit, final NLGlobalStat stat)
            throws MalformedURLException, URISyntaxException {
        int httpCode;
        HashMap<String, String> head = new HashMap<String, String>();
        HashMap<String, String> parameters = new HashMap<String, String>();
        String url = getApiUrl() + DYNATRACE_TIME_SERIES_CREATION + ":" + timeSeriesName;
        addTokenIngetParam(parameters);

        String jsonString = "{\"displayName\":\"" + displayName + "\","
                + "\"unit\":\"" + unit + "\","
                + "\"dimensions\": [\"Neoload\"],"
                + "\"types\":[\"" + type + "\"]}";

        HTTPGenerator insightHttp = HTTPGenerator.newJsonHttpGenerator(HTTP_PUT_METHOD, url, head, parameters, jsonString);

        try {
            httpCode = insightHttp.executeAndGetResponseCode();
            if (httpCode == HTTP_RESPONSE_CREATED || httpCode == HTTP_RESPONSE_ALREADY)
                //------change the code to give a status if the data has been properly created...---review this pieece of code
                stat.setStatus(timeSeriesName);
            //throw new DynatraceStatException("Unable to create TImeseries : "+DYNATRACE_TIME_SERIES_CREATION+":"+TimeseriesName);

        } catch (IOException e) {
            e.printStackTrace();
        }

        insightHttp.closeHttpClient();
    }

    private String getApiUrl() {
        String result;

        if (dynatraceManagedHostName != null) {
            result = DYNATRACE_PROTOCOL + dynatraceManagedHostName + "/api/v1/";
        } else {
            result = DYNATRACE_PROTOCOL + dynatraceAccountId + DYNATRACE_URL;
        }
        return result;
    }

    private String getNlUrl() {
        String result;
        if (nlInstance != null) {
            result = HTTPS + nlInstance + NEOLOAD_URL_LAST;
        } else {
            result = HTTPS + NEOLOAD_SAAS_NEOTYS_COM + NEOLOAD_URL_LAST;
        }
        return result;
    }

    ///---------to update after the feedback from ANdy
    private void sendMetricToTimeSeriestApi(final List<String[]> data, final String type)
            throws DynatraceStatException, MalformedURLException, URISyntaxException {
        int httpCode;
        HashMap<String, String> head = new HashMap<String, String>();
        HashMap<String, String> parameters = new HashMap<String, String>();
        HTTPGenerator insightHttp;

        addTokenIngetParam(parameters);

        String url = getApiUrl() + DYNATRACE_NEW_DATA + "NeoLoadData";
        String exceptionMessage = null;
        long time = System.currentTimeMillis();

        if ("localhost".equalsIgnoreCase(nlControllerHost)){
            nlControllerHost = "10.0.1.0";
        }

        String jsonString = "{\"displayName\" : \"NeoLoad Data\","
                + "\"ipAddresses\" : [\"" + nlControllerHost + "\"],"
                + "\"listenPorts\" : [\"" + 7400 + "\"],"
                + "\"type\" : \"" + type + "\","
                + "\"favicon\" : \"" + NL_PICTURE_URL + "\","
                + "\"configUrl\" : \"" + getNlUrl() + testId + "\","
                + "\"tags\": [\"Loadtest\", \"NeoLoad\"],"
                + "\"properties\" : { \"TestName\" : \"" + testName + "\" ,\"ScenarioName\" : \"" + nlScenarioName + "\"  },"
                + "\"series\" : [";

        String conStr;

        int i = 0;
        int totalSize = data.size() - 1;
        for (String[] metric : data) {
            if (metric[4].equalsIgnoreCase("true")) {

                conStr = "{"
                        + "\"timeseriesId\" : \"custom:" + metric[1] + "\","
                        + "\"dimensions\" : { \"Neoload\" : \"" + metric[0] + "\"  },"
                        + "\"dataPoints\" : [ [" + String.valueOf(time) + "  , " + metric[3] + " ] ]"
                        + "}";


                jsonString += conStr;
                if (i < (totalSize))
                    jsonString += ",";

                i++;
            } else
                totalSize--;
        }

        if (jsonString.substring(jsonString.length() - 1).equalsIgnoreCase(","))
            jsonString = jsonString.substring(0, jsonString.length() - 1);

        jsonString += "]}";

        if (i > 0) {

			insightHttp = HTTPGenerator.newJsonHttpGenerator(HTTP_POST_METHOD, url, head, parameters, jsonString);

            try {
                httpCode = insightHttp.executeAndGetResponseCode();
                switch (httpCode) {

                    case BAD_REQUEST:
                        exceptionMessage = "The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
                        break;
                    case UNAUTHORIZED:
                        exceptionMessage = "Authentication error (no license key header, or invalid license key).";
                        break;
                    case NOT_FOUND:
                        exceptionMessage = "Invalid URL.";
                        break;
                    case METHOD_NOT_ALLOWED:
                        exceptionMessage = "Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
                        break;
                    case REQUEST_ENTITY_TOO_LARGE:
                        exceptionMessage = "Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
                        break;
                    case INTERNAL_SERVER_ERROR:
                        exceptionMessage = "Unexpected server error";
                        break;
                    case BAD_GATEWAY:
                        exceptionMessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
                        break;
                    case SERVICE_UNAVAIBLE:
                        exceptionMessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
                        break;
                    case GATEWAY_TIMEOUT:
                        exceptionMessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
                        break;

                }
                if (exceptionMessage != null)
                    throw new DynatraceStatException(exceptionMessage);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            insightHttp.closeHttpClient();
        }
    }


    @Override
    public void run() {
        try {
            sendStatsToDynatrace();
        } catch (ApiException | DynatraceStatException | IOException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

