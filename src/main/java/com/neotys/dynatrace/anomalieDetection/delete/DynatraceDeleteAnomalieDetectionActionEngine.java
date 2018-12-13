package com.neotys.dynatrace.anomalieDetection.delete;

import com.google.common.base.Optional;
import com.neotys.action.result.ResultFactory;
import com.neotys.dynatrace.anomalieDetection.NeoLoadAnomalieDetectionApi;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;

public class DynatraceDeleteAnomalieDetectionActionEngine implements ActionEngine {
    private static final String STATUS_CODE_INVALID_PARAMETER = "NL-DYNATRACE_ANOMALIE_ACTION-01";
    private static final String STATUS_CODE_TECHNICAL_ERROR = "NL-DYNATRACE_ANOMALIE_ACTION-02";
    private static final String STATUS_CODE_BAD_CONTEXT = "NL-DYNATRACE_ANOMALIE_ACTION-03";

    @Override
    public SampleResult execute(Context context, List<ActionParameter> list) {
        final SampleResult sampleResult = new SampleResult();
        final StringBuilder requestBuilder = new StringBuilder();
        final StringBuilder responseBuilder = new StringBuilder();
        List<String> listofids;
        final Map<String, Optional<String>> parsedArgs;
        try {
            parsedArgs = parseArguments(list, DynatraceDeleteAnomalieDetectionOption.values());
        } catch (final IllegalArgumentException iae) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ", iae);
        }
        final Logger logger = context.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + this.getClass().getName() + " with parameters: "
                    + getArgumentLogString(parsedArgs, DynatraceDeleteAnomalieDetectionOption.values()));
        }
        final String dynatraceId = parsedArgs.get(DynatraceDeleteAnomalieDetectionOption.DynatraceId.getName()).get();
        final String dynatraceApiKey = parsedArgs.get(DynatraceDeleteAnomalieDetectionOption.DynatraceApiKey.getName()).get();
        final Optional<String> dynatraceManagedHostname = parsedArgs.get(DynatraceDeleteAnomalieDetectionOption.DynatraceManagedHostname.getName());
        final Optional<String> proxyName = parsedArgs.get(DynatraceDeleteAnomalieDetectionOption.NeoLoadProxy.getName());
        final Optional<String> optionalTraceMode = parsedArgs.get(DynatraceDeleteAnomalieDetectionOption.TraceMode.getName());
        boolean traceMode = optionalTraceMode.isPresent() && Boolean.valueOf(optionalTraceMode.get());

        try
        {
            listofids=(List<String>)context.getCurrentVirtualUser().remove("Dynatrace_Anoamlie");
            if(listofids==null)
                return ResultFactory.newErrorResult(context, STATUS_CODE_BAD_CONTEXT, "There is no Anamalies created during this test");

            NeoLoadAnomalieDetectionApi anomalieDetectionApi=new NeoLoadAnomalieDetectionApi(dynatraceApiKey,dynatraceId,dynatraceManagedHostname,proxyName,context,traceMode);
            anomalieDetectionApi.deleteAnomalieDetectionfromIds(listofids);
        }
        catch (Exception e)
        {
            return ResultFactory.newErrorResult(context, STATUS_CODE_TECHNICAL_ERROR, "Technical Error: ", e);
        }
        return sampleResult;
    }

    @Override
    public void stopExecute() {

    }
}
