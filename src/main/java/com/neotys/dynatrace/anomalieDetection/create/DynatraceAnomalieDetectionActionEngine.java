package com.neotys.dynatrace.anomalieDetection.create;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
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

public class DynatraceAnomalieDetectionActionEngine implements ActionEngine {
    private static final String STATUS_CODE_INVALID_PARAMETER = "NL-DYNATRACE_ANOMALIE_ACTION-01";
    private static final String STATUS_CODE_TECHNICAL_ERROR = "NL-DYNATRACE_ANOMALIE_ACTION-02";
    private static final String STATUS_CODE_BAD_CONTEXT = "NL-DYNATRACE_ANOMALIE_ACTION-03";
    public static final ImmutableList<String> OPERATOR = ImmutableList.of("ABOVE", "BELOW");
    public static final ImmutableList<String> TYPE_ANOMALIE = ImmutableList.of("AVAILABILITY", "CUSTOM_ALERT", "ERROR", "PERFORMANCE", "RESOURCE_CONTENTION");
    @Override
    public SampleResult execute(Context context, List<ActionParameter> list) {
        final SampleResult sampleResult = new SampleResult();
        final StringBuilder requestBuilder = new StringBuilder();
        final StringBuilder responseBuilder = new StringBuilder();
        final Map<String, Optional<String>> parsedArgs;
        try {
            parsedArgs = parseArguments(list, DynatraceAnomalieDetectionOption.values());
        } catch (final IllegalArgumentException iae) {
            return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ", iae);
        }
        final Logger logger = context.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + this.getClass().getName() + " with parameters: "
                    + getArgumentLogString(parsedArgs, DynatraceAnomalieDetectionOption.values()));
        }

        final String dynatraceId = parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceId.getName()).get();
        final String dynatraceApiKey = parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceApiKey.getName()).get();
        final Optional<String> dynatraceManagedHostname = parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceManagedHostname.getName());
        final Optional<String> proxyName = parsedArgs.get(DynatraceAnomalieDetectionOption.NeoLoadProxy.getName());
        final Optional<String> optionalTraceMode = parsedArgs.get(DynatraceAnomalieDetectionOption.TraceMode.getName());
        final Optional<String> dynatraceTags=parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceTags.getName());
        final String dynatracemetric=parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceMetricName.getName()).get();;
        String operator=parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceOperator.getName()).get();;
        final String threshold=parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceMericValue.getName()).get();;
        String typeOfAnomalie=parsedArgs.get(DynatraceAnomalieDetectionOption.DynatraceTypeofAnomalie.getName()).get();
        boolean traceMode = optionalTraceMode.isPresent() && Boolean.valueOf(optionalTraceMode.get());
        List<String> listofids;
        //----------validate the value----------------
        //----valid Operator------------------
        if(isPartOftheList(operator,OPERATOR))
            operator=operator.toUpperCase();
        else
            return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Invalid value of Operator: "+operator, null);


        //----valid value ---------------------
        if(!isaDigit(threshold))
            return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Threshold needs to be a digit: "+threshold,null );

        //----valid type of anoamlie------------
        if(isPartOftheList(typeOfAnomalie,TYPE_ANOMALIE))
            typeOfAnomalie=typeOfAnomalie.toUpperCase();
        else
            return ResultFactory.newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Invalid value of type of anomalie: "+typeOfAnomalie,null );

        try
        {
            listofids=(List<String>)context.getCurrentVirtualUser().get("Dynatrace_Anoamlie");
            if(listofids==null)
                listofids=new ArrayList<>();

            NeoLoadAnomalieDetectionApi anomalieDetectionApi=new NeoLoadAnomalieDetectionApi(dynatraceApiKey,dynatraceId,dynatraceManagedHostname,proxyName,context,traceMode);
            String id=anomalieDetectionApi.createAnomalie(dynatracemetric,operator,typeOfAnomalie,threshold,dynatraceTags);
            if(id!=null)
            {
                listofids.add(id);
                context.getCurrentVirtualUser().put("Dynatrace_Anoamlie",listofids);
            }
        }
        catch (Exception e)
        {
            return ResultFactory.newErrorResult(context, STATUS_CODE_TECHNICAL_ERROR, "Technical Error: ", e);
        }
        return sampleResult;
    }

    private boolean isaDigit(String numeric)
    {
        int test;
        try
        {
            test=Integer.parseInt(numeric);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
        catch(Exception e)
        {
            return false;
        }

    }
    private boolean isPartOftheList(String key, ImmutableList<String> list)
    {
        if(list.contains(key.toUpperCase()))
            return true;
        else
            return false;
    }
    @Override
    public void stopExecute() {

    }
}
