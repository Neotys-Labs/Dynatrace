package com.neotys.dynatrace.DynatraceMonitoring;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Strings;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;

public final class DynatraceMonitoringActionEngine implements ActionEngine {
	private String NeoLoadAPIHost;
	private  String NeoLoadAPIport;
	private  String NeoLoadKeyAPI;
	private  String Dynatrace_ID;
	private  String Dynatrace_API_KEY;
	private  String HTTP_PROXY_HOST;
	private  String HTTP_PROXY_PORT;
	private  String HTTP_PROXY_LOGIN;
	private  String HTTP_PROXY_PASSWORD;
	private  String Dynatrace_ApplicationName;
	private  DynatraceIntegration Dynatrace;
	private  DynatracePluginData pluginData;
	private  boolean PluginStored=false;
	private String Dynatrace_Managed_Hostname;
	private String NL_Managed_Instance;
	@Override
	public SampleResult execute(Context context, List<ActionParameter> parameters) {
		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();
		long 	Start_TS;
		for(ActionParameter parameter:parameters) {
			switch(parameter.getName()) 
			{
			case  DynatraceMonitoringAction.NeoLoadAPIHost:
				NeoLoadAPIHost = parameter.getValue();
				break;
			case  DynatraceMonitoringAction.NeoLoadAPIport:
				NeoLoadAPIport = parameter.getValue();
				break;
			case  DynatraceMonitoringAction.NeoLoadKeyAPI:
				NeoLoadKeyAPI = parameter.getValue();
				break;
			
			case  DynatraceMonitoringAction.Dynatrace_ID:
				Dynatrace_ID = parameter.getValue();
				break;
			case  DynatraceMonitoringAction.Dynatrace_API_KEY:
				Dynatrace_API_KEY = parameter.getValue();
				break;
			case  DynatraceMonitoringAction.HTTP_PROXY_HOST:
				HTTP_PROXY_HOST = parameter.getValue();
				break;
			case  DynatraceMonitoringAction.HTTP_PROXY_PASSWORD:
				HTTP_PROXY_PASSWORD = parameter.getValue();
				break;
			case  DynatraceMonitoringAction.HTTP_PROXY_LOGIN:
				HTTP_PROXY_LOGIN = parameter.getValue();
				break;
			case  DynatraceMonitoringAction.HTTP_PROXY_PORT:
				HTTP_PROXY_PORT = parameter.getValue();
				break;
			case  DynatraceMonitoringAction.Dynatrace_Managed_Hostname:
				Dynatrace_Managed_Hostname = parameter.getValue();
				break;
			
			case  DynatraceMonitoringAction.Dynatrace_ApplicationName:
				Dynatrace_ApplicationName = parameter.getValue();
				break;
				
			case  DynatraceMonitoringAction.NL_Managed_Instance:
				NL_Managed_Instance = parameter.getValue();
				break;
			
			
			}
		}
		if (Strings.isNullOrEmpty(NeoLoadAPIHost)) {
			return getErrorResult(context, sampleResult, "Invalid argument: NeoLoadAPIHost cannot be null "
					+ DynatraceMonitoringAction.NeoLoadAPIHost + ".", null);
		}
		if (Strings.isNullOrEmpty(NeoLoadAPIport)) {
			return getErrorResult(context, sampleResult, "Invalid argument: NeoLoadAPIport cannot be null "
					+ DynatraceMonitoringAction.NeoLoadAPIport + ".", null);
		}
		else
		{
			try
			{
				int test= Integer.parseInt(NeoLoadAPIport);
			}
			catch(Exception e)
			{
				return getErrorResult(context, sampleResult, "Invalid argument: NeoLoadAPIport needs to be an Integer "
						+ DynatraceMonitoringAction.NeoLoadAPIport + ".", null);
			}
			
		}
		
		if (Strings.isNullOrEmpty(Dynatrace_API_KEY)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_API_KEY cannot be null "
					+ DynatraceMonitoringAction.Dynatrace_API_KEY + ".", null);
		}
		if (Strings.isNullOrEmpty(Dynatrace_ApplicationName)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ApplicationName cannot be null "
					+ DynatraceMonitoringAction.Dynatrace_ApplicationName + ".", null);
		}
		if (Strings.isNullOrEmpty(Dynatrace_ID)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ID cannot be null "
					+ DynatraceMonitoringAction.Dynatrace_ID + ".", null);
		}
		if (! Strings.isNullOrEmpty(HTTP_PROXY_HOST) ) {
			if(Strings.isNullOrEmpty(HTTP_PROXY_PORT))
				return getErrorResult(context, sampleResult, "Invalid argument: HTTP_PROXY_PORT cannot be null if you specify a Proxy Host"
						+ DynatraceMonitoringAction.HTTP_PROXY_PORT + ".", null);
		}
		if (! Strings.isNullOrEmpty(HTTP_PROXY_PORT) ) {
			if(Strings.isNullOrEmpty(HTTP_PROXY_HOST))
				return getErrorResult(context, sampleResult, "Invalid argument: HTTP_PROXY_HOST cannot be null if you specify a Proxy Host"
						+ DynatraceMonitoringAction.HTTP_PROXY_HOST + ".", null	);
			
						
	
		}
		
		sampleResult.sampleStart();
		try
		{
			pluginData =(DynatracePluginData)context.getCurrentVirtualUser().get("PLUGINDATA");
			if(pluginData == null)
			{
				
				if(Strings.isNullOrEmpty(Dynatrace_API_KEY))
					return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_API_KEY cannot be null if the Dynatrace Plugin is enabled"
							+ DynatraceMonitoringAction.Dynatrace_API_KEY + ".", null);
			
				if(Strings.isNullOrEmpty(Dynatrace_ID))
					return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ID cannot be null if the Dynatrace Plugin is enabled"
							+ DynatraceMonitoringAction.Dynatrace_ID + ".", null);
				if(Strings.isNullOrEmpty(Dynatrace_ApplicationName))
					return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ApplicationName cannot be null if the Dynatrace Plugin is enabled"
							+ DynatraceMonitoringAction.Dynatrace_ApplicationName + ".", null);
				
			    // Delay by two seconds to ensure no conflicts in re-establishing connection
				try {
					if(!Strings.isNullOrEmpty(HTTP_PROXY_PORT))
					{
						pluginData=new DynatracePluginData(Dynatrace_API_KEY,context.getAccountToken(),HTTP_PROXY_HOST,HTTP_PROXY_PORT,HTTP_PROXY_LOGIN,HTTP_PROXY_PASSWORD,context,Dynatrace_ID, NeoLoadAPIHost,Dynatrace_Managed_Hostname,NL_Managed_Instance);
						
					}
					else
					{
						pluginData=new DynatracePluginData(Dynatrace_API_KEY,context.getAccountToken(),context,Dynatrace_ID, NeoLoadAPIHost,Dynatrace_Managed_Hostname,NL_Managed_Instance);
						
					}
					//----bug to resolve on the senteventapi
					//eventAPI.SendStartTest();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					return getErrorResult(context, sampleResult, "Technical Error PLugin/Insight API:", e);
				
				}
			}
			else
				PluginStored=true;
			
			if(!PluginStored)
				pluginData.StartTimer();
			else
				pluginData.ResumeTimer();
			//----check if object exists-----
			Start_TS=System.currentTimeMillis()-context.getElapsedTime();
			if(!Strings.isNullOrEmpty(HTTP_PROXY_PORT))
				Dynatrace = new DynatraceIntegration(Dynatrace_API_KEY, Dynatrace_ID,Dynatrace_ApplicationName, NeoLoadAPIHost, NeoLoadAPIport, NeoLoadKeyAPI, HTTP_PROXY_HOST, HTTP_PROXY_PORT, HTTP_PROXY_LOGIN, HTTP_PROXY_PASSWORD,Dynatrace_Managed_Hostname,Start_TS);
			else
				Dynatrace = new DynatraceIntegration(Dynatrace_API_KEY, Dynatrace_ID,Dynatrace_ApplicationName, NeoLoadAPIHost, NeoLoadAPIport, NeoLoadKeyAPI, Dynatrace_Managed_Hostname,Start_TS);
					
			///first call send event to dynatatrfece
			
			
			sampleResult.sampleEnd();
			
			pluginData.StopTimer();
			
			if(!PluginStored)
				context.getCurrentVirtualUser().put("PLUGINDATA",pluginData);
			
			
			//---check if aggregator exists 
			// startimer
		}
		catch(Exception e)
		{
			return getErrorResult(context, sampleResult, "Techncial Error with Dynatrace Monitoringt.", e	);
		}
		// TODO perform execution.

	
		sampleResult.setRequestContent(requestBuilder.toString());
		sampleResult.setResponseContent(responseBuilder.toString());
		return sampleResult;
	}

	private void appendLineToStringBuilder(final StringBuilder sb, final String line){
		sb.append(line).append("\n");
	}

	/**
	 * This method allows to easily create an error result and log exception.
	 */
	private static SampleResult getErrorResult(final Context context, final SampleResult result, final String errorMessage, final Exception exception) {
		result.setError(true);
		result.setStatusCode("NL-DynatraceMonitoring_ERROR");
		result.setResponseContent(errorMessage);
		if(exception != null){
			context.getLogger().error(errorMessage, exception);
		} else{
			context.getLogger().error(errorMessage);
		}
		return result;
	}

	@Override
	public void stopExecute() {
		// TODO add code executed when the test have to stop.
		if(pluginData!=null)
			pluginData.StopTimer();
		
		if(Dynatrace!=null)
			Dynatrace.SetTestToStop();
	}

}
