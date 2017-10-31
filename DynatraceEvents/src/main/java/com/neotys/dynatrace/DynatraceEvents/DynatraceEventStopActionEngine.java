package com.neotys.dynatrace.DynatraceEvents;

import java.util.List;

import com.google.common.base.Strings;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;

public final class DynatraceEventStopActionEngine implements ActionEngine {
	private  String Dynatrace_ID;
	private  String Dynatrace_API_KEY;
	private  String HTTP_PROXY_HOST;
	private  String HTTP_PROXY_PORT;
	private  String HTTP_PROXY_LOGIN;
	private  String HTTP_PROXY_PASSWORD;
	private  String Dynatrace_ApplicationName;
	private String Dynatrace_Managed_Hostname;
	private  String EventStatus;
	private static final String START="START";
	private static final String STOP="STOP";
	private DynatraceEventAPI eventAPI;
	private String NL_Managed_Instance;
	@Override
	public SampleResult execute(Context context, List<ActionParameter> parameters) {
		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();
		for(ActionParameter parameter:parameters) {
			switch(parameter.getName()) 
			{
			case  DynatraceEventAction.EventStatus:
				EventStatus = parameter.getValue();
				break;
			case  DynatraceEventAction.Dynatrace_ID:
				Dynatrace_ID = parameter.getValue();
				break;
			case  DynatraceEventAction.Dynatrace_API_KEY:
				Dynatrace_API_KEY = parameter.getValue();
				break;
			case  DynatraceEventAction.HTTP_PROXY_HOST:
				HTTP_PROXY_HOST = parameter.getValue();
				break;
			case  DynatraceEventAction.HTTP_PROXY_PASSWORD:
				HTTP_PROXY_PASSWORD = parameter.getValue();
				break;
			case  DynatraceEventAction.HTTP_PROXY_LOGIN:
				HTTP_PROXY_LOGIN = parameter.getValue();
				break;
			case  DynatraceEventAction.HTTP_PROXY_PORT:
				HTTP_PROXY_PORT = parameter.getValue();
				break;
			case  DynatraceEventAction.Dynatrace_Managed_Hostname:
				Dynatrace_Managed_Hostname = parameter.getValue();
				break;
			
			case  DynatraceEventAction.Dynatrace_ApplicationName:
				Dynatrace_ApplicationName = parameter.getValue();
				break;
				
			case  DynatraceEventAction.NL_Managed_Instance:
				NL_Managed_Instance = parameter.getValue();
				break;
			
			}
		}
		
		
		if (Strings.isNullOrEmpty(Dynatrace_API_KEY)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_API_KEY cannot be null "
					+ DynatraceEventAction.Dynatrace_API_KEY + ".", null);
		}
		if (Strings.isNullOrEmpty(Dynatrace_ApplicationName)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ApplicationName cannot be null "
					+ DynatraceEventAction.Dynatrace_ApplicationName + ".", null);
		}
		if (Strings.isNullOrEmpty(Dynatrace_ID)) {
			return getErrorResult(context, sampleResult, "Invalid argument: Dynatrace_ID cannot be null "
					+ DynatraceEventAction.Dynatrace_ID + ".", null);
		}
		if (! Strings.isNullOrEmpty(HTTP_PROXY_HOST) ) {
			if(Strings.isNullOrEmpty(HTTP_PROXY_PORT))
				return getErrorResult(context, sampleResult, "Invalid argument: HTTP_PROXY_PORT cannot be null if you specify a Proxy Host"
						+ DynatraceEventAction.HTTP_PROXY_PORT + ".", null);
		}
		if (! Strings.isNullOrEmpty(HTTP_PROXY_PORT) ) {
			if(Strings.isNullOrEmpty(HTTP_PROXY_HOST))
				return getErrorResult(context, sampleResult, "Invalid argument: HTTP_PROXY_HOST cannot be null if you specify a Proxy Host"
						+ DynatraceEventAction.HTTP_PROXY_HOST + ".", null	);
			
						
	
		}
		if (Strings.isNullOrEmpty(EventStatus)) {
			return getErrorResult(context, sampleResult, "Invalid argument: EventStatus cannot be null "
					+ DynatraceEventAction.EventStatus + ".", null);
		}
		
		
		sampleResult.sampleStart();
		try
		{
			eventAPI=new DynatraceEventAPI(Dynatrace_API_KEY, Dynatrace_ID, Dynatrace_ApplicationName, context,Dynatrace_Managed_Hostname,NL_Managed_Instance);
			
			switch(EventStatus)
			{
			case START:
				eventAPI.SendStartTest();
				break;
			case STOP:
				eventAPI.SendStopTest();				
				break;
			default: 
				return getErrorResult(context, sampleResult, "Invalid argument: EventStatus can be equal to START or STOP "
						+ DynatraceEventAction.EventStatus + ".", null);
			}
		}
		catch(Exception e)
		{
			return getErrorResult(context, sampleResult, "Technical Error encouter  :", e);
		}

		sampleResult.sampleEnd();

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
		result.setStatusCode("NL-DynatraceEvent_ERROR");
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
	}

}
