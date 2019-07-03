package com.neotys.dynatrace.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class NeoLoadRequestAttributes {
    public static final String NEOLOAD_SCENARIO_SUFFIX="SN";
    public static final String NEOLOAD_TRANSACTION_SUFFIX="NA";
    public static final String NEOLOAD_ZONE_SUFFIX="GR";
    public static final String NEOLOAD_REQUEST_SUFFIX="PC";
    public static final String NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE="NEOLOAD_Transaction";
    public static final String NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE="NEOLOAD_ScenarioName";
    public static final String NEOLOAD_ZONE_REQUEST_ATTRIBUTE="NEOLOAD_Zone";
    public static final String NEOLOAD_REQUESTS_REQUEST_ATTRIBUTE="NEOLOAD_Requests";
    public static final String NEOLOAD_NEW_TRANSACTION_REQUEST_ATTRIBUTE="NeoLoad_Transaction";
    public static final String NEOLOAD_NEW_SCENARIO_REQUEST_ATTRIBUTE="NeoLoad_ScenarioName";
    public static final String NEOLOAD_NEW_ZONE_REQUEST_ATTRIBUTE="NeoLoad_Zone";
    public static final String NEOLOAD_NEW_REQUESTS_REQUEST_ATTRIBUTE="NeoLoad_Requests";
    public static final String NEOLOAD_HTTP_HEADER_NAME="x-dynatrace";
    public static final String NEW="NEW";
    public static final String NEOLOAD_HTTP_HEADER_NAME_NEW="x-dynatrace-test";
    public static final String[] NEOLOAD_REQUESt_ATTRIBUTE= {NEOLOAD_REQUESTS_REQUEST_ATTRIBUTE,NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE,NEOLOAD_ZONE_REQUEST_ATTRIBUTE,NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE};
    public static final String[] NEOLOAD_NEW_REQUESt_ATTRIBUTE= {NEOLOAD_NEW_REQUESTS_REQUEST_ATTRIBUTE,NEOLOAD_NEW_TRANSACTION_REQUEST_ATTRIBUTE,NEOLOAD_NEW_ZONE_REQUEST_ATTRIBUTE,NEOLOAD_NEW_SCENARIO_REQUEST_ATTRIBUTE};

    public static boolean isNeoLoadRequestAttributesExists(JSONArray jsonArray,String type)
    {
        boolean result=false;
        for(int i =0; i<jsonArray.length();i++)
        {
            JSONObject obj=jsonArray.getJSONObject(i);
            String name=obj.getString("name");
            if(type.equalsIgnoreCase(NEW)){
                if(Arrays.stream(NEOLOAD_NEW_REQUESt_ATTRIBUTE).parallel().anyMatch(name::contains))
                    result=true;
            }
            else{
                if(Arrays.stream(NEOLOAD_REQUESt_ATTRIBUTE).parallel().anyMatch(name::contains))
                    result=true;
            }

        }
        return result;
    }

    public static HashMap<String,String> getNeoLoadRequestAttributeEqualToKey(JSONArray jsonArray, List<String> key)
    {
        HashMap<String,String> requestHashMap=new HashMap<>();

        boolean result=false;
        for(int i =0; i<jsonArray.length();i++)
        {
            JSONObject obj=jsonArray.getJSONObject(i);
            String name=obj.getString("name");
            String id=obj.getString("id");

            key.stream().forEach(requestkey->{
                if(name.equals(requestkey))
                    requestHashMap.put(requestkey,id);
            });


        }
        return requestHashMap;
    }



    public static Map<String,String> generateHasMap(String type)
    {
        HashMap<String,String> hashMap=new HashMap<>();

        if(type.equalsIgnoreCase(NEW))
        {
            hashMap.put(NEOLOAD_NEW_TRANSACTION_REQUEST_ATTRIBUTE, NEOLOAD_TRANSACTION_SUFFIX);
            hashMap.put(NEOLOAD_NEW_SCENARIO_REQUEST_ATTRIBUTE, NEOLOAD_SCENARIO_SUFFIX);
            hashMap.put(NEOLOAD_NEW_REQUESTS_REQUEST_ATTRIBUTE, NEOLOAD_REQUEST_SUFFIX);
            hashMap.put(NEOLOAD_NEW_ZONE_REQUEST_ATTRIBUTE, NEOLOAD_ZONE_SUFFIX);
        }
        else {
            hashMap.put(NEOLOAD_TRANSACTION_REQUEST_ATTRIBUTE, NEOLOAD_TRANSACTION_SUFFIX);
            hashMap.put(NEOLOAD_SCENARIO_REQUEST_ATTRIBUTE, NEOLOAD_SCENARIO_SUFFIX);
            hashMap.put(NEOLOAD_REQUESTS_REQUEST_ATTRIBUTE, NEOLOAD_REQUEST_SUFFIX);
            hashMap.put(NEOLOAD_ZONE_REQUEST_ATTRIBUTE, NEOLOAD_ZONE_SUFFIX);
        }
        return hashMap;

    }
}
