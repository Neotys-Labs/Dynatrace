package com.neotys.dynatrace.DynatraceMonitoring;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Strings;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;

public final class DynatraceMonitoringActionEngine implements ActionEngine {
	private String neoloadApiHost;
	private String neoloadApiport;
	private String neoloadKeyApi;
	private String dynatraceId;
	private String dynatraceApiKey;
	private String httpProxyHost;
	private String httpProxyPort;
	private String httpProxyLogin;
	private String httpProxyPassword;
	private String dynatraceApplicationName;
	private DynatraceIntegration dynatrace;
	private DynatracePluginData pluginData;
	private boolean pluginStored = false;
	private String dynatraceManagedHostname;
	private String nlManagedInstance;

	@Override
	public SampleResult execute(final Context context, final List<ActionParameter> parameters) {
		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();
		long startTs;
		for (final ActionParameter parameter : parameters) {
			switch (parameter.getName()) {
				case DynatraceMonitoringAction.NEOLOAD_API_HOST:
					neoloadApiHost = parameter.getValue();
					break;
				case DynatraceMonitoringAction.NEOLOAD_API_PORT:
					neoloadApiport = parameter.getValue();
					break;
				case DynatraceMonitoringAction.NEOLOAD_KEY_API:
					neoloadKeyApi = parameter.getValue();
					break;

				case DynatraceMonitoringAction.DYNATRACE_ID:
					dynatraceId = parameter.getValue();
					break;
				case DynatraceMonitoringAction.DYNATRACE_API_KEY:
					dynatraceApiKey = parameter.getValue();
					break;
				case DynatraceMonitoringAction.HTTP_PROXY_HOST:
					httpProxyHost = parameter.getValue();
					break;
				case DynatraceMonitoringAction.HTTP_PROXY_PASSWORD:
					httpProxyPassword = parameter.getValue();
					break;
				case DynatraceMonitoringAction.HTTP_PROXY_LOGIN:
					httpProxyLogin = parameter.getValue();
					break;
				case DynatraceMonitoringAction.HTTP_PROXY_PORT:
					httpProxyPort = parameter.getValue();
					break;
				case DynatraceMonitoringAction.DYNATRACE_MANAGED_HOSTNAME:
					dynatraceManagedHostname = parameter.getValue();
					break;

				case DynatraceMonitoringAction.DYNATRACE_APPLICATION_NAME:
					dynatraceApplicationName = parameter.getValue();
					break;

				case DynatraceMonitoringAction.NL_MANAGED_INSTANCE:
					nlManagedInstance = parameter.getValue();
					break;


			}
		}
		if (Strings.isNullOrEmpty(neoloadApiHost)) {
			return getErrorResult(context, sampleResult, "Invalid argument: NeoLoadAPIHost cannot be null "
					+ DynatraceMonitoringAction.NEOLOAD_API_HOST + ".", null);
		}
		if (Strings.isNullOrEmpty(neoloadApiport)) {
			return getErrorResult(context, sampleResult, "Invalid argument: NeoLoadAPIport cannot be null "
					+ DynatraceMonitoringAction.NEOLOAD_API_PORT + ".", null);
		} else {
			try {
				int test = Integer.parseInt(neoloadApiport);
			} catch (Exception e) {
				return getErrorResult(context, sampleResult, "Invalid argument: NeoLoadAPIport needs to be an Integer "
						+ DynatraceMonitoringAction.NEOLOAD_API_PORT + ".", null);
			}

		}

		if (Strings.isNullOrEmpty(dynatraceApiKey)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_API_KEY cannot be null "
					+ DynatraceMonitoringAction.DYNATRACE_API_KEY + ".", null);
		}
		if (Strings.isNullOrEmpty(dynatraceApplicationName)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ApplicationName cannot be null "
					+ DynatraceMonitoringAction.DYNATRACE_APPLICATION_NAME + ".", null);
		}
		if (Strings.isNullOrEmpty(dynatraceId)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ID cannot be null "
					+ DynatraceMonitoringAction.DYNATRACE_ID + ".", null);
		}
		if (!Strings.isNullOrEmpty(httpProxyHost)) {
			if (Strings.isNullOrEmpty(httpProxyPort))
				return getErrorResult(context, sampleResult, "Invalid argument: HTTP_PROXY_PORT cannot be null if you specify a Proxy Host"
						+ DynatraceMonitoringAction.HTTP_PROXY_PORT + ".", null);
		}
		if (!Strings.isNullOrEmpty(httpProxyPort)) {
			if (Strings.isNullOrEmpty(httpProxyHost))
				return getErrorResult(context, sampleResult, "Invalid argument: HTTP_PROXY_HOST cannot be null if you specify a Proxy Host"
						+ DynatraceMonitoringAction.HTTP_PROXY_HOST + ".", null);


		}

		sampleResult.sampleStart();
		try {
			pluginData = (DynatracePluginData) context.getCurrentVirtualUser().get("PLUGINDATA");
			if (pluginData == null) {

				if (Strings.isNullOrEmpty(dynatraceApiKey))
					return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_API_KEY cannot be null if the Dynatrace Plugin is enabled"
							+ DynatraceMonitoringAction.DYNATRACE_API_KEY + ".", null);

				if (Strings.isNullOrEmpty(dynatraceId))
					return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ID cannot be null if the Dynatrace Plugin is enabled"
							+ DynatraceMonitoringAction.DYNATRACE_ID + ".", null);
				if (Strings.isNullOrEmpty(dynatraceApplicationName))
					return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ApplicationName cannot be null if the Dynatrace Plugin is enabled"
							+ DynatraceMonitoringAction.DYNATRACE_APPLICATION_NAME + ".", null);

				// Delay by two seconds to ensure no conflicts in re-establishing connection
				try {
					if (!Strings.isNullOrEmpty(httpProxyPort)) {
						pluginData = new DynatracePluginData(dynatraceApiKey, context.getAccountToken(), httpProxyHost, httpProxyPort, httpProxyLogin, httpProxyPassword, context, dynatraceId, neoloadApiHost, dynatraceManagedHostname, nlManagedInstance);

					} else {
						pluginData = new DynatracePluginData(dynatraceApiKey, context.getAccountToken(), context, dynatraceId, neoloadApiHost, dynatraceManagedHostname, nlManagedInstance);

					}
					//----bug to resolve on the senteventapi
					//eventAPI.SendStartTest();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					return getErrorResult(context, sampleResult, "Technical Error PLugin/Insight API:", e);

				}
			} else
				pluginStored = true;

			if (!pluginStored)
				pluginData.StartTimer();
			else
				pluginData.ResumeTimer();
			//----check if object exists-----
			startTs = System.currentTimeMillis() - context.getElapsedTime();
			if (!Strings.isNullOrEmpty(httpProxyPort))
				dynatrace = new DynatraceIntegration(dynatraceApiKey, dynatraceId, dynatraceApplicationName, neoloadApiHost, neoloadApiport, neoloadKeyApi, httpProxyHost, httpProxyPort, httpProxyLogin, httpProxyPassword, dynatraceManagedHostname, startTs);
			else
				dynatrace = new DynatraceIntegration(dynatraceApiKey, dynatraceId, dynatraceApplicationName, neoloadApiHost, neoloadApiport, neoloadKeyApi, dynatraceManagedHostname, startTs);

			///first call send event to dynatatrfece


			sampleResult.sampleEnd();

			pluginData.StopTimer();

			if (!pluginStored)
				context.getCurrentVirtualUser().put("PLUGINDATA", pluginData);


			//---check if aggregator exists 
			// startimer
		} catch (Exception e) {
			return getErrorResult(context, sampleResult, "Techncial Error with Dynatrace Monitoringt.", e);
		}


		sampleResult.setRequestContent(requestBuilder.toString());
		sampleResult.setResponseContent(responseBuilder.toString());
		return sampleResult;
	}

	private void appendLineToStringBuilder(final StringBuilder sb, final String line) {
		sb.append(line).append("\n");
	}

	/**
	 * This method allows to easily create an error result and log exception.
	 */
	private static SampleResult getErrorResult(final Context context, final SampleResult result, final String errorMessage, final Exception exception) {
		result.setError(true);
		result.setStatusCode("NL-DynatraceMonitoring_ERROR");
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
		if (pluginData != null)
			pluginData.StopTimer();

		if (dynatrace != null)
			dynatrace.SetTestToStop();
	}

}
