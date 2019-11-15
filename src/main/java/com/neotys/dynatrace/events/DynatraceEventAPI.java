package com.neotys.dynatrace.events;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.DynatraceContext;
import com.neotys.dynatrace.common.DynatraceUtils;
import com.neotys.dynatrace.common.topology.DynatraceTopologyWalker;
import com.neotys.extensions.action.engine.Context;
import static com.neotys.dynatrace.common.Constants.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;

class DynatraceEventAPI {
	private static final String MESSAGE_NL_TEST = "Start/Stop NeoLoad Test";

	private final Set<String> serviceEntityIds;
	private final Context context;
	private final DynatraceContext dynatracecontext;
	private boolean traceMode;

	DynatraceEventAPI(final Context context,
					  final String dynatraceID,
					  final String dynatraceAPIKEY,
					  final Optional<String> dynatraceTags,
					  final Optional<String> dynatraceManagedHostname,
					  Optional<String> proxyName, final boolean traceMode)
			throws Exception {
		this.traceMode = traceMode;
		this.context = context;
		this.dynatracecontext = new DynatraceContext(dynatraceAPIKEY, dynatraceManagedHostname, dynatraceID, proxyName, dynatraceTags);
		
		DynatraceTopologyWalker dtw=new DynatraceTopologyWalker(this.context,this.dynatracecontext,this.traceMode);
		dtw.executeDiscovery();
		this.serviceEntityIds = dtw.getDiscoveredData().getServices();		
	}

	void sendMessage() throws Exception {
		long start;
		final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		start = now.toInstant().toEpochMilli() - context.getElapsedTime();
		sendMetricToEventAPI(MESSAGE_NL_TEST, start, now.toInstant().toEpochMilli());
	}

	private void sendMetricToEventAPI(final String message, final long startTime, final long endTime) throws Exception {	
		final StringBuilder entitiesBuilder = new StringBuilder();
		for (String service : serviceEntityIds) {
			entitiesBuilder.append("\"").append(service).append("\",");
		}
		final String entities = entitiesBuilder.substring(0, entitiesBuilder.length() - 1);

		final String bodyJson = "{\"start\":" + startTime + ","
				+ "\"end\":" + endTime + ","
				+ "\"eventType\": \"CUSTOM_ANNOTATION\","
				+ "\"annotationType\": \"NeoLoad Test" + context.getTestName() + "\","
				+ "\"annotationDescription\": \"" + message + " " + context.getTestName() + "\","
				+ "\"attachRules\":"
				+ "{ \"entityIds\":[" + entities + "] ,"
				+ "\"tagRule\" : {"
				+ "\"meTypes\": \"SERVICE\","
				+ "\"tags\": [\"Loadtest\", \"NeoLoad\"]"
				+ "}},"
				+ "\"source\":\"NeoLoadWeb\","
				+ "\"customProperties\":"
				+ "{ \"ScriptName\": \"" + context.getProjectName() + "\","
				+ "\"NeoLoad_TestName\":\"" + context.getTestName() + "\","
				+ "\"NeoLoad_URL\":\"" + context.getWebPlatformRunningTestUrl() + "\","
				+ "\"NeoLoad_Scenario\":\"" + context.getScenarioName() + "\"}"
				+ "}";

		DynatraceUtils.executeDynatraceAPIPostObjectRequest(context, dynatracecontext, DTAPI_ENV1_EP_EVENTS, bodyJson, traceMode);
		
		// TODO : add events to hosts / processgroups / processes
		// TODO : add the update of the events by retrieving the eventid ....required the change on the API of DYnatrace. FEATURE in PENDING
	}
}

