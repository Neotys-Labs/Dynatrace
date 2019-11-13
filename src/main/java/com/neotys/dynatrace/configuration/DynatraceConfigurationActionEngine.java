package com.neotys.dynatrace.configuration;

import com.google.common.base.Optional;
import com.neotys.action.result.ResultFactory;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;

import java.util.List;
import java.util.Map;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;

public class DynatraceConfigurationActionEngine implements ActionEngine {
    private static final String STATUS_CODE_INVALID_PARAMETER = "NL-DYNATRACE_CONF_ACTION-01";
    private static final String STATUS_CODE_TECHNICAL_ERROR = "NL-DYNATRACE_CONF_ACTION-02";
    private static final String STATUS_CODE_BAD_CONTEXT = "NL-DYNATRACE_CONF_ACTION-03";

    @Override
    public SampleResult execute(Context context, List<ActionParameter> list) {
        final SampleResult sampleResult = new SampleResult();
        final StringBuilder requestBuilder = new StringBuilder();
        final StringBuilder responseBuilder = new StringBuilder();
        final Map<String, Optional<String>> parsedArgs;
        try {
            parsedArgs = parseArguments(list, DynatraceConfigurationOption.values());
        } catch (final IllegalArgumentException iae) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ", iae);
        }
        final Logger logger = context.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + this.getClass().getName() + " with parameters: "
                    + getArgumentLogString(parsedArgs, DynatraceConfigurationOption.values()));
        }

        final String dynatraceId = parsedArgs.get(DynatraceConfigurationOption.DynatraceId.getName()).get();
        final String dynatraceApiKey = parsedArgs.get(DynatraceConfigurationOption.DynatraceApiKey.getName()).get();
        final Optional<String> dynatraceManagedHostname = parsedArgs.get(DynatraceConfigurationOption.DynatraceManagedHostname.getName());
        final Optional<String> proxyName = parsedArgs.get(DynatraceConfigurationOption.NeoLoadProxy.getName());
        final Optional<String> optionalTraceMode = parsedArgs.get(DynatraceConfigurationOption.TraceMode.getName());
        final Optional<String> dynatraceTags=parsedArgs.get(DynatraceConfigurationOption.DynatraceTags.getName());
        final Optional<String> compatilitymode=parsedArgs.get(DynatraceConfigurationOption.compatibilityMode.getName());

        boolean traceMode = optionalTraceMode.isPresent() && Boolean.valueOf(optionalTraceMode.get());

        try
        {
            String mode;

            if(compatilitymode.isPresent())
            {
                if(compatilitymode.get().equalsIgnoreCase("TRUE"))
                    mode="OLD";
                else
                    mode="NEW";

            }
            else
                mode="NEW";

            sampleResult.sampleStart();
            DynatraceConfigurationAPI configurationAPI=new DynatraceConfigurationAPI(dynatraceApiKey,dynatraceId,dynatraceManagedHostname,proxyName,dynatraceTags,context,traceMode);
            configurationAPI.generateRequestAttributes(mode);

            //#TODO remove applicaiton name ---parse the architecture based on tags
            configurationAPI.setDynatraceTags(dynatraceTags);

            configurationAPI.createRequestNamingRules(mode);
            sampleResult.sampleEnd();
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
