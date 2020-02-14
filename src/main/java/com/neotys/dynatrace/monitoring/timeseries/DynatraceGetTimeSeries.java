package com.neotys.dynatrace.monitoring.timeseries;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.*;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;

public class DynatraceGetTimeSeries {
    private static final String DYNATRACE_API_PROCESS_GROUP = "entity/infrastructure/process-groups";
    private static final String DYNATRACE_HOSTS = "entity/infrastructure/hosts";
    private static final String DYNATRACE_TIMESERIES = "timeseries";

    private static final String COUNT = "COUNT";
    private static final String NONE = null;

    private static final String ENTITY_ID = "entityId";
    private static final String DISPLAY_NAME = "displayName";
    private static final String TAG = "tag";
    private static final String DYNATRACE = "Dynatrace";

    private static final Map<String, String> TIMESERIES_INFRA_MAP = new HashMap<>();
    private static final Map<String, String> TIMESERIES_SERVICES_MAP = new HashMap<>();


    static {
        ///------requesting only infrastructure and services metrics-------------//
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.availability", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.cpu.idle", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.cpu.iowait", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.cpu.steal", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.cpu.system", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.cpu.user", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.availablespace", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.bytesread", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.byteswritten", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.freespacepercentage", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.queuelength", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.readoperations", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.readtime", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.usedspace", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.writeoperations", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.writetime", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.mem.available", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.mem.availablepercentage", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.mem.pagefaults", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.mem.used", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.nic.bytesreceived", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.nic.bytessent", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.nic.packetsreceived", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:pgi.cpu.usage", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:pgi.jvm.committedmemory", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:pgi.jvm.garbagecollectioncount", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:pgi.jvm.garbagecollectiontime", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:pgi.jvm.threadcount", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:pgi.jvm.usedmemory", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:pgi.mem.usage", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:pgi.nic.bytesreceived", "AVG");
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:pgi.nic.bytessent", "AVG");

        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.clientsidefailurerate", "AVG");
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.errorcounthttp4xx", NONE);
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.errorcounthttp5xx", NONE);
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.failurerate", "AVG");
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.requestspermin", COUNT);
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.responsetime", "AVG");
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.serversidefailurerate", "AVG");
    }

    private final Optional<String> proxyName;
    private final DataExchangeAPIClient dataExchangeApiClient;
    private final String dynatraceApiKey;
    private final String dynatraceId;
    private final Optional<String> dynatraceManagedHostname;
    private final Optional<String> dynatraceApplication;

    private HTTPGenerator httpGenerator;
    private List<String> dynatraceApplicationServiceIds;
    private List<String> dynatraceApplicationHostIds;
    private Map<String, String> header = null;
    private boolean isRunning = true;
    private final Context context;
    private long startTS;
    private boolean traceMode;

    public DynatraceGetTimeSeries(final Context context,
                                  final String dynatraceApiKey,
                                  final String dynatraceId,
                                  final Optional<String> dynatraceTags,
                                  final DataExchangeAPIClient dataExchangeAPIClient,
                                  final Optional<String> proxyName,
                                  final Optional<String> dynatraceManagedHostname,
                                  final long startTs,
                                  final boolean traceMode) throws Exception {
        this.context = context;
	    this.dynatraceApiKey = dynatraceApiKey;
	    this.dynatraceId = dynatraceId;
	    this.dynatraceApplication = dynatraceTags;
	    this.dataExchangeApiClient = dataExchangeAPIClient;
	    this.proxyName = proxyName;
	    this.dynatraceManagedHostname = dynatraceManagedHostname;
	    this.startTS = startTs;
	    this.traceMode = traceMode;


	    this.isRunning = true;
	    this.header = new HashMap<>();
	    this.dynatraceApplicationHostIds = new ArrayList<>();

	    this.dynatraceApplicationServiceIds = DynatraceUtils.getApplicationEntityIds(context, new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceId, dynatraceTags, header), proxyName, traceMode);

	    initHostsFromProcessGroup();
        initHosts();
    }

    public void processDynatraceData() throws Exception {
        if (isRunning) {
            List<com.neotys.rest.dataexchange.model.Entry> serviceEntries = processtServices(dynatraceApplicationServiceIds);
            List<com.neotys.rest.dataexchange.model.Entry> infraEntries = processInfrastructures(dynatraceApplicationHostIds);

            List<com.neotys.rest.dataexchange.model.Entry> entryList = Stream.concat(serviceEntries.stream(), infraEntries.stream())
                    .collect(Collectors.toList());

            if(!entryList.isEmpty()){
                //Send merged Entries
                dataExchangeApiClient.addEntries(entryList);
            }
        }
    }

    private List<com.neotys.rest.dataexchange.model.Entry> processInfrastructures(final List<String> dynatraceApplicationIds) throws Exception {
        List<com.neotys.rest.dataexchange.model.Entry> entries = new ArrayList<>();
        if (dynatraceApplicationIds != null && !dynatraceApplicationIds.isEmpty()) {
            for (Entry<String, String> m : TIMESERIES_INFRA_MAP.entrySet()) {
                if (isRunning) {
                    final List<DynatraceMetric> dynatraceMetrics = getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceApplicationIds);
                    entries.addAll(toEntries(dynatraceMetrics));
                }
            }
        }
        return entries;
    }

    private List<com.neotys.rest.dataexchange.model.Entry> processtServices(final List<String> dynatraceApplicationIds) throws Exception {
        List<com.neotys.rest.dataexchange.model.Entry> entries = new ArrayList<>();
        if (dynatraceApplicationIds != null && !dynatraceApplicationIds.isEmpty()) {
            for (Entry<String, String> m : TIMESERIES_SERVICES_MAP.entrySet()) {
                if (isRunning) {
                    final List<DynatraceMetric> dynatraceMetrics = getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceApplicationIds);
                    entries.addAll(toEntries(dynatraceMetrics));
                }
            }
        }
        return entries;
    }

    private List<com.neotys.rest.dataexchange.model.Entry> toEntries(final List<DynatraceMetric> dynatraceMetrics) {
        return dynatraceMetrics.stream()
                .map(this::toEntry)
                .collect(Collectors.toList());
    }

    private com.neotys.rest.dataexchange.model.Entry toEntry(final DynatraceMetric dynatraceMetric) {
        String timeseries = dynatraceMetric.getTimeseries();
        String[] metricname = timeseries.split(":");
        List<String> path = new ArrayList<>();
        path.add(DYNATRACE);
        path.add(dynatraceMetric.getMetricName());
        path.addAll(Arrays.asList(metricname[1].split("\\.")));

        return new EntryBuilder(path, dynatraceMetric.getTime())
                .unit(dynatraceMetric.getUnit())
                .value(dynatraceMetric.getValue())
                .build();
    }

    private Optional<Proxy> getProxy(final Optional<String> proxyName, final String url) throws MalformedURLException {
        if (proxyName.isPresent()) {
            return Optional.fromNullable(context.getProxyByName(proxyName.get(), new URL(url)));
        }
        return Optional.absent();
    }

    private HashMap<String, String> getEntityDefinition(final JSONObject entity) {
        final HashMap<String, String> result = new HashMap<>();
        final Iterator keysIterator = entity.keys();
        while (keysIterator.hasNext()) {
            final Object key = keysIterator.next();
            result.put((String) key, (String) entity.get((String) key));
        }
        return result;

    }

    private String getEntityDisplayName(final Map<String, String> map, final String entity) {
        final String[] entities = entity.split(",");
        for (Entry<String, String> e : map.entrySet()) {
            for (String entityFromMap : entities) {
                if (entityFromMap.equalsIgnoreCase(e.getKey())) {
                    return e.getValue();
                }
            }
        }
        return null;
    }

    private void initHosts() throws Exception {
        final String url = DynatraceUtils.getDynatraceApiUrl(dynatraceManagedHostname, dynatraceId) + DYNATRACE_HOSTS;
        final Map<String, String> parameters = new HashMap<>();
        if(dynatraceApplication.isPresent()){
            parameters.put(TAG, dynatraceApplication.get());
        }
        sendTokenIngetParam(parameters);

        final Optional<Proxy> proxy = getProxy(proxyName, url);
        httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, header, parameters, proxy);

        try {
            if(traceMode){
                context.getLogger().info("Dynatrace service, get hosts:\n" + httpGenerator.getRequest());
            }
            final JSONArray jsonArray = httpGenerator.executeAndGetJsonArrayResponse();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    final JSONObject jsonApplication = jsonArray.getJSONObject(i);
                    if (jsonApplication.has(ENTITY_ID) && jsonApplication.has(DISPLAY_NAME)) {
                        dynatraceApplicationHostIds.add(jsonApplication.getString(ENTITY_ID));
                    }
                }
            }
            if(dynatraceApplicationHostIds.isEmpty()){
                context.getLogger().info("No host found.");
            }

        }catch (DynatraceException e){
            context.getLogger().error("Failed to init Host", e);
        } finally{
            httpGenerator.closeHttpClient();
        }
    }

    private void initHostsFromProcessGroup() throws Exception {
        final String url = DynatraceUtils.getDynatraceApiUrl(dynatraceManagedHostname, dynatraceId) + DYNATRACE_API_PROCESS_GROUP;
        final Map<String, String> parameters = new HashMap<>();
        if(dynatraceApplication.isPresent()){
            parameters.put(TAG, dynatraceApplication.get());
        }
        sendTokenIngetParam(parameters);

        final Optional<Proxy> proxy = getProxy(proxyName, url);
        httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, header, parameters, proxy);

        try {
            if(traceMode){
                context.getLogger().info("Dynatrace service, get hosts from process group:\n" + httpGenerator.getRequest());
            }
            final JSONArray jsonObj = httpGenerator.executeAndGetJsonArrayResponse();
            if (jsonObj != null) {
                for (int i = 0; i < jsonObj.length(); i++) {
                    final JSONObject jsonApplication = jsonObj.getJSONObject(i);
                    if (jsonApplication.has(ENTITY_ID) && jsonApplication.has("fromRelationships")) {
                        final JSONObject jsonFromRelation = jsonApplication.getJSONObject("fromRelationships");
                        if (jsonFromRelation.has("runsOn")) {
                            final JSONArray jsonRunOn = jsonFromRelation.getJSONArray("runsOn");
                            if (jsonRunOn != null) {
                                for (int j = 0; j < jsonRunOn.length(); j++) {
                                    dynatraceApplicationHostIds.add(jsonRunOn.getString(j));
                                }
                            }
                        }
                    }
                }
            }
            if(dynatraceApplicationHostIds.isEmpty()){
                context.getLogger().debug("No host found in process group");
            }
        }catch (DynatraceException e){
            context.getLogger().error("Failed to init host", e);
        } finally {
            httpGenerator.closeHttpClient();
        }
    }

    private void sendTokenIngetParam(final Map<String, String> param) {
        param.put("Api-Token", dynatraceApiKey);
    }

    public void setTestToStop() {
        isRunning = false;
    }

    private long getUtcDate() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        long timeInMillisSinceEpoch123 = now.toInstant().toEpochMilli();
        timeInMillisSinceEpoch123 -= 120000;
        return timeInMillisSinceEpoch123;
    }

    private List<DynatraceMetric> getTimeSeriesMetricData(final String timeSeries,
                                                          final String aggregate, final List<String> listEntityId)
            throws Exception {
        JSONObject jsonApplication;

        final String url = DynatraceUtils.getDynatraceApiUrl(dynatraceManagedHostname, dynatraceId) + DYNATRACE_TIMESERIES + "/" + timeSeries;
        final Map<String, String> parameters = new HashMap<>();
        sendTokenIngetParam(parameters);

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        final StringBuilder jsonEntitiesBuilder = new StringBuilder().append("{");
        if(aggregate != null){
            jsonEntitiesBuilder.append("\"aggregationType\": \"").append(aggregate.toLowerCase()).append("\",");
        }
        jsonEntitiesBuilder
                .append("\"timeseriesId\" : \"").append(timeSeries).append("\",")
                .append("\"includeData\" : \"true\",")
                .append("\"endTimestamp\":\"").append(String.valueOf(now.toInstant().toEpochMilli())).append("\",")
                .append("\"startTimestamp\":\"").append(String.valueOf(getUtcDate())).append("\",")
                .append("\"entities\":[");

        for (String entit : listEntityId) {
            jsonEntitiesBuilder.append("\"").append(entit).append("\",");
        }

        final String bodyJson = jsonEntitiesBuilder.substring(0, jsonEntitiesBuilder.length() - 1) + "]}";
        final Optional<Proxy> proxy = getProxy(proxyName, url);
        httpGenerator = HTTPGenerator.newJsonHttpGenerator(HTTP_POST_METHOD, url, header, parameters, proxy, bodyJson);

        final List<DynatraceMetric> metrics = new ArrayList<>();
        try {
            if(traceMode){
                context.getLogger().info("Dynatrace service, get timeseries:\n" + httpGenerator.getRequest() + "\n" + bodyJson);
            }
            final HttpResponse httpResponse = httpGenerator.execute();

            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (HttpResponseUtils.isSuccessHttpCode(statusCode)) {
                jsonApplication = HttpResponseUtils.getJsonResponse(httpResponse);
                if (jsonApplication == null || !jsonApplication.has("result")) {
                    context.getLogger().debug("No timeseries found.");
                    return Collections.emptyList();
                }
                jsonApplication = jsonApplication.getJSONObject("result");
                if (jsonApplication.has("dataPoints") && jsonApplication.has("entities")) {
                    final JSONObject jsonEntity = jsonApplication.getJSONObject("entities");
                    final Map<String, String> entities = getEntityDefinition(jsonEntity);

                    final JSONObject jsonDataPoint = jsonApplication.getJSONObject("dataPoints");
                    final Iterator<?> keysIterator = jsonDataPoint.keys();
                    while (keysIterator.hasNext()) {
                        final String entity = (String) keysIterator.next();
                        final String displayName = getEntityDisplayName(entities, entity);
                        final JSONArray arr = jsonDataPoint.getJSONArray(entity);
                        addDataMetrics(metrics, jsonApplication, entity, displayName, arr);
                    }
                }
                if(metrics.isEmpty() && traceMode){
                    context.getLogger().info("No timeseries found.");
                }
            }
            else if(statusCode != HttpStatus.SC_BAD_REQUEST && statusCode != HttpStatus.SC_NOT_FOUND){
                final String stringResponse = HttpResponseUtils.getStringResponse(httpResponse);
                throw new DynatraceException(httpResponse.getStatusLine().getReasonPhrase() + " - "+ url + " - "+ bodyJson + " - " + stringResponse);
            }
        } finally {
            httpGenerator.closeHttpClient();
        }
        return metrics;
    }

    private void addDataMetrics(final List<DynatraceMetric> metrics, final JSONObject jsonApplication,
                                final String entity, final String displayName, final JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONArray data = jsonArray.getJSONArray(i);
            if (data.get(1) instanceof Double) {
                final long time = data.getLong(0);
                DateTime utcTime = new DateTime(time, DateTimeZone.UTC);
                final DateTime localTime = utcTime.withZone(DateTimeZone.getDefault());

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


