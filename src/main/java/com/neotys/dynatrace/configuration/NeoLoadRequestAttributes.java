package com.neotys.dynatrace.configuration;

//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class NeoLoadRequestAttributes {
    public static final String NEOLOAD_SCENARIO_SUFFIX="SN";
    public static final String NEOLOAD_TRANSACTION_SUFFIX="NA";
    public static final String NEOLOAD_ZONE_SUFFIX="GR";
    public static final String NEOLOAD_REQUEST_SUFFIX="PC";
    public static final String NEOLOAD_SOURCE_SUFFIX="SI";
    public static final String NEOLOAD_TOOL_SUFFIX="LST";

    public static final String NEW="NEW";
    
    public static final String NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE="NEOLOAD_Transaction";
    public static final String NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE="NEOLOAD_ScenarioName";
    public static final String NEOLOAD_ZONE_REQUEST_ATTRIBUTE="NEOLOAD_Zone";
    public static final String NEOLOAD_REQUESTS_REQUEST_ATTRIBUTE="NEOLOAD_Requests";
    public static final String NEOLOAD_HTTP_HEADER_NAME="x-dynatrace";
    public static final String[] NEOLOAD_REQUEST_ATTRIBUTE= {NEOLOAD_REQUESTS_REQUEST_ATTRIBUTE,NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE,NEOLOAD_ZONE_REQUEST_ATTRIBUTE,NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE};
    
    public static final String NEOLOAD_NEW_TRANSACTION_REQUEST_ATTRIBUTE="NeoLoad_Transaction";
    public static final String NEOLOAD_NEW_SCENARIO_REQUEST_ATTRIBUTE="NeoLoad_ScenarioName";
    public static final String NEOLOAD_NEW_ZONE_REQUEST_ATTRIBUTE="NeoLoad_Zone";
    public static final String NEOLOAD_NEW_REQUESTS_REQUEST_ATTRIBUTE="NeoLoad_Requests";
    public static final String NEOLOAD_NEW_SOURCE_REQUEST_ATTRIBUTE="NeoLoad_SourceId";
    public static final String NEOLOAD_NEW_TOOL_REQUEST_ATTRIBUTE="NeoLoad_TestTool";
    public static final String NEOLOAD_HTTP_HEADER_NAME_NEW="X-Dynatrace-Test";

    public static final String[] NEOLOAD_NEW_REQUEST_ATTRIBUTE= {NEOLOAD_NEW_REQUESTS_REQUEST_ATTRIBUTE,NEOLOAD_NEW_TRANSACTION_REQUEST_ATTRIBUTE,NEOLOAD_NEW_ZONE_REQUEST_ATTRIBUTE,NEOLOAD_NEW_SCENARIO_REQUEST_ATTRIBUTE,NEOLOAD_NEW_SOURCE_REQUEST_ATTRIBUTE,NEOLOAD_NEW_TOOL_REQUEST_ATTRIBUTE};

	public static boolean isNeoLoadRequestAttributesExists(JSONArray jsonArray, String type) {
		boolean result = false;
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = jsonArray.getJSONObject(i);
			String name = obj.getString("name");
			if (type.equalsIgnoreCase(NEW)) {
				if (Arrays.stream(NEOLOAD_NEW_REQUEST_ATTRIBUTE).parallel().anyMatch(name::contains))
					result = true;
			} else {
				if (Arrays.stream(NEOLOAD_REQUEST_ATTRIBUTE).parallel().anyMatch(name::contains))
					result = true;
			}
		}
		return result;
	}

	public static Map<String, String> generateHashMap(String type) {
		HashMap<String, String> hashMap = new HashMap<>();

		if (type.equalsIgnoreCase(NEW)) {
			hashMap.put(NEOLOAD_NEW_TRANSACTION_REQUEST_ATTRIBUTE, NEOLOAD_TRANSACTION_SUFFIX);
			hashMap.put(NEOLOAD_NEW_SCENARIO_REQUEST_ATTRIBUTE, NEOLOAD_SCENARIO_SUFFIX);
			hashMap.put(NEOLOAD_NEW_REQUESTS_REQUEST_ATTRIBUTE, NEOLOAD_REQUEST_SUFFIX);
			hashMap.put(NEOLOAD_NEW_ZONE_REQUEST_ATTRIBUTE, NEOLOAD_ZONE_SUFFIX);
			hashMap.put(NEOLOAD_NEW_SOURCE_REQUEST_ATTRIBUTE, NEOLOAD_SOURCE_SUFFIX);
			hashMap.put(NEOLOAD_NEW_TOOL_REQUEST_ATTRIBUTE, NEOLOAD_TOOL_SUFFIX);
		} else {
			hashMap.put(NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE, NEOLOAD_TRANSACTION_SUFFIX);
			hashMap.put(NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE, NEOLOAD_SCENARIO_SUFFIX);
			hashMap.put(NEOLOAD_REQUESTS_REQUEST_ATTRIBUTE, NEOLOAD_REQUEST_SUFFIX);
			hashMap.put(NEOLOAD_ZONE_REQUEST_ATTRIBUTE, NEOLOAD_ZONE_SUFFIX);
		}
		return hashMap;
	}
}
