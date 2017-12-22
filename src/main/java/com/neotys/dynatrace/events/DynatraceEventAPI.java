package com.neotys.dynatrace.events;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.DynatraceContext;
import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.dynatrace.common.HTTPGenerator;
import com.neotys.dynatrace.common.HttpResponseUtils;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import org.apache.http.StatusLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neotys.dynatrace.common.DynatraceUtils.*;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;


class DynatraceEventAPI {

	private static final String DYNATRACE_EVENTS_API_URL = "events";
	private static final String MESSAGE_NL_TEST = "Start/Stop NeoLoad Test";

	private final Map<String, String> headers;
	private final String dynatraceApiKey;
	private final String dynatraceAccountID;
	private final List<String> applicationEntityid;
	private final Optional<String> dynatraceManagedHostname;
	private final Optional<String> proxyName;
	private final Context context;

	DynatraceEventAPI(final Context context,
					  final String dynatraceID,
					  final String dynatraceAPIKEY,
					  final Optional<String> dynatraceTags,
					  final Optional<String> dynatraceManagedHostname,
					  Optional<String> proxyName)
			throws Exception {
		this.dynatraceAccountID = dynatraceID;
		this.dynatraceApiKey = dynatraceAPIKEY;
		this.dynatraceManagedHostname = dynatraceManagedHostname;
		this.proxyName = proxyName;
		this.headers = new HashMap<>();
		this.context = context;
		this.applicationEntityid = getApplicationEntityId(context, new DynatraceContext(dynatraceAPIKEY, dynatraceManagedHostname, dynatraceAccountID, dynatraceTags, headers), proxyName);
	}

	void sendMessage() throws Exception {
		long start;
		start = System.currentTimeMillis() - context.getElapsedTime();
		sendMetricToEventAPI(MESSAGE_NL_TEST, start, System.currentTimeMillis());
	}

	private void sendMetricToEventAPI(final String message, final long startTime, final long endTime) throws Exception {
		final String url = getDynatraceApiUrl(dynatraceManagedHostname, dynatraceAccountID) + DYNATRACE_EVENTS_API_URL;
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("Api-Token", dynatraceApiKey);

		final StringBuilder entitiesBuilder = new StringBuilder();
		for (String service : applicationEntityid) {
			entitiesBuilder.append("\"").append(service).append("\",");
		}
		final String entities = entitiesBuilder.substring(0, entitiesBuilder.length() - 1);

		final String jsonString = "{\"start\":" + startTime + ","
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

		context.getLogger().debug("dynatrace event JSON content : " + jsonString);

		final Optional<Proxy> proxy = getProxy(context, proxyName, url);
		final HTTPGenerator insightHttp = HTTPGenerator.newJsonHttpGenerator(HTTP_POST_METHOD, url, headers, parameters, proxy, jsonString);
		StatusLine statusLine;
		try {
			statusLine = insightHttp.executeAndGetStatusLine();
		} finally {
			insightHttp.closeHttpClient();
		}

		if (statusLine != null && !HttpResponseUtils.isSuccessHttpCode(statusLine.getStatusCode())) {
			throw new DynatraceException(statusLine.getReasonPhrase());
		}
	}
}

