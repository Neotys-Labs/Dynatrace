package com.neotys.dynatrace.DynatraceEvents;

import com.google.common.base.Strings;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;

import java.util.List;

public final class DynatraceEventStopActionEngine implements ActionEngine {
	private static final String START = "START";
	private static final String STOP = "STOP";

	private String dynatraceId;
	private String dynatraceApiKey;
	private String httpProxyHost;
	private String httpProxyPort;
	private String httpProxyLogin;
	private String httpProxyPassword;
	private String dynatraceApplicationName;
	private String dynatraceManagedHostname;
	private String eventStatus;
	private DynatraceEventAPI eventAPI;
	private String nlManagedInstance;

	@Override
	public SampleResult execute(final Context context, final List<ActionParameter> parameters) {
		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();

		parseParameters(parameters);

		if (Strings.isNullOrEmpty(dynatraceApiKey)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_API_KEY cannot be null "
					+ DynatraceEventAction.DYNATRACE_API_KEY + ".", null);
		}
		if (Strings.isNullOrEmpty(dynatraceApplicationName)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ApplicationName cannot be null "
					+ DynatraceEventAction.DYNATRACE_APPLICATION_NAME + ".", null);
		}
		if (Strings.isNullOrEmpty(dynatraceId)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ID cannot be null "
					+ DynatraceEventAction.DYNATRACE_ID + ".", null);
		}
		if (!Strings.isNullOrEmpty(httpProxyHost)) {
			if (Strings.isNullOrEmpty(httpProxyPort))
				return getErrorResult(context, sampleResult, "Invalid argument: HTTP_PROXY_PORT cannot be null if you specify a Proxy Host"
						+ DynatraceEventAction.HTTP_PROXY_PORT + ".", null);
		}
		if (!Strings.isNullOrEmpty(httpProxyPort)) {
			if (Strings.isNullOrEmpty(httpProxyHost))
				return getErrorResult(context, sampleResult, "Invalid argument: HTTP_PROXY_HOST cannot be null if you specify a Proxy Host"
						+ DynatraceEventAction.HTTP_PROXY_HOST + ".", null);


		}
		if (Strings.isNullOrEmpty(eventStatus)) {
			return getErrorResult(context, sampleResult, "Invalid argument: EventStatus cannot be null "
					+ DynatraceEventAction.EVENT_SATUS + ".", null);
		}


		sampleResult.sampleStart();
		try {
			eventAPI = new DynatraceEventAPI(dynatraceApiKey, dynatraceId, dynatraceApplicationName, context, dynatraceManagedHostname, nlManagedInstance);

			switch (eventStatus) {
				case START:
					eventAPI.sendStartTest();
					break;
				case STOP:
					eventAPI.sendStopTest();
					break;
				default:
					return getErrorResult(context, sampleResult, "Invalid argument: EventStatus can be equal to START or STOP "
							+ DynatraceEventAction.EVENT_SATUS + ".", null);
			}
		} catch (Exception e) {
			return getErrorResult(context, sampleResult, "Technical Error encouter  :", e);
		}

		sampleResult.sampleEnd();

		sampleResult.setRequestContent(requestBuilder.toString());
		sampleResult.setResponseContent(responseBuilder.toString());
		return sampleResult;
	}

	private void parseParameters(final List<ActionParameter> parameters) {
		for (ActionParameter parameter : parameters) {
			switch (parameter.getName()) {
				case DynatraceEventAction.EVENT_SATUS:
					eventStatus = parameter.getValue();
					break;
				case DynatraceEventAction.DYNATRACE_ID:
					dynatraceId = parameter.getValue();
					break;
				case DynatraceEventAction.DYNATRACE_API_KEY:
					dynatraceApiKey = parameter.getValue();
					break;
				case DynatraceEventAction.HTTP_PROXY_HOST:
					httpProxyHost = parameter.getValue();
					break;
				case DynatraceEventAction.HTTP_PROXY_PASSWORD:
					httpProxyPassword = parameter.getValue();
					break;
				case DynatraceEventAction.HTTP_PROXY_LOGIN:
					httpProxyLogin = parameter.getValue();
					break;
				case DynatraceEventAction.HTTP_PROXY_PORT:
					httpProxyPort = parameter.getValue();
					break;
				case DynatraceEventAction.DYNATRACE_MANAGED_HOSTNAME:
					dynatraceManagedHostname = parameter.getValue();
					break;

				case DynatraceEventAction.DYNATRACE_APPLICATION_NAME:
					dynatraceApplicationName = parameter.getValue();
					break;

				case DynatraceEventAction.NL_MANAGED_INSTANCE:
					nlManagedInstance = parameter.getValue();
					break;

			}
		}
	}

	private void appendLineToStringBuilder(final StringBuilder sb, final String line) {
		sb.append(line).append("\n");
	}

	/**
	 * This method allows to easily create an error result and log exception.
	 */
	private static SampleResult getErrorResult(final Context context, final SampleResult result, final String errorMessage, final Exception exception) {
		result.setError(true);
		result.setStatusCode("NL-DynatraceEvent_ERROR");
		result.setResponseContent(errorMessage);
		if (exception != null) {
			context.getLogger().error(errorMessage, exception);
		} else {
			context.getLogger().error(errorMessage);
		}
		return result;
	}

	@Override
	public void stopExecute() {
		// TODO add code executed when the test have to stop.
	}

}
