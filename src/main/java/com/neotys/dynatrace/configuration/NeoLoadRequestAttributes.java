package com.neotys.dynatrace.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NeoLoadRequestAttributes {
    public static final String NEOLOAD_SCENARIO_SUFFIX="SN";
    public static final String NEOLOAD_TRANSACTION_SUFFIX="NA";
    public static final String NEOLOAD_ZONE_SUFFIX="GR";
    public static final String NEOLOAD_REQUEST_SUFFIX="PC";
    public static final String NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE="NEOLOAD_Transaction";
    public static final String NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE="NEOLOAD_ScenarioName";
    public static final String NEOLOAD_ZONE_REQUEST_ATTRIBUTE="NEOLOAD_Zone";
    public static final String NEOLOAD_REQUESTS_REQUEST_ATTRIBUTE="NEOLOAD_Requests";
    public static final String NEOLOAD_HTTP_HEADER_NAME="x-dynatrace";

    public static final String[] NEOLOAD_REQUESt_ATTRIBUTE= {NEOLOAD_REQUESTS_REQUEST_ATTRIBUTE,NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE,NEOLOAD_ZONE_REQUEST_ATTRIBUTE,NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE};

    public static boolean isNeoLoadRequestAttributesExists(JSONArray jsonArray)
    {
        boolean result=false;
        for(int i =0; i<jsonArray.length();i++)
        {
            JSONObject obj=jsonArray.getJSONObject(i);
            String name=obj.getString("name");
            if(Arrays.stream(NEOLOAD_REQUESt_ATTRIBUTE).parallel().anyMatch(name::contains))
                result=true;
        }
        return result;
    }

    public static Map<String,String> generateHasMap()
    {
        HashMap<String,String> hashMap=new HashMap<>();

        hashMap.put(NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE,NEOLOAD_TRANSACTION_SUFFIX);
        hashMap.put(NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE,NEOLOAD_SCENARIO_SUFFIX);
        hashMap.put(NEOLOAD_REQUESTS_REQUEST_ATTRIBUTE,NEOLOAD_REQUEST_SUFFIX);
        hashMap.put(NEOLOAD_ZONE_REQUEST_ATTRIBUTE,NEOLOAD_ZONE_SUFFIX);

        return hashMap;

    }
}
