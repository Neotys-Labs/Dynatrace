package com.neotys.dynatrace.sanityCheck;

import com.google.common.base.Optional;
import com.neotys.action.result.ResultFactory;
import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.dynatrace.sanityCheck.jsonExport.DynatracePGIMetrics;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;

public class DynatraceSanityCheckActionEngine implements ActionEngine {
    private static final String STATUS_CODE_INVALID_PARAMETER = "NL-DYNATRACE_SANITYCHECK_ACTION-01";
    private static final String STATUS_CODE_TECHNICAL_ERROR = "NL-DYNATRACE_SANITYCHECK_ACTION-02";
    private static final String STATUS_CODE_BAD_CONTEXT = "NL-DYNATRACE_SANITYCHECK_ACTION-03";

    @Override
    public SampleResult execute(Context context, List<ActionParameter> list) {
        final SampleResult sampleResult = new SampleResult();
        final StringBuilder requestBuilder = new StringBuilder();
        final StringBuilder responseBuilder = new StringBuilder();


        final Map<String, Optional<String>> parsedArgs;
        try {
            parsedArgs = parseArguments(list, DynatraceSanityCheckOption.values());
        } catch (final IllegalArgumentException iae) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ", iae);
        }


        final Logger logger = context.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + this.getClass().getName() + " with parameters: "
                    + getArgumentLogString(parsedArgs, DynatraceSanityCheckOption.values()));
        }

        final String dynatraceId = parsedArgs.get(DynatraceSanityCheckOption.DynatraceId.getName()).get();
        final String dynatraceApiKey = parsedArgs.get(DynatraceSanityCheckOption.DynatraceApiKey.getName()).get();
        final Optional<String> dynatraceTags = parsedArgs.get(DynatraceSanityCheckOption.DynatraceTags.getName());
        final Optional<String> dynatraceManagedHostname = parsedArgs.get(DynatraceSanityCheckOption.DynatraceManagedHostname.getName());
        final String outPutReferenceFile = parsedArgs.get(DynatraceSanityCheckOption.OutputJSONReferenceFile.getName()).get();
        final Optional<String> proxyName = parsedArgs.get(DynatraceSanityCheckOption.NeoLoadProxy.getName());
        final Optional<String> optionalTraceMode = parsedArgs.get(DynatraceSanityCheckOption.TraceMode.getName());
        try
        {
            //#TODO remove applicaiton name ---parse the architecture based on tags

            boolean traceMode = optionalTraceMode.isPresent() && Boolean.valueOf(optionalTraceMode.get());
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            long startTs = now.toInstant().toEpochMilli();
            DynatracePGIMetrics dynatracePGIMetrics=new DynatracePGIMetrics(dynatraceApiKey,dynatraceId,dynatraceTags,dynatraceManagedHostname,proxyName,context,startTs,traceMode);
            dynatracePGIMetrics.sanityCheck(outPutReferenceFile);
        }
        catch (DynatraceException e)
        {
            return ResultFactory.newErrorResult(context, STATUS_CODE_BAD_CONTEXT, "Error encountered :", e);

        }
        catch (Exception e) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_TECHNICAL_ERROR, "Error encountered :", e);
        }

        sampleResult.setRequestContent(requestBuilder.toString());
        sampleResult.setResponseContent(responseBuilder.toString());
        return sampleResult;

    }

    @Override
    public void stopExecute() {

    }
}
