package com.neotys.dynatrace.monitoring;

import com.google.common.base.Optional;
import com.neotys.action.result.ResultFactory;
import com.neotys.dynatrace.common.Constants;
import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.dynatrace.monitoring.custommetrics.DynatracePluginData;
import com.neotys.dynatrace.monitoring.timeseries.DynatraceGetTimeSeries;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.error.NeotysAPIException;
import org.apache.olingo.odata2.api.exception.ODataException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.emptyToNull;
import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;

public final class DynatraceMonitoringActionEngine implements ActionEngine {

    private static final String STATUS_CODE_INVALID_PARAMETER = "NL-DYNATRACE_MONITORING_ACTION-01";
    private static final String STATUS_CODE_TECHNICAL_ERROR = "NL-DYNATRACE_MONITORING_ACTION-02";
    private static final String STATUS_CODE_BAD_CONTEXT = "NL-DYNATRACE_MONITORING_ACTION-03";

    private DynatraceGetTimeSeries dynatraceIntegration;

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
        final Optional<String> dataExchangeApiKey = parsedArgs.get(DynatraceMonitoringOption.NeoLoadDataExchangeApiKey.getName());
        final Optional<String> proxyName = parsedArgs.get(DynatraceMonitoringOption.NeoLoadProxy.getName());
        final Optional<String> optionalTraceMode = parsedArgs.get(DynatraceMonitoringOption.TraceMode.getName());

        final String dataExchangeApiUrl = Optional.fromNullable(emptyToNull(parsedArgs.get(DynatraceMonitoringOption.NeoLoadDataExchangeApiUrl.getName()).orNull()))
                .or(() -> getDefaultDataExchangeApiUrl(context));

        if (context.getLogger().isDebugEnabled()) {
            context.getLogger().debug("Data Exchange API URL used: " + dataExchangeApiUrl);
        }

        try {

            // Check last execution time (and fail if called less than 45 seconds ago).
            final Object dynatraceLastExecutionTime = context.getCurrentVirtualUser().get(Constants.DYNATRACE_LAST_EXECUTION_TIME);
            final Long dynatraceCurrentExecution = System.currentTimeMillis();

            if(!(dynatraceLastExecutionTime instanceof Long)){
                requestBuilder.append("(first execution).\n");
            } else if((Long)dynatraceLastExecutionTime + 25*1000 > dynatraceCurrentExecution){
                return ResultFactory.newErrorResult(context, STATUS_CODE_BAD_CONTEXT, "Bad context: Not enough delay between the two Dynatrace advanced action execution. Make sure to have at least 30 seconds pacing on the Actions container.");
            } else {
                requestBuilder.append("(last execution was " + ((dynatraceCurrentExecution - (Long)dynatraceLastExecutionTime)/1000) + " seconds ago)\n");
            }

            context.getCurrentVirtualUser().put(Constants.DYNATRACE_LAST_EXECUTION_TIME, dynatraceCurrentExecution);
            boolean traceMode = optionalTraceMode.isPresent() && Boolean.valueOf(optionalTraceMode.get());

	        /*
	         * Handle push Neoload custom metrics
	         */
            final DynatracePluginData pluginData = DynatracePluginData.getInstance(context, dynatraceId, dynatraceApiKey, dynatraceManagedHostname, proxyName, traceMode);

            final String virtualUserId = context.getCurrentVirtualUser().getId();
            // if therer is multiple virtual user handling the action return error
            if (pluginData != null && !pluginData.getVirtualUserId().equals(virtualUserId)) {
                return ResultFactory.newErrorResult(context, STATUS_CODE_BAD_CONTEXT, "Bad context: ", new DynatraceException("Multiple VU on action"));
            }

            sampleResult.sampleStart();
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            long startTs = now.toInstant().toEpochMilli() - context.getElapsedTime();
            logger.debug("Sending start test...");

            pluginData.getNeoLoadAggregator().run();

	        /*
	         * Handle Dynatrace Timeseries
	         */
            // Retrieve DataExchangeAPIClient from Context, or instantiate new one
            DataExchangeAPIClient dataExchangeAPIClient = getDataExchangeAPIClient(context, requestBuilder, dataExchangeApiUrl, dataExchangeApiKey);

            dynatraceIntegration = new DynatraceGetTimeSeries(context, dynatraceApiKey, dynatraceId, dynatraceTags, dataExchangeAPIClient, proxyName, dynatraceManagedHostname, startTs, traceMode);
            dynatraceIntegration.processDynatraceData();

            //first call send event to dynatrace
            sampleResult.sampleEnd();
        } catch (Exception e) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_TECHNICAL_ERROR, "Error encountered :", e);
        }

        sampleResult.setRequestContent(requestBuilder.toString());
        sampleResult.setResponseContent(responseBuilder.toString());
        return sampleResult;
    }

    private String getDefaultDataExchangeApiUrl(final Context context) {
        return "http://" + context.getControllerIp() + ":7400/DataExchange/v1/Service.svc/";
    }

    private DataExchangeAPIClient getDataExchangeAPIClient(final Context context, final StringBuilder requestBuilder, final String dataExchangeApiUrl, final Optional<String> dataExchangeApiKey) throws GeneralSecurityException, IOException, ODataException, URISyntaxException, NeotysAPIException {
        DataExchangeAPIClient dataExchangeAPIClient = (DataExchangeAPIClient) context.getCurrentVirtualUser().get(Constants.NL_DATA_EXCHANGE_API_CLIENT);
        if (dataExchangeAPIClient == null) {
                final ContextBuilder contextBuilder = new ContextBuilder();
                contextBuilder.hardware(Constants.NEOLOAD_CONTEXT_HARDWARE).location(Constants.NEOLOAD_CONTEXT_LOCATION).software(
                        Constants.NEOLOAD_CONTEXT_SOFTWARE).script("DynatraceMonitoring" + System.currentTimeMillis());
                dataExchangeAPIClient = DataExchangeAPIClientFactory.newClient(dataExchangeApiUrl,
                        contextBuilder.build(),
                        dataExchangeApiKey.orNull());
                context.getCurrentVirtualUser().put(Constants.NL_DATA_EXCHANGE_API_CLIENT, dataExchangeAPIClient);
                requestBuilder.append("DataExchangeAPIClient created.\n");
        } else {
            requestBuilder.append("DataExchangeAPIClient retrieved from User Path Context.\n");
        }
        return dataExchangeAPIClient;
    }

    @Override
    public void stopExecute() {
        if (dynatraceIntegration != null)
            dynatraceIntegration.setTestToStop();
    }

}
