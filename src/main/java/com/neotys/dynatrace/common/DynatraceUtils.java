package com.neotys.dynatrace.common;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.tag.DynatraceTaggingUtils;
import com.neotys.dynatrace.monitoring.timeseries.DynatraceMetric;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.extensions.action.engine.ProxyType;
import org.apache.http.HttpResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_DELETE_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_PUT_METHOD;
import static com.neotys.dynatrace.common.HttpResponseUtils.getJsonArrayResponse;
import static com.neotys.dynatrace.common.HttpResponseUtils.getJsonResponse;

import static com.neotys.dynatrace.common.Constants.*;

/**
 * Created by anouvel on 20/12/2017.
 */

public class DynatraceUtils {
    private static final ImageIcon DYNATRACE_ICON;
    static {
        final URL iconURL = DynatraceUtils.class.getResource("dynatrace.png");
        if (iconURL != null) {
            DYNATRACE_ICON = new ImageIcon(iconURL);
        } else {
            DYNATRACE_ICON = null;
        }
    }

    private DynatraceUtils() {
    }

    public static ImageIcon getDynatraceIcon() {
        return DYNATRACE_ICON;
    }

	public static String cleanUpTags(String tag) {
		if (tag != null) {
			return tag.replaceAll(":", "\\\\:");
		} else
			return null;
	}

    public static MultivaluedMap<String,String> generateGetTagsParameters(Optional<String> tags,boolean usetag) {
    	MultivaluedMap<String,String> parameters=new MultivaluedHashMap<>();
        if(usetag) {
            if (tags.isPresent()) {
            	String tagcsv=tags.get();
            	String[] tagvalues=tagcsv.split(",");
            	for (String tag:tagvalues)
            		addGetParameterTag(parameters,tag);
            }
        }
        return parameters;
    }

    public static void addGetParameterTag(MultivaluedMap<String,String> params,String tag) {
    	params.add(PARAM_TAG,cleanUpTags(tag));
    }
    
    public static void addGetParameterEntity(MultivaluedMap<String,String> params,String entity) {
    	params.add(PARAM_ENTITY,entity);
    }

    private static long getUtcDate(Optional<Long> delta) {
        long diff;
        if(delta.isPresent())
            diff=delta.get();
        else
            diff=DYNATRACE_DEFAULT_DIFF;

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        long timeInMillisSinceEpoch123 = now.toInstant().toEpochMilli();
        timeInMillisSinceEpoch123 -= diff;
        return timeInMillisSinceEpoch123;
    }

    public static List<DynatraceMetric> getTimeSeriesMetricData(final String timeSeries,
                                                                final String aggregate, final Set<String> listEntityId, final long startTS, final Context context, final DynatraceContext dynatraceContext, final boolean traceMode, final Optional<Long> difftimeseries,final Optional<Boolean> noDataExchangeAPI)
            throws Exception {
    	
        final List<DynatraceMetric> metrics = new ArrayList<>();
    	if (listEntityId.isEmpty())
    		return metrics;

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        final StringBuilder jsonEntitiesBuilder = new StringBuilder().append("{");
        if(aggregate != null){
            jsonEntitiesBuilder.append("\"aggregationType\": \"").append(aggregate.toLowerCase()).append("\",");
        }
        jsonEntitiesBuilder
                .append("\"timeseriesId\" : \"").append(timeSeries).append("\",")
                .append("\"endTimestamp\":\"").append(String.valueOf(now.toInstant().toEpochMilli())).append("\",")
                .append("\"startTimestamp\":\"").append(String.valueOf(getUtcDate(difftimeseries))).append("\",")
                .append("\"entities\":[");

        for (String entit : listEntityId) {
            jsonEntitiesBuilder.append("\"").append(entit).append("\",");
        }

        final String bodyJson = jsonEntitiesBuilder.substring(0, jsonEntitiesBuilder.length() - 1) + "]}";

        
            JSONObject jsonMetricResults=executeDynatraceAPIPostObjectRequest(context, dynatraceContext, DTAPI_ENV1_EP_TIMESERIES + "/" + timeSeries, bodyJson, traceMode);

            if(traceMode){
                context.getLogger().info("Dynatrace service, get timeseries:\n" + DTAPI_ENV1_EP_TIMESERIES + "\n" + bodyJson);
            }
                if (jsonMetricResults == null || !jsonMetricResults.has("result")) {
                    context.getLogger().debug("No timeseries found.");
                    return Collections.emptyList();
                }
                jsonMetricResults = jsonMetricResults.getJSONObject("result");
                if (jsonMetricResults.has("dataPoints") && jsonMetricResults.has("entities")) {
                    final JSONObject jsonEntity = jsonMetricResults.getJSONObject("entities");
                    final Map<String, String> entities = getEntityDefinition(jsonEntity);

                    final JSONObject jsonDataPoint = jsonMetricResults.getJSONObject("dataPoints");
                    final Iterator<?> keysIterator = jsonDataPoint.keys();
                    while (keysIterator.hasNext()) {
                        final String entity = (String) keysIterator.next();
                        final String displayName = getEntityDisplayName(entities, entity);
                        final JSONArray arr = jsonDataPoint.getJSONArray(entity);
                        addDataMetrics(startTS,metrics, jsonMetricResults, entity, displayName, arr,noDataExchangeAPI);
                    }
                }
                if(metrics.isEmpty() && traceMode){
                    context.getLogger().info("No timeseries found.");
                }
        return metrics;
    }

    private static HashMap<String, String> getEntityDefinition(final JSONObject entity) {
        final HashMap<String, String> result = new HashMap<>();
        final Iterator<?> keysIterator = entity.keys();
        while (keysIterator.hasNext()) {
            final Object key = keysIterator.next();
            result.put((String) key, (String) entity.get((String) key));
        }
        return result;
    }

    private static String getEntityDisplayName(final Map<String, String> map, final String entity) {
        final String[] entities = entity.split(",");
        for (Map.Entry<String, String> e : map.entrySet()) {
            for (String entityFromMap : entities) {
                if (entityFromMap.equalsIgnoreCase(e.getKey())) {
                    return e.getValue();
                }
            }
        }
        return null;
    }

    private static void addDataMetrics(final long startTS, final List<DynatraceMetric> metrics, final JSONObject jsonApplication,
                                final String entity, final String displayName, final JSONArray jsonArray,final Optional<Boolean> noDataExchangeApi) {
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONArray data = jsonArray.getJSONArray(i);
            if (data.get(1) instanceof Double) {
                final long time = data.getLong(0);
                DateTime utcTime = new DateTime(time, DateTimeZone.UTC);
                final DateTime localTime = utcTime.withZone(DateTimeZone.getDefault());

                if(noDataExchangeApi.isPresent()) {
                    if(noDataExchangeApi.get()) {
                        final String unit = jsonApplication.getString("unit");
                        final double value = data.getDouble(1);
                        final String timeseriesId = jsonApplication.getString("timeseriesId");
                        final DynatraceMetric metric = new DynatraceMetric(unit, value, localTime.getMillis(), displayName, timeseriesId, entity);
                        metrics.add(metric);
                    } else                     {
                        if (time >= startTS) {
                            final String unit = jsonApplication.getString("unit");
                            final double value = data.getDouble(1);
                            final String timeseriesId = jsonApplication.getString("timeseriesId");
                            final DynatraceMetric metric = new DynatraceMetric(unit, value, localTime.getMillis(), displayName, timeseriesId, entity);
                            metrics.add(metric);
                        }
                    }
                } else {
                    if (time >= startTS) {
                        final String unit = jsonApplication.getString("unit");
                        final double value = data.getDouble(1);
                        final String timeseriesId = jsonApplication.getString("timeseriesId");
                        final DynatraceMetric metric = new DynatraceMetric(unit, value, localTime.getMillis(), displayName, timeseriesId, entity);
                        metrics.add(metric);
                    }
                }
            }
        }
    }

    public static void updateTagOnServiceID(final Context context, final DynatraceContext dynatraceContext, final boolean traceMode,String serviceID) throws Exception {
        updateTagsOnEntity(context,dynatraceContext,traceMode,DTAPI_ENV1_EP_SERVICE+"/"+serviceID);
    }

    public static void updateTagOnPgInstanceID(final Context context, final DynatraceContext dynatraceContext, final boolean traceMode,String pginstanceid) throws Exception {
        updateTagsOnEntity(context,dynatraceContext,traceMode,DTAPI_ENV1_EP_PROCESS+"/"+pginstanceid);
    }
    
    public static void updateTagOnProcessGroupID(final Context context, final DynatraceContext dynatraceContext, final boolean traceMode,String processgroupid) throws Exception {
        updateTagsOnEntity(context,dynatraceContext,traceMode,DTAPI_ENV1_EP_PROCESSGROUP+"/"+processgroupid);
    }

    public static void updateTagOnHostID(final Context context, final DynatraceContext dynatraceContext, final boolean traceMode,String hostid) throws Exception {
        updateTagsOnEntity(context,dynatraceContext,traceMode,DTAPI_ENV1_EP_HOST+"/"+hostid);
    }
    
    private static void updateTagsOnEntity(final Context context, final DynatraceContext dynatraceContext, final boolean traceMode,String endpoint) throws Exception {
        String jsonpayloadstart;
        String jsonpayloadend;
        String jsonpayload;

        if(dynatraceContext.getTags().isPresent()) {

            jsonpayloadstart="{\n" +
                    "  \"tags\": [\n";

            jsonpayloadend ="  ]\n" +
                    "}";

            jsonpayload=jsonpayloadstart+ DynatraceTaggingUtils.convertforUpdateTags(dynatraceContext.getTags()) + jsonpayloadend;
            
            DynatraceUtils.executeDynatraceAPIPostObjectRequest(context, dynatraceContext, endpoint, jsonpayload, traceMode);
        }
    }

    public static Optional<Proxy> getProxy(final Context context, final Optional<String> proxyName, final String url) throws MalformedURLException {
        if (proxyName.isPresent()) {
            return Optional.fromNullable(context.getProxyByName(proxyName.get(), new URL(url)));
        }
        return Optional.absent();
    }

    public static Optional<Proxy> getNeoLoadWebProxy(final Context context, final String url) throws MalformedURLException {
       return Optional.fromNullable(context.getProxyByType(ProxyType.NEOLOAD_WEB, new URL(url)));
    }
    
    public static String getDynatraceApiUrl(final Optional<String> dynatraceManagedHostname, final String dynatraceAccountID) {
        if (dynatraceManagedHostname.isPresent()) {
            return DYNATRACE_PROTOCOL + dynatraceManagedHostname.get() + "/e/" + dynatraceAccountID;
        } else {
            return DYNATRACE_PROTOCOL + dynatraceAccountID;
        }
    }

/*   
    @Deprecated
    public static String getDynatraceAnyApiUrl(final Optional<String> dynatraceManagedHostname, final String dynatraceAccountID, Api api) {
		switch (api) {
		case ENV1:
			return getDynatraceEnv1ApiUrl(dynatraceManagedHostname,dynatraceAccountID);
		case ENV2:
			return getDynatraceEnv2ApiUrl(dynatraceManagedHostname,dynatraceAccountID);
		case CFG:
		default:
			return getDynatraceConfigApiUrl(dynatraceManagedHostname,dynatraceAccountID);
		}
    }
    
    @Deprecated
    public static String getDynatraceEnv1ApiUrl(final Optional<String> dynatraceManagedHostname, final String dynatraceAccountID) {
        if (dynatraceManagedHostname.isPresent()) {
            return DYNATRACE_PROTOCOL + dynatraceManagedHostname.get() + "/e/" + dynatraceAccountID + DTAPI_ENV1_PREFIX;
        } else {
            return DYNATRACE_PROTOCOL + dynatraceAccountID + DTAPI_ENV1_PREFIX;
        }
    }

    @Deprecated
    public static String getDynatraceEnv2ApiUrl(final Optional<String> dynatraceManagedHostname, final String dynatraceAccountID) {
        if (dynatraceManagedHostname.isPresent()) {
            return DYNATRACE_PROTOCOL + dynatraceManagedHostname.get() + "/e/" + dynatraceAccountID + DTAPI_ENV2_PREFIX;
        } else {
            return DYNATRACE_PROTOCOL + dynatraceAccountID + DTAPI_ENV2_PREFIX;
        }
    }

    @Deprecated
    public static String getDynatraceConfigApiUrl(final Optional<String> dynatraceManagedHostname, final String dynatraceAccountID) {
        if (dynatraceManagedHostname.isPresent()) {
            return DYNATRACE_PROTOCOL + dynatraceManagedHostname.get() + "/e/" + dynatraceAccountID + DTAPI_CFG_PREFIX;
        } else {
            return DYNATRACE_PROTOCOL + dynatraceAccountID + DTAPI_CFG_PREFIX;
        }
    }
*/
    
    public static JSONArray executeDynatraceAPIGetArrayRequest(Context context, DynatraceContext dynatracecontext, String endpoint, MultivaluedMap<String,String> params, boolean tracemode) throws Exception {
		final String dynatraceurl=getDynatraceApiUrl(dynatracecontext.getDynatraceManagedHostname(),dynatracecontext.getDynatraceAccountID()) + endpoint;
		Map<String,String> headers=new HashMap<>();
		headers.put("Authorization","Api-Token "+dynatracecontext.getApiKey());
        headers.put("Accept","application/json; charset=utf-8");
    	
		final Optional<Proxy> proxy = getProxy(context, dynatracecontext.getProxyname(), dynatraceurl);
		final HTTPGenerator http = new HTTPGenerator(HTTP_GET_METHOD, dynatraceurl, headers, params, proxy);
		
		try {
			if (tracemode) {
				context.getLogger().info("Dynatrace GET request:\n" + http.getRequest());
			}
			final HttpResponse httpresponse = http.execute();
			
			if (HttpResponseUtils.isSuccessHttpCode(httpresponse.getStatusLine().getStatusCode())) {
				return getJsonArrayResponse(httpresponse);
			} else {
				final String stringResponse = HttpResponseUtils.getStringResponse(httpresponse);
				throw new DynatraceException(
					httpresponse.getStatusLine().getReasonPhrase() + " - " + dynatraceurl + " - " + stringResponse);
			}
		} finally {
			http.closeHttpClient();
		}
    }

    public static JSONObject executeDynatraceAPIGetObjectRequest(Context context, DynatraceContext dynatracecontext, String endpoint, MultivaluedMap<String,String> params, boolean tracemode) throws Exception {
		final String dynatraceurl = getDynatraceApiUrl(dynatracecontext.getDynatraceManagedHostname(),dynatracecontext.getDynatraceAccountID()) + endpoint;
		Map<String,String> headers=new HashMap<>();
		headers.put("Authorization","Api-Token "+dynatracecontext.getApiKey());
        headers.put("Accept","application/json; charset=utf-8");
   	
		final Optional<Proxy> proxy = getProxy(context, dynatracecontext.getProxyname(), dynatraceurl);
		final HTTPGenerator http = new HTTPGenerator(HTTP_GET_METHOD, dynatraceurl, headers, params, proxy);
		
		try {
			if (tracemode) {
				context.getLogger().info("Dynatrace GET request:\n" + http.getRequest());
			}
			final HttpResponse httpresponse = http.execute();
			
			if (HttpResponseUtils.isSuccessHttpCode(httpresponse.getStatusLine().getStatusCode())) {
				return getJsonResponse(httpresponse);
			} else {
				final String stringResponse = HttpResponseUtils.getStringResponse(httpresponse);
				throw new DynatraceException(
					httpresponse.getStatusLine().getReasonPhrase() + " - " + dynatraceurl + " - " + stringResponse);
			}
		} finally {
			http.closeHttpClient();
		}
    }

    public static JSONObject executeDynatraceAPIPostObjectRequest(Context context, DynatraceContext dynatracecontext, String endpoint, String payload, boolean tracemode) throws Exception {
		final String dynatraceurl = getDynatraceApiUrl(dynatracecontext.getDynatraceManagedHostname(),dynatracecontext.getDynatraceAccountID()) + endpoint;
		MultivaluedMap<String,String> params=new MultivaluedHashMap<>();
		Map<String,String> headers=new HashMap<>();
		headers.put("Authorization","Api-Token "+dynatracecontext.getApiKey());
        headers.put("Accept","application/json; charset=utf-8");
		headers.put("Content-Type", "application/json");
   	
		final Optional<Proxy> proxy = getProxy(context, dynatracecontext.getProxyname(), dynatraceurl);
		final HTTPGenerator http = new HTTPGenerator(HTTP_POST_METHOD, dynatraceurl, headers, params, proxy,payload);
		
		try {
			if (tracemode) {
				context.getLogger().info("Dynatrace POST request:\n" + http.getRequest());
			}
			final HttpResponse httpresponse = http.execute();
			
			if (HttpResponseUtils.isSuccessHttpCode(httpresponse.getStatusLine().getStatusCode())) {
				return getJsonResponse(httpresponse);
			} else {
				final String stringResponse = HttpResponseUtils.getStringResponse(httpresponse);
				throw new DynatraceException(
					httpresponse.getStatusLine().getReasonPhrase() + " - " + dynatraceurl + " - " + stringResponse);
			}
		} finally {
			http.closeHttpClient();
		}
    }

    public static void executeDynatraceAPIDeleteRequest(Context context, DynatraceContext dynatracecontext, String endpoint, boolean tracemode) throws Exception {
		final String dynatraceurl = getDynatraceApiUrl(dynatracecontext.getDynatraceManagedHostname(),dynatracecontext.getDynatraceAccountID()) + endpoint;
		MultivaluedMap<String,String> params=new MultivaluedHashMap<>();
		Map<String,String> headers=new HashMap<>();
		headers.put("Authorization","Api-Token "+dynatracecontext.getApiKey());
   	
		final Optional<Proxy> proxy = getProxy(context, dynatracecontext.getProxyname(), dynatraceurl);
		final HTTPGenerator http = new HTTPGenerator(HTTP_DELETE_METHOD, dynatraceurl, headers, params, proxy);
		
		try {
			if (tracemode) {
				context.getLogger().info("Dynatrace DELETE request:\n" + http.getRequest());
			}
			final HttpResponse httpresponse = http.execute();
			
			if (HttpResponseUtils.isSuccessHttpCode(httpresponse.getStatusLine().getStatusCode())) {
                if(tracemode)
                    context.getLogger().info("Dynatrace object properly deleted");
			} else {
				final String stringResponse = HttpResponseUtils.getStringResponse(httpresponse);
				throw new DynatraceException(
					httpresponse.getStatusLine().getReasonPhrase() + " - " + dynatraceurl + " - " + stringResponse);
			}
		} finally {
			http.closeHttpClient();
		}
    }
    
    public static int executeDynatraceAPIPutRequest(Context context, DynatraceContext dynatracecontext, String endpoint, String payload, boolean tracemode) throws Exception {
		final String dynatraceurl = getDynatraceApiUrl(dynatracecontext.getDynatraceManagedHostname(),dynatracecontext.getDynatraceAccountID()) + endpoint;
		MultivaluedMap<String,String> params=new MultivaluedHashMap<>();
		Map<String,String> headers=new HashMap<>();
		headers.put("Authorization","Api-Token "+dynatracecontext.getApiKey());
		headers.put("Content-Type", "application/json");
   	
		final Optional<Proxy> proxy = getProxy(context, dynatracecontext.getProxyname(), dynatraceurl);
		final HTTPGenerator http = new HTTPGenerator(HTTP_PUT_METHOD, dynatraceurl, headers, params, proxy, payload);
		
		try {
			if (tracemode) {
				context.getLogger().info("Dynatrace PUT request:\n" + http.getRequest());
			}
			final HttpResponse httpresponse = http.execute();
			int statuscode=httpresponse.getStatusLine().getStatusCode();
			if (HttpResponseUtils.isSuccessHttpCode(statuscode)) {
                if(tracemode)
                    context.getLogger().info("Dynatrace data properly put");
                return statuscode;
			} else {
				final String stringResponse = HttpResponseUtils.getStringResponse(httpresponse);
				throw new DynatraceException(
					httpresponse.getStatusLine().getReasonPhrase() + " - " + dynatraceurl + " - " + stringResponse);
			}
		} finally {
			http.closeHttpClient();
		}
    }
    
}
