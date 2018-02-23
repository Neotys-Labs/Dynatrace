package com.neotys.dynatrace.monitoring;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.*;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import com.neotys.rest.error.NeotysAPIException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.Map.Entry;

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;

public class DynatraceIntegration {
    private static final String DYNATRACE_API_PROCESS_GROUP = "entity/infrastructure/process-groups";
    private static final String DYNATRACE_HOSTS = "entity/infrastructure/hosts";
    private static final String DYNATRACE_TIMESERIES = "timeseries";

    private static final String COUNT = "COUNT";

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
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.errorcounthttp4xx", COUNT);
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.errorcounthttp5xx", COUNT);
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

    public DynatraceIntegration(final Context context,
                                final String dynatraceApiKey,
                                final String dynatraceId,
                                final Optional<String> dynatraceTags,
                                final String dataExchangeApiUrl,
                                final Optional<String> dataExchangeApiKey,
                                final Optional<String> proxyName,
                                final Optional<String> dynatraceManagedHostname,
                                final long startTs) throws Exception {
        this.context = context;
        this.startTS = startTs;
        this.dynatraceApiKey = dynatraceApiKey;
        this.dynatraceApplication = dynatraceTags;
        this.dynatraceId = dynatraceId;
        this.dynatraceManagedHostname = dynatraceManagedHostname;
        this.isRunning = true;
        this.proxyName = proxyName;
        final ContextBuilder contextBuilder = new ContextBuilder().hardware(DYNATRACE).location(DYNATRACE).software("OneAgent")
                .script("DynatraceMonitoring" + System.currentTimeMillis());
        this.dataExchangeApiClient = DataExchangeAPIClientFactory.newClient(dataExchangeApiUrl, contextBuilder.build(), dataExchangeApiKey.orNull());
        initHttpClient();
        this.dynatraceApplicationServiceIds = DynatraceUtils.getApplicationEntityIds(context, new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceId, dynatraceTags, header), proxyName);
        this.dynatraceApplicationHostIds = new ArrayList<>();
        getHostsFromProcessGroup();
        getHosts();
        initDynatraceData();
    }

    private void createAndAddEntry(final String entityName, final String metricName,
                                   final String metricValueName, final double value,
                                   final String unit, final long valueDate)
            throws GeneralSecurityException, IOException, URISyntaxException, NeotysAPIException {
        final EntryBuilder entryBuilder = new EntryBuilder(Arrays.asList(DYNATRACE, entityName, metricName, metricValueName), valueDate);
        entryBuilder.unit(unit);
        entryBuilder.value(value);
        dataExchangeApiClient.addEntry(entryBuilder.build());
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

    private void getHosts() throws Exception {
        final String tags = DynatraceUtils.getTags(dynatraceApplication);
        final String url = DynatraceUtils.getDynatraceApiUrl(dynatraceManagedHostname, dynatraceId) + DYNATRACE_HOSTS;
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(TAG, tags);
        sendTokenIngetParam(parameters);

        final Optional<Proxy> proxy = getProxy(proxyName, url);
        httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, header, parameters, proxy);

        try {
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
                context.getLogger().debug("No host found.");
            }
        } finally {
            httpGenerator.closeHttpClient();
        }
    }

    private void getHostsFromProcessGroup() throws Exception {
        final String tags = DynatraceUtils.getTags(dynatraceApplication);
        final String url = DynatraceUtils.getDynatraceApiUrl(dynatraceManagedHostname, dynatraceId) + DYNATRACE_API_PROCESS_GROUP;
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(TAG, tags);
        sendTokenIngetParam(parameters);

        final Optional<Proxy> proxy = getProxy(proxyName, url);
        httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, header, parameters, proxy);

        try {
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
        } finally {
            httpGenerator.closeHttpClient();
        }
    }

    private void sendTokenIngetParam(final Map<String, String> param) {
        param.put("Api-Token", dynatraceApiKey);
    }

	private void initDynatraceData() throws Exception {
		if (isRunning) {
			///---Send the service data of this entity-----
            initDynatraceServiceData(dynatraceApplicationServiceIds);

			//----send the infrastructure entity---------------
            initDynatraceInfraData(dynatraceApplicationHostIds);
		}
	}

    private void initDynatraceServiceData(final List<String> dynatraceApplicationIds) throws Exception {
        if (dynatraceApplicationIds != null && !dynatraceApplicationIds.isEmpty()) {
            for (Entry<String, String> m : TIMESERIES_SERVICES_MAP.entrySet()) {
                if (isRunning) {
                    final List<DynatraceMetric> data = getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceApplicationIds);
                    initDynatraceMetricEntity(data);
                }
            }
        }
    }

    private void initDynatraceInfraData(final List<String> dynatraceApplicationIds) throws Exception {
        if (dynatraceApplicationIds != null && !dynatraceApplicationIds.isEmpty()) {
            for (Entry<String, String> m : TIMESERIES_INFRA_MAP.entrySet()) {
                if (isRunning) {
                    final List<DynatraceMetric> data = getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceApplicationIds);
                    initDynatraceMetricEntity(data);
                }
            }
        }
    }

    private void initDynatraceMetricEntity(final List<DynatraceMetric> metric)
            throws GeneralSecurityException, IOException, URISyntaxException, NeotysAPIException {
        for (DynatraceMetric data : metric) {
            String timeseries = data.getTimeseries();
            String[] metricname = timeseries.split(":");
            createAndAddEntry(data.getMetricName(), metricname[0], metricname[1], data.getValue(), data.getUnit(), data.getTime());
        }
    }

    public void setTestToStop() {
        isRunning = false;
    }

    private long getUtcDate() {
        long timeInMillisSinceEpoch123 = System.currentTimeMillis();
        timeInMillisSinceEpoch123 -= 120000;
        return timeInMillisSinceEpoch123;
    }

    private List<DynatraceMetric> getTimeSeriesMetricData(final String timeSeries,
                                                          final String aggregate, final List<String> listEntityId)
            throws Exception {
        JSONObject jsonApplication;

        final String url = DynatraceUtils.getDynatraceApiUrl(dynatraceManagedHostname, dynatraceId) + DYNATRACE_TIMESERIES;
        final Map<String, String> parameters = new HashMap<>();
        sendTokenIngetParam(parameters);

        final StringBuilder jsonEntitiesBuilder = new StringBuilder().append("{")
                .append("\"aggregationType\": \"").append(aggregate.toLowerCase()).append("\",")
                .append("\"timeseriesId\" : \"").append(timeSeries).append("\",")
                .append("\"endTimestamp\":\"").append(String.valueOf(System.currentTimeMillis())).append("\",")
                .append("\"startTimestamp\":\"").append(String.valueOf(getUtcDate())).append("\",")
                .append("\"entities\":[");

        for (String entit : listEntityId) {
            jsonEntitiesBuilder.append("\"").append(entit).append("\",");
        }

        final String json = jsonEntitiesBuilder.substring(0, jsonEntitiesBuilder.length() - 1) + "]}";
        final Optional<Proxy> proxy = getProxy(proxyName, url);
        httpGenerator = HTTPGenerator.newJsonHttpGenerator(HTTP_POST_METHOD, url, header, parameters, proxy, json);

        final List<DynatraceMetric> metrics = new ArrayList<>();
        try {
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
                if(metrics.isEmpty()){
                    context.getLogger().debug("No timeseries found.");
                }
            }
            else if(statusCode != HttpStatus.SC_BAD_REQUEST && statusCode != HttpStatus.SC_NOT_FOUND){
                throw new DynatraceException(httpResponse.getStatusLine().getReasonPhrase());
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
                if (time >= startTS) {
                    final String unit = jsonApplication.getString("unit");
                    final double value = data.getDouble(1);
                    final String timeseriesId = jsonApplication.getString("timeseriesId");
                    final DynatraceMetric metric = new DynatraceMetric(unit, value, time, displayName, timeseriesId, entity);
                    metrics.add(metric);
                }
            }

        }
    }

    private void initHttpClient() {
        header = new HashMap<>();
    }
}
