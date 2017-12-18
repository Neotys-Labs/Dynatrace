package com.neotys.dynatrace.monitoring;

import java.io.IOException;
import java.util.Timer;

import com.google.common.base.Optional;
import org.apache.http.client.ClientProtocolException;

import com.neotys.extensions.action.engine.Context;

import io.swagger.client.ApiClient;
import io.swagger.client.api.ResultsApi;

public class DynatracePluginData {
    private static final String NEOLOAD_WEB_BASEURL = "https://neoload-api.saas.neotys.com/v1/";
    private static final int MAXDURATION_TIME = 2000;
    private static final String NL_TEST_RUNNING = "RUNNING";
    private static final int TIMER_FREQUENCY = 30000;
    static final int TIMER_DELAY = 0;
    private final Optional<String> proxyName;

    private Context neoLoadContext;
    private ApiClient neoLoadWebApiClient;
    private ResultsApi nlwebResult;
    private NeoLoadStatAggregator neoLoadAggregator = null;
    private NLGlobalStat neoLoadStat = null;
    Timer timerDynatrace = null;

    private final String neoLoadHost;

    private String dynataceApiKey;
    private String testId = null;
    private String projectname;
    private String dynatraceAccountId = null;
    private String testName = null;
    private String dynatraceApplicationName;
    private Optional<String> nlManagedInstance;
    private Optional<String> dynatraceManagedHostname = null;

    public DynatracePluginData(final String dynataceApiKey, final String neoLoadWebApiKey,
                               final Optional<String> proxyName, final Context context, final String dynatraceId,
                               final String neoLoadHost, final Optional<String> dynatraceManagedHostname, final Optional<String> nlInstance)
            throws ClientProtocolException, DynatraceStatException, IOException {

        this.dynataceApiKey = dynataceApiKey;
        dynatraceAccountId = dynatraceId;

        //----define  the NLWEB API-----
        neoLoadWebApiClient = new ApiClient();
        neoLoadWebApiClient.setApiKey(neoLoadWebApiKey);
        neoLoadWebApiClient.setBasePath(NEOLOAD_WEB_BASEURL);
        //TODO get from param
        this.dynatraceManagedHostname = dynatraceManagedHostname;
		nlManagedInstance=nlInstance;
        initNeoLoadApi();
        //-------------------------
        neoLoadContext = context;
        //TODO get from context
        this.proxyName = proxyName;
        this.neoLoadHost = neoLoadHost;
        neoLoadStat = new NLGlobalStat();
        projectname = getProjecName();
        testName = getTestName();

        if (testId == null) {
            setTestId(getTestId());
            neoLoadStat = new NLGlobalStat();
            if (neoLoadAggregator == null)
                neoLoadAggregator = new NeoLoadStatAggregator(dynataceApiKey, projectname,
                        dynatraceAccountId, nlwebResult, testId, neoLoadStat, getTestScenarioName(),
                        testName, neoLoadHost, dynatraceManagedHostname, nlManagedInstance);
        }
    }

    private void setTestId(final String testId) {
        this.testId = testId;
    }

    public void setProjectName(final String projectName) {
        projectname = projectName;
    }

    public void startTimer() {
        timerDynatrace = new Timer();
        timerDynatrace.scheduleAtFixedRate(neoLoadAggregator, TIMER_DELAY, TIMER_FREQUENCY);
    }

    public void stopTimer() {
        timerDynatrace.cancel();
    }

    public void resumeTimer() throws ClientProtocolException, DynatraceStatException, IOException {
        timerDynatrace = new Timer();
        neoLoadAggregator = new NeoLoadStatAggregator(dynataceApiKey, projectname, dynatraceAccountId, nlwebResult, testId, neoLoadStat, getTestScenarioName(), testName, neoLoadHost, dynatraceManagedHostname, nlManagedInstance);
        timerDynatrace.scheduleAtFixedRate(neoLoadAggregator, TIMER_DELAY, TIMER_FREQUENCY);
    }

    private void initNeoLoadApi() {
        nlwebResult = new ResultsApi(neoLoadWebApiClient);
    }

    private String getTestName() {
        return neoLoadContext.getTestName();
    }

    private String getTestScenarioName() {
        return neoLoadContext.getScenarioName();
    }

    private String getProjecName() {
        return neoLoadContext.getProjectName();
    }


    private String getTestId() {
        return neoLoadContext.getTestId();

    }
}
