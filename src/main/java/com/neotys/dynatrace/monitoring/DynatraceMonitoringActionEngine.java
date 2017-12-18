package com.neotys.dynatrace.monitoring;

import com.google.common.base.Optional;
import com.neotys.action.result.ResultFactory;
import com.neotys.dynatrace.events.DynatraceEventOption;
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

    private DynatraceIntegration dynatrace;
    private DynatracePluginData pluginData;

    @Override
    public SampleResult execute(final Context context, final List<ActionParameter> parameters) {
        final SampleResult sampleResult = new SampleResult();
        final StringBuilder requestBuilder = new StringBuilder();
        final StringBuilder responseBuilder = new StringBuilder();


        final Map<String, Optional<String>> parsedArgs;
        try {
            parsedArgs = parseArguments(parameters, DynatraceEventOption.values());
        } catch (final IllegalArgumentException iae) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ", iae);
        }

        final Logger logger = context.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + this.getClass().getName() + " with parameters: "
                    + getArgumentLogString(parsedArgs, DynatraceEventOption.values()));
        }

        final String dynatraceId = parsedArgs.get(DynatraceMonitoringOption.DynatraceId.getName()).get();
        final String dynatraceApiKey = parsedArgs.get(DynatraceMonitoringOption.DynatraceApiKey.getName()).get();
        final Optional<String> dynatraceTags = parsedArgs.get(DynatraceMonitoringOption.DynatraceTags.getName());
        final Optional<String> dynatraceManagedHostname = parsedArgs.get(DynatraceMonitoringOption.DynatraceManagedHostname.getName());
        final String dataExchangeApiUrl = parsedArgs.get(DynatraceMonitoringOption.NeoLoadDataExchangeApiUrl.getName()).get();
        final Optional<String> dataExchangeApiKey = parsedArgs.get(DynatraceMonitoringOption.NeoLoadDataExchangeApiKey.getName());
        final Optional<String> proxyName = parsedArgs.get(DynatraceMonitoringOption.NeoLoadProxy.getName());

        sampleResult.sampleStart();
        //TODO Add logs
        try {
            pluginData = (DynatracePluginData) context.getCurrentVirtualUser().get("PLUGINDATA");
            if (pluginData == null) {
                // Delay by two seconds to ensure no conflicts in re-establishing connection
                // TODO catch exceptions?
//                try {
                    pluginData = new DynatracePluginData(dynatraceApiKey, context.getAccountToken(), proxyName,
                            context, dynatraceId, dataExchangeApiUrl, dynatraceManagedHostname, dataExchangeApiKey);

                    context.getCurrentVirtualUser().put("PLUGINDATA", pluginData);
                    //----bug to resolve on the senteventapi
                    //eventAPI.SendStartTest();
//                } catch (IOException e) {
//                    return ResultFactory.newErrorResult(context, STATUS_CODE_TECHNICAL_ERROR, "Technical Error encouter :", e);
//                }

                pluginData.startTimer();
            } else {
                pluginData.resumeTimer();
            }

            long startTs = System.currentTimeMillis() - context.getElapsedTime();
            logger.debug("Sending start test...");
            dynatrace = new DynatraceIntegration(dynatraceApiKey, dynatraceId, dynatraceTags, dataExchangeApiUrl, proxyName, dynatraceManagedHostname, startTs);

            //first call send event to dynatrace
            sampleResult.sampleEnd();
            pluginData.stopTimer();

            //---check if aggregator exists
            // startimer
        } catch (Exception e) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_TECHNICAL_ERROR, "Technical Error encouter :", e);
        }

        sampleResult.setRequestContent(requestBuilder.toString());
        sampleResult.setResponseContent(responseBuilder.toString());
        return sampleResult;
    }

    @Override
    public void stopExecute() {
        // TODO add code executed when the test have to stop.
        if (pluginData != null)
            pluginData.stopTimer();

        if (dynatrace != null)
            dynatrace.setTestToStop();
    }

}
