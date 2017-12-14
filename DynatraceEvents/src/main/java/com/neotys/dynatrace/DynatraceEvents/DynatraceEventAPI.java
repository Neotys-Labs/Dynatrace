package com.neotys.dynatrace.DynatraceEvents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.neotys.NewRelic.HttpUtils.HTTPGenerator;
import com.neotys.extensions.action.engine.Context;


class DynatraceEventAPI {

	private static final String DYNATRACE_API_URL = "events";
	private static final String DYNATRACE_URL = ".live.dynatrace.com/api/v1/";
	private static final String DynatraceApplication = "entity/services";
	private static final String NL_URL_START = "https://";
	private static final String NL_SAAS = "neoload.saas.neotys.com";
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

	private Map<String, String> headers = null;
	private HTTPGenerator http;
	private String dynatraceApplicationName;
	private String componentsName;
	private final String dynatraceApiKey;
	private final String dynatraceAccountID;
	private final String testName;
	private final String testID;
	private final List<String> applicationEntityid;
	private final String nlScenarioName;
	private final String dynatraceManagedHostname;
	private final String nlProject;
	private final Context context;
	private final String nlInstance;

	public DynatraceEventAPI(final String dynatraceAPIKEY,
							 final String dynatraceID,
							 final String dynatraceApplicationName,
							 final Context context,
							 final String dynatraceManaged,
							 final String nlManagedInstance) throws DynatraceException, IOException {
		this.dynatraceAccountID = dynatraceID;
		this.dynatraceApiKey = dynatraceAPIKEY;
		this.dynatraceManagedHostname = dynatraceManaged;
		this.nlInstance = nlManagedInstance;
		initHttpClient();
		this.applicationEntityid = getApplicationID(dynatraceApplicationName);
		this.testName = context.getTestName();
		this.nlScenarioName = context.getScenarioName();
		this.testID = context.getTestId();
		this.nlProject = context.getProjectName();
		this.context = context;
	}

	private void initHttpClient() {
		headers = new HashMap<>();
		//	headers.put("X-License-Key", NewRElicLicenseKey);
		//headers.put("Content-Type", "application/json");
		//headers.put("Accept","application/json");

	}

	public void addTokenInParameters(final Map<String, String> param) {
		param.put("Api-Token", dynatraceApiKey);
	}


	public String getTags(final String applicationName) {
		StringBuilder result = new StringBuilder();
		String[] tagstable = null;
		if (applicationName != null) {

			if (applicationName.contains(",")) {
				tagstable = applicationName.split(",");
				for (String tag : tagstable) {
					result.append(tag).append("AND");
				}
				result = new StringBuilder(result.substring(0, result.length() - 3));
			} else
				result = new StringBuilder(applicationName);
		}
		return result.toString();

	}

	public List<String> getApplicationID(String applicationName) throws DynatraceException, IOException {
		JSONArray jsoobj;
		String Url;
		JSONObject jsonApplication;
		HashMap<String, String> Parameters;
		String tags = getTags(applicationName);
		Url = getApiUrl() + DynatraceApplication;
		Parameters = new HashMap<String, String>();
		Parameters.put("tag", tags);
		addTokenInParameters(Parameters);
		//initHttpClient();
	/*	if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
			http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, headers,Parameters );
		else*/
		http = new HTTPGenerator(Url, "GET", headers, Parameters);


		jsoobj = http.getJSONArrayHTTPresponse();
		List<String> applicationEntityid;
		if (jsoobj != null) {
			applicationEntityid = new ArrayList<String>();
			for (int i = 0; i < jsoobj.length(); i++) {
				jsonApplication = jsoobj.getJSONObject(i);
				if (jsonApplication.has("entityId")) {
					if (jsonApplication.has("displayName")) {

						applicationEntityid.add(jsonApplication.getString("entityId"));

					}

				}

			}


		} else
			applicationEntityid = null;

		if (applicationEntityid == null)
			throw new DynatraceException("No Application find in The Dynatrace Account with the name " + dynatraceApplicationName);

		http.closeHttpClient();

		return applicationEntityid;

	}

	public void sendStartTest() throws DynatraceException {
		long start;
		start = System.currentTimeMillis() - context.getElapsedTime();
		sendMetricToEventAPI(START_NL_TEST, start, System.currentTimeMillis());
	}

	public void sendStopTest() throws DynatraceException {
		long start;
		start = System.currentTimeMillis() - context.getElapsedTime();
		sendMetricToEventAPI(STOP_NL_TEST, start, System.currentTimeMillis());
	}

	private String getNlUrl() {
		String result;
		if (nlInstance != null) {
			result = NL_URL_START + nlInstance + NL_RUL_LAST;
		} else {
			result = NL_URL_START + NL_SAAS + NL_RUL_LAST;
		}
		return result;
	}

	private void sendMetricToEventAPI(final String message, final long startDuration, final long endDuration) throws DynatraceException {
		int httpcode;
		Map<String, String> parameters = new HashMap<>();
		HTTPGenerator insightHttp;
		String url = getApiUrl() + DYNATRACE_API_URL;
		addTokenInParameters(parameters);
		String exceptionMessage = null;
		long duration = System.currentTimeMillis();
		String entities = "";

		for (String service : applicationEntityid) {
			entities += "\"" + service + "\",";
		}
		entities = entities.substring(0, entities.length() - 1);

		String jsonString = "{\"start\":" + startDuration + ","
				+ "\"end\":" + endDuration + ","
				+ "\"eventType\": \"CUSTOM_ANNOTATION\","
				+ "\"annotationType\": \"NeoLoad Test" + testName + "\","
				+ "\"annotationDescription\": \"" + message + " " + testName + "\","
				+ "\"attachRules\":"
				+ "{ \"entityIds\":[" + entities + "] ,"
				+ "\"tagRule\" : {"
				+ "\"meTypes\": \"SERVICE\","
				+ "\"tags\": [\"Loadtest\", \"NeoLoad\"]"
				+ "}},"
				+ "\"source\":\"NeoLoadWeb\","
				+ "\"customProperties\":"
				+ "{ \"ScriptName\": \"" + nlProject + "\","
				+ "\"NeoLoad_TestName\":\"" + testName + "\","
				+ "\"NeoLoad_URL\":\"" + NL_URL_START + NL_SAAS + "\","
//-----------wait for patch on the dynatrace UI to send the exact link
				//	+ "\"NeoLoad_URL\":\""+getNlUrl()+testID+"\","
				+ "\"NeoLoad_Scenario\":\"" + nlScenarioName + "\"}"
				+ "}";

		System.out.println(" Payload : " + jsonString);


		insightHttp = new HTTPGenerator("POST", url, headers, parameters, jsonString);
		try {
			httpcode = insightHttp.getHttpResponseCodeFromResponse();
			switch (httpcode) {

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

			}
			if (exceptionMessage != null)
				throw new DynatraceException(exceptionMessage);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		insightHttp.closeHttpClient();

	}

	private String getApiUrl() {
		String result;

		if (dynatraceManagedHostname != null) {
			result = DYNATRACE_PROTOCOL + dynatraceManagedHostname + "/api/v1/";
		} else {
			result = DYNATRACE_PROTOCOL + dynatraceAccountID + DYNATRACE_URL;
		}
		return result;
	}
}

