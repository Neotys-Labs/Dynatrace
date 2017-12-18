package com.neotys.dynatrace.events;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.extensions.action.engine.Proxy;
import org.json.JSONArray;
import org.json.JSONObject;

import com.neotys.dynatrace.common.HTTPGenerator;
import com.neotys.extensions.action.engine.Context;

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;


class DynatraceEventAPI {

	private static final String DYNATRACE_EVENTS_API_URL = "events";
	private static final String DYNATRACE_URL = ".live.dynatrace.com/api/v1/";
	private static final String DYNATRACE_APPLICATION = "entity/services";
	private static final String NL_RUL_LAST = "/#!result/overview/?benchId=";
	private static final String DYNATRACE_PROTOCOL = "https://";
	private static final String START_NL_TEST = "Start NeoLoad Test";
	private static final String STOP_NL_TEST = "Stop NeoLoad Test";

	private static final int BAD_REQUEST = 400;
	private static final int UNAUTHORIZED = 403;
	private static final int NOT_FOUND = 404;
	private static final int METHOD_NOT_ALLOWED = 405;
	private static final int REQUEST_ENTITY_TOO_LARGE = 413;
	private static final int INTERNAL_SERVER_ERROR = 500;
	private static final int BAD_GATEWAY = 502;
	private static final int SERVICE_UNAVAIBLE = 503;
	private static final int GATEWAY_TIMEOUT = 504;

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
			throws DynatraceException, IOException, URISyntaxException {
		this.dynatraceAccountID = dynatraceID;
		this.dynatraceApiKey = dynatraceAPIKEY;
		this.dynatraceManagedHostname = dynatraceManagedHostname;
		this.proxyName = proxyName;
		this.headers = new HashMap<>();
		this.applicationEntityid = getApplicationEntityId(dynatraceTags);
		this.context = context;
	}

	private void addTokenInParameters(final Map<String, String> param) {
		param.put("Api-Token", dynatraceApiKey);
	}

	private String getTags(final Optional<String> tags) {
		if (tags.isPresent()) {
			final StringBuilder result = new StringBuilder();
			final String tagsAsString = tags.get();
			if (tagsAsString.contains(",")) {
				final String[] tagsArray = tagsAsString.split(",");
				for (String tag : tagsArray) {
					result.append(tag).append("AND");
				}
				return result.substring(0, result.length() - 3);
			} else {
				return tagsAsString;
			}
		}
		return "";
	}

	private List<String> getApplicationEntityId(final Optional<String> tagsParameter) throws DynatraceException, IOException, URISyntaxException {
		final String tags = getTags(tagsParameter);
		final String dynatraceUrl = getDynatraceApiUrl() + DYNATRACE_APPLICATION;
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("tag", tags);
		addTokenInParameters(parameters);

		context.getLogger().debug("Getting application...");

		final Optional<Proxy> proxy = getProxy(proxyName, dynatraceUrl);
		final HTTPGenerator http = new HTTPGenerator(HTTP_GET_METHOD, dynatraceUrl, headers, parameters, proxy);
		final List<String> applicationEntityId = new ArrayList<>();;
		try {
			final JSONArray jsonArrayResponse = http.executeAndGetJsonArrayResponse();
			if (jsonArrayResponse != null) {
				for (int i = 0; i < jsonArrayResponse.length(); i++) {
					final JSONObject jsonApplication = jsonArrayResponse.getJSONObject(i);
					if (jsonApplication.has("entityId") && jsonApplication.has("displayName")) {
						applicationEntityId.add(jsonApplication.getString("entityId"));
					}
				}
			} else {
				throw new DynatraceException("No Application find in The Dynatrace Account with the name " + tagsParameter.or(""));
			}
		} finally {
			http.closeHttpClient();
		}
		if (context.getLogger().isDebugEnabled()) {
			context.getLogger().debug("Found applications: " + applicationEntityId);
		}

		return applicationEntityId;
	}

	private Optional<Proxy> getProxy(final Optional<String> proxyName, final String url) throws MalformedURLException {
		if (proxyName.isPresent()) {
			return Optional.fromNullable(context.getProxyByName(proxyName.get(), new URL(url)));
		}
		return Optional.absent();
	}

	void sendStartTest() throws DynatraceException, IOException, URISyntaxException {
		long start;
		start = System.currentTimeMillis() - context.getElapsedTime();
		sendMetricToEventAPI(START_NL_TEST, start, System.currentTimeMillis());
	}

	private static String getTestUrlInNlWeb(final Context context) {
		// TODO get neoload web front URL
		return context.getWebPlatformApiUrl() + NL_RUL_LAST + context.getTestId();
	}

	private void sendMetricToEventAPI(final String message, final long startDuration, final long endDuration) throws DynatraceException, IOException, URISyntaxException {
		final String url = getDynatraceApiUrl() + DYNATRACE_EVENTS_API_URL;
		final Map<String, String> parameters = new HashMap<>();
		addTokenInParameters(parameters);

		final StringBuilder entitiesBuilder = new StringBuilder();
		for (String service : applicationEntityid) {
			entitiesBuilder.append("\"").append(service).append("\",");
		}
		final String entities = entitiesBuilder.substring(0, entitiesBuilder.length() - 1);

		final String jsonString = "{\"start\":" + startDuration + ","
				+ "\"end\":" + endDuration + ","
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
				// TODO get neoload web front URL
				/*+ "\"NeoLoad_URL\":\"" + getTestUrlInNlWeb(context) + "\","*/
				+ "\"NeoLoad_Scenario\":\"" + context.getScenarioName() + "\"}"
				+ "}";

		context.getLogger().debug("dynatrace event JSON content : " + jsonString);

		final Optional<Proxy> proxy = getProxy(proxyName, url);
		final HTTPGenerator insightHttp = HTTPGenerator.newJsonHttpGenerator(HTTP_POST_METHOD, url, headers, parameters, proxy, jsonString);
		try {
			final int httpCode = insightHttp.executeAndGetResponseCode();
			final String exceptionMessage = getExceptionMessageFromHttpCode(httpCode);
			if (exceptionMessage != null) {
				throw new DynatraceException(exceptionMessage);
			}
		} finally {
			insightHttp.closeHttpClient();
		}
	}

	private String getExceptionMessageFromHttpCode(final int httpCode) {
		final String exceptionMessage;
		switch (httpCode) {
			case BAD_REQUEST:
				exceptionMessage = "The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
				break;
			case UNAUTHORIZED:
				exceptionMessage = "Authentication error (no license key header, or invalid license key).";
				break;
			case NOT_FOUND:
				exceptionMessage = "Invalid URL.";
				break;
			case METHOD_NOT_ALLOWED:
				exceptionMessage = "Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
				break;
			case REQUEST_ENTITY_TOO_LARGE:
				exceptionMessage = "Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
				break;
			case INTERNAL_SERVER_ERROR:
				exceptionMessage = "Unexpected server error";
				break;
			case BAD_GATEWAY:
				exceptionMessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
				break;
			case SERVICE_UNAVAIBLE:
				exceptionMessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
				break;
			case GATEWAY_TIMEOUT:
				exceptionMessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
				break;
			default:
				exceptionMessage = null;
		}
		return exceptionMessage;
	}

	private String getDynatraceApiUrl() {
		String result;
		if (dynatraceManagedHostname != null) {
			result = DYNATRACE_PROTOCOL + dynatraceManagedHostname + "/api/v1/";
		} else {
			result = DYNATRACE_PROTOCOL + dynatraceAccountID + DYNATRACE_URL;
		}
		return result;
	}
}

