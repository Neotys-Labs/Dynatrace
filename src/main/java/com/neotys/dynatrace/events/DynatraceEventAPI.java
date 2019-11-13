package com.neotys.dynatrace.events;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.Api;
import com.neotys.dynatrace.common.DynatraceContext;
//import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.dynatrace.common.DynatraceUtils;
//import com.neotys.dynatrace.common.HTTPGenerator;
//import com.neotys.dynatrace.common.HttpResponseUtils;
import com.neotys.dynatrace.common.topology.DynatraceTopologyWalker;
import com.neotys.extensions.action.engine.Context;
//import com.neotys.extensions.action.engine.Proxy;
//import org.apache.http.HttpResponse;
//import org.json.JSONArray;
//import org.json.JSONObject;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
import java.util.Set;

//import javax.ws.rs.core.MultivaluedHashMap;
//import javax.ws.rs.core.MultivaluedMap;

//import static com.neotys.dynatrace.common.DynatraceUtils.*;
//import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;


class DynatraceEventAPI {

	private static final String DYNATRACE_EVENTS_API_URL = "events";
	private static final String MESSAGE_NL_TEST = "Start/Stop NeoLoad Test";

//	private final Map<String, String> headers;
//	private final String dynatraceApiKey;
//	private final String dynatraceAccountID;
	private final Set<String> serviceEntityIds;
//	private final Optional<String> dynatraceManagedHostname;
//	private final Optional<String> proxyName;
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
//		this.dynatraceAccountID = dynatraceID;
//		this.dynatraceApiKey = dynatraceAPIKEY;
//		this.dynatraceManagedHostname = dynatraceManagedHostname;
//		this.proxyName = proxyName;
		this.traceMode = traceMode;
//		this.headers = new HashMap<>();
		this.context = context;
		this.dynatracecontext = new DynatraceContext(dynatraceAPIKEY, dynatraceManagedHostname, dynatraceID, proxyName, /*getDynatracetag(*/dynatraceTags/*)*/, new HashMap<>());
		
		DynatraceTopologyWalker dtw=new DynatraceTopologyWalker(this.context,this.dynatracecontext,this.traceMode);
		
		dtw.executeDiscovery();
		this.serviceEntityIds = dtw.getDiscoveredData().getServices();
		
//		this.serviceEntityIds = getServiceEntityIds(context, dynatracecontext, proxyName, this.traceMode);
	}
/*
	private Optional<String> getDynatracetag(Optional<String> tag) {
		Optional<String> result;
		if(tag.isPresent()) {
			result = Optional.of(tag.get().replaceAll(":", ":NL"));
			if(!result.get().contains(":"))
				result=Optional.of("NL"+tag.get());
		}
		else
			result=Optional.absent();

		return result;
	}
*/	
	void sendMessage() throws Exception {
		long start;
		final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		start = now.toInstant().toEpochMilli() - context.getElapsedTime();
		sendMetricToEventAPI(MESSAGE_NL_TEST, start, now.toInstant().toEpochMilli());
	}

	private void sendMetricToEventAPI(final String message, final long startTime, final long endTime) throws Exception {	
/*		
		final String url = getDynatraceEnv1ApiUrl(dynatraceManagedHostname, dynatraceAccountID) + DYNATRACE_EVENTS_API_URL;
		final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
//		List<String> eventids=new ArrayList<>();
		parameters.add("Api-Token", dynatraceApiKey);
*/
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

		DynatraceUtils.executeDynatraceAPIPostObjectRequest(context, dynatracecontext, Api.ENV1, DYNATRACE_EVENTS_API_URL, bodyJson, traceMode);
		
		// TODO : add events to hosts / processgroups / processes
		// TODO : add the update of the events by retrieving the eventid ....required the change on the API of DYnatrace. FEATURE in PENDING

/*
		final Optional<Proxy> proxy = getProxy(context, proxyName, url);
		final HTTPGenerator insightHttp = new HTTPGenerator(HTTP_POST_METHOD, url, headers, parameters, proxy, bodyJson);
		HttpResponse httpResponse;
		try {
			if(traceMode){
				context.getLogger().info("Dynatrace service, event:\n" + insightHttp.getRequest() + "\n" + bodyJson);
			}
			httpResponse = insightHttp.execute();

			if (httpResponse != null && !HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode())) {
				final String stringResponse = HttpResponseUtils.getStringResponse(httpResponse);
				throw new DynatraceException(httpResponse.getStatusLine().getReasonPhrase() + " - "+ url + " - "+ bodyJson + " - " + stringResponse);
			}
			//#TODO add the update of the events by retrieving the eventid ....required the change on the API of DYnatrace. FEATURE in PENDING
*/
			/*else
			{
				if(httpResponse!=null)
				{
					JSONObject jsonObject= HttpResponseUtils.getJsonResponse(httpResponse);
					if(jsonObject!=null)
					{
						JSONArray jsonArray=jsonObject.getJSONArray("storedEventIds");
						for(int j=0;j<jsonArray.length();j++)
						{
							eventids.add(jsonArray.getString(j));
						}
					}
				}
			}*/
/*		
		} finally {
			insightHttp.closeHttpClient();
		}
*/		
	}
}

