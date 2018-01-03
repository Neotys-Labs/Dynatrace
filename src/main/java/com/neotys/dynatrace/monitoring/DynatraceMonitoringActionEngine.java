package com.neotys.dynatrace.monitoring;

import com.google.common.base.Optional;
import com.neotys.action.result.ResultFactory;
import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;

import java.util.List;
import java.util.Map;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;

public final class DynatraceMonitoringActionEngine implements ActionEngine {

    private static final String STATUS_CODE_INVALID_PARAMETER = "NL-DYNATRACE_MONITORING_ACTION-01";
    private static final String STATUS_CODE_TECHNICAL_ERROR = "NL-DYNATRACE_MONITORING_ACTION-02";
    private static final String STATUS_CODE_BAD_CONTEXT = "NL-DYNATRACE_MONITORING_ACTION-03";

    private DynatraceIntegration dynatraceIntegration;
    private DynatracePluginData dynatracePluginData;

    @Override
    public SampleResult execute(final Context context, final List<ActionParameter> parameters) {
        final SampleResult sampleResult = new SampleResult();
        final StringBuilder requestBuilder = new StringBuilder();
        final StringBuilder responseBuilder = new StringBuilder();


        final Map<String, Optional<String>> parsedArgs;
        try {
            parsedArgs = parseArguments(parameters, DynatraceMonitoringOption.values());
        } catch (final IllegalArgumentException iae) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ", iae);
        }

        if (context.getWebPlatformRunningTestUrl() == null) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_BAD_CONTEXT, "Bad context: ", new DynatraceException("No NeoLoad Web test is running"));
        }

        final Logger logger = context.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + this.getClass().getName() + " with parameters: "
                    + getArgumentLogString(parsedArgs, DynatraceMonitoringOption.values()));
        }

        final String dynatraceId = parsedArgs.get(DynatraceMonitoringOption.DynatraceId.getName()).get();
        final String dynatraceApiKey = parsedArgs.get(DynatraceMonitoringOption.DynatraceApiKey.getName()).get();
        final Optional<String> dynatraceTags = parsedArgs.get(DynatraceMonitoringOption.DynatraceTags.getName());
        final Optional<String> dynatraceManagedHostname = parsedArgs.get(DynatraceMonitoringOption.DynatraceManagedHostname.getName());
        final String dataExchangeApiUrl = parsedArgs.get(DynatraceMonitoringOption.NeoLoadDataExchangeApiUrl.getName()).get();
        final Optional<String> dataExchangeApiKey = parsedArgs.get(DynatraceMonitoringOption.NeoLoadDataExchangeApiKey.getName());
        final Optional<String> proxyName = parsedArgs.get(DynatraceMonitoringOption.NeoLoadProxy.getName());

        sampleResult.sampleStart();
        try {
            dynatracePluginData = (DynatracePluginData) context.getCurrentVirtualUser().get("PLUGINDATA");
            if (dynatracePluginData == null) {
                // Delay by two seconds to ensure no conflicts in re-establishing connection
                    dynatracePluginData = new DynatracePluginData(dynatraceApiKey, context.getAccountToken(), proxyName,
                            context, dynatraceId, dataExchangeApiUrl, dynatraceManagedHostname);

                    context.getCurrentVirtualUser().put("PLUGINDATA", dynatracePluginData);
                dynatracePluginData.startTimer();
            } else {
                dynatracePluginData.resumeTimer();
            }

            long startTs = System.currentTimeMillis() - context.getElapsedTime();
            logger.debug("Sending start test...");
            dynatraceIntegration = new DynatraceIntegration(context, dynatraceApiKey, dynatraceId, dynatraceTags, dataExchangeApiUrl, dataExchangeApiKey, proxyName, dynatraceManagedHostname, startTs);

            //first call send event to dynatrace
            sampleResult.sampleEnd();
            dynatracePluginData.stopTimer();

        } catch (Exception e) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_TECHNICAL_ERROR, "Technical Error encouter :", e);
        }

        sampleResult.setRequestContent(requestBuilder.toString());
        sampleResult.setResponseContent(responseBuilder.toString());
        return sampleResult;
    }

    @Override
    public void stopExecute() {
        if (dynatracePluginData != null)
            dynatracePluginData.stopTimer();

        if (dynatraceIntegration != null)
            dynatraceIntegration.setTestToStop();
    }

}
