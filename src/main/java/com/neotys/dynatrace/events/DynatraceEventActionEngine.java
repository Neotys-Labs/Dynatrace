package com.neotys.dynatrace.events;

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

public final class DynatraceEventActionEngine implements ActionEngine {

	private static final String STATUS_CODE_INVALID_PARAMETER = "NL-DYNATRACE_EVENT_ACTION-01";
	private static final String STATUS_CODE_TECHNICAL_ERROR = "NL-DYNATRACE_EVENT_ACTION-02";

	@Override
	public SampleResult execute(final Context context, final List<ActionParameter> parameters) {
		final SampleResult sampleResult = new SampleResult();
		// TODO logs, what are the request and the response?
		// TODO handle proxy to access Dynatrace
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

		final String dynatraceId = parsedArgs.get(DynatraceEventOption.DynatraceId.getName()).get();
		final String dynatraceApiKey = parsedArgs.get(DynatraceEventOption.DynatraceApiKey.getName()).get();
		final Optional<String> dynatraceTags = parsedArgs.get(DynatraceEventOption.DynatraceTags.getName());
		final Optional<String> dynatraceManagedHostname = parsedArgs.get(DynatraceEventOption.DynatraceManagedHostname.getName());
		sampleResult.sampleStart();
		try {
			final DynatraceEventAPI eventAPI = new DynatraceEventAPI(context, dynatraceId, dynatraceApiKey, dynatraceTags, dynatraceManagedHostname);
			logger.debug("Sending start test...");
			eventAPI.sendStartTest();
			logger.debug("Start test sent");
		} catch (Exception e) {
			return ResultFactory.newErrorResult(context, STATUS_CODE_TECHNICAL_ERROR, "Technical Error encouter :", e);
		}
		sampleResult.sampleEnd();

		sampleResult.setRequestContent(requestBuilder.toString());
		sampleResult.setResponseContent(responseBuilder.toString());
		return sampleResult;
	}

	@Override
	public void stopExecute() {
		// nothing to do
	}

}
