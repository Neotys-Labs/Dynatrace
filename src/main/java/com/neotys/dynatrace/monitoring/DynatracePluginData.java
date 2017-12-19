package com.neotys.dynatrace.monitoring;

import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Context;
import io.swagger.client.ApiClient;
import io.swagger.client.api.ResultsApi;

import java.util.Timer;

public class DynatracePluginData {
    private static final int MAXDURATION_TIME = 2000;
    private static final String NL_TEST_RUNNING = "RUNNING";
    private static final int TIMER_FREQUENCY = 30000;
    private static final int TIMER_DELAY = 0;

    private Context neoLoadContext;
    private ApiClient neoLoadWebApiClient;
    private ResultsApi nlWebResult;
    private NeoLoadStatAggregator neoLoadAggregator = null;
    Timer timerDynatrace = null;

    private final String dataExchangeApiUrl;

    private String dynataceApiKey;
    private String testId = null;
    private String projectname;
    private String dynatraceAccountId = null;
    private String testName = null;
    private String dynatraceApplicationName;
    private Optional<String> dynatraceManagedHostname = null;
    private final Optional<String> proxyName;

    public DynatracePluginData(final String dynataceApiKey, final String neoLoadWebApiKey,
                               final Optional<String> proxyName, final Context context, final String dynatraceId,
                               final String dataExchangeApiUrl, final Optional<String> dynatraceManagedHostname) {

        this.dynataceApiKey = dynataceApiKey;
        dynatraceAccountId = dynatraceId;

        //----define  the NLWEB API-----
        neoLoadWebApiClient = new ApiClient();
        neoLoadWebApiClient.setApiKey(neoLoadWebApiKey);
        neoLoadWebApiClient.setBasePath(context.getWebPlatformApiUrl());
        this.dynatraceManagedHostname = dynatraceManagedHostname;
        initNeoLoadApi();
        //-------------------------
        neoLoadContext = context;
        //TODO get from context ?
        this.proxyName = proxyName;
        this.dataExchangeApiUrl = dataExchangeApiUrl;
        projectname = getProjecName();
        testName = getTestName();

        if (testId == null) {
            setTestId(getTestId());
            if (neoLoadAggregator == null)
                neoLoadAggregator = new NeoLoadStatAggregator(dynataceApiKey, projectname,
                        dynatraceAccountId, nlWebResult, testId, getTestScenarioName(),
                        testName, dataExchangeApiUrl, dynatraceManagedHostname);
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

    public void resumeTimer() {
        timerDynatrace = new Timer();
        neoLoadAggregator = new NeoLoadStatAggregator(dynataceApiKey, projectname, dynatraceAccountId, nlWebResult, testId, getTestScenarioName(), testName, dataExchangeApiUrl, dynatraceManagedHostname);
        timerDynatrace.scheduleAtFixedRate(neoLoadAggregator, TIMER_DELAY, TIMER_FREQUENCY);
    }

    private void initNeoLoadApi() {
        nlWebResult = new ResultsApi(neoLoadWebApiClient);
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
