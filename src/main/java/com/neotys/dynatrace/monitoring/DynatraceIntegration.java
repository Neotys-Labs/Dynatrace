package com.neotys.dynatrace.monitoring;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.dynatrace.common.HTTPGenerator;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import com.neotys.rest.error.NeotysAPIException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;

import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_POST_METHOD;

public class DynatraceIntegration {
	private static final String DYNATRACE_URL = ".live.dynatrace.com/api/v1/";
	private static final String DYNATRACE_APPLICATION = "entity/services";
	private static final String DYNATRACE_API_PROCESS_GROUP = "entity/infrastructure/process-groups";
	private static final String DYNATRACE_HOSTS = "entity/infrastructure/hosts";
	private static final String DYNATRACE_TIMESERIES = "timeseries";
	private static final String DYNATRACE_PROTOCOL = "https://";
	private static final String NEOLOAD_LOCATION = "Dynatrace";

	private static final Map<String, String> TIMESERIES_INFRA_MAP = new HashMap<>();
	private static final Map<String, String> TIMESERIES_SERVICES_MAP = new HashMap<>();;

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
		TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.errorcounthttp4xx", "COUNT");
		TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.errorcounthttp5xx", "COUNT");
		TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.failurerate", "AVG");
		TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.requestspermin", "COUNT");
		TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.responsetime", "AVG");
		TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.serversidefailurerate", "AVG");
	}

	private final Optional<String> proxyName;
	private final DataExchangeAPIClient dataExchangeApiClient;
	private final String dynatraceApiKey;
	private final String dynatraceId;
	private final String dynatraceManagedHostname;
	private final String dynatraceApplication;

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
		this.dynatraceApplication = dynatraceTags.orNull();
		this.dynatraceId = dynatraceId;
		this.dynatraceManagedHostname = dynatraceManagedHostname.orNull();
		this.isRunning = true;
		this.proxyName = proxyName;
		final ContextBuilder contextBuilder = new ContextBuilder().hardware("Dynatrace").location(NEOLOAD_LOCATION).software("OneAgent")
				.script("DynatraceMonitoring" + System.currentTimeMillis());
		this.dataExchangeApiClient = DataExchangeAPIClientFactory.newClient(dataExchangeApiUrl, contextBuilder.build(), dataExchangeApiKey.orNull());
		initHttpClient();
		this.dynatraceApplicationServiceIds = getApplicationId();
		getHostsFromProcessGroup();
		getHosts();
		getDynatraceData();
	}

	private String getTags(final String applicationName) {
		final StringBuilder result = new StringBuilder();
		if (applicationName != null) {
			if (applicationName.contains(",")) {
				final String[] tagsTable = applicationName.split(",");
				for (String tag : tagsTable) {
					result.append(tag).append("AND");
				}
				return result.substring(0, result.length() - 3);
			} else {
				return applicationName;
			}
		}
		return null;
	}

	// TODO why is it not used?
	private void configureHttpsfordynatrace() throws NoSuchAlgorithmException, KeyManagementException {
		httpGenerator.setAllowHostnameSSL();
	}

	private void createAndAddEntry(final String entityName, final String metricName,
								   final String metricValueName, final double value,
								   final String unit, final long valueDate)
			throws GeneralSecurityException, IOException, URISyntaxException, NeotysAPIException {
		final EntryBuilder entryBuilder = new EntryBuilder(Arrays.asList("Dynatrace", entityName, metricName, metricValueName), valueDate);
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

	private List<String> getApplicationId() throws DynatraceException, IOException, URISyntaxException {
		final String tags = getTags(dynatraceApplication);
		final String url = getApiUrl() + DYNATRACE_APPLICATION;
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("tag", tags);
		sendTokenIngetParam(parameters);
		//initHttpClient();
		final Optional<Proxy> proxy = getProxy(proxyName, url);
		httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, header, parameters, proxy);

		try {
			final JSONArray jsonObj = httpGenerator.executeAndGetJsonArrayResponse();
			if (jsonObj != null) {
				dynatraceApplicationServiceIds = new ArrayList<>();
				for (int i = 0; i < jsonObj.length(); i++) {
					final JSONObject jsonApplication = jsonObj.getJSONObject(i);
					if (jsonApplication.has("entityId")) {
						dynatraceApplicationServiceIds.add(jsonApplication.getString("entityId"));
					}
				}
			} else {
				throw new DynatraceException("No Application find in the Dynatrace Account with the name " + tags);
			}
		} finally {
			httpGenerator.closeHttpClient();
		}

		return dynatraceApplicationServiceIds;

	}

	private HashMap<String, String> getEntityDefinition(final JSONObject entity) {
		final HashMap<String, String>result = new HashMap<>();
		for (Object key : entity.keySet()) {
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

	private void getHosts() throws IOException, URISyntaxException {
		final String tags = getTags(dynatraceApplication);
		final String url = getApiUrl() + DYNATRACE_HOSTS;
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("tag", tags);
		sendTokenIngetParam(parameters);

		final Optional<Proxy> proxy = getProxy(proxyName, url);
		httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, header, parameters, proxy);

		try {
			final JSONArray jsonArray = httpGenerator.executeAndGetJsonArrayResponse();
			if (jsonArray != null) {
				for (int i = 0; i < jsonArray.length(); i++) {
					final JSONObject jsonApplication = jsonArray.getJSONObject(i);
					if (jsonApplication.has("entityId")) {
						if (jsonApplication.has("displayName")) {
							dynatraceApplicationHostIds.add(jsonApplication.getString("entityId"));
						}
					}
				}
			}
		} finally {
			httpGenerator.closeHttpClient();
		}
	}

	private void getHostsFromProcessGroup() throws IOException, NoSuchAlgorithmException, URISyntaxException {
		final String tags = getTags(dynatraceApplication);
		final String url = getApiUrl() + DYNATRACE_API_PROCESS_GROUP;
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("tag", tags);
		sendTokenIngetParam(parameters);

		final Optional<Proxy> proxy = getProxy(proxyName, url);
		httpGenerator = new HTTPGenerator(HTTP_GET_METHOD, url, header, parameters, proxy);

		try {
			final JSONArray jsonObj = httpGenerator.executeAndGetJsonArrayResponse();
			if (jsonObj != null) {
				dynatraceApplicationHostIds = new ArrayList<>();
				for (int i = 0; i < jsonObj.length(); i++) {
					final JSONObject jsonApplication = jsonObj.getJSONObject(i);
					if (jsonApplication.has("entityId")) {
						if (jsonApplication.has("fromRelationships")) {
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
			}
		} finally {
			httpGenerator.closeHttpClient();
		}
	}

	private void sendTokenIngetParam(final Map<String, String> param) {
		param.put("Api-Token", dynatraceApiKey);
	}

	private void getDynatraceData() throws Exception {
		if (isRunning) {
			///---Send the service data of this entity-----
			for (Entry<String, String> m : TIMESERIES_SERVICES_MAP.entrySet()) {
				if (isRunning) {
					final List<DynatraceMetric> data = getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceApplicationServiceIds);
					sendDynatraceMetricEntity(data);
				}
			}
			//---------------------------------

			//----send the infrastructure entity---------------
			for (Entry<String, String> m : TIMESERIES_INFRA_MAP.entrySet()) {
				if (isRunning) {
					final List<DynatraceMetric> data = getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceApplicationHostIds);
					sendDynatraceMetricEntity(data);
				}
			}
		}
	}

	private void sendDynatraceMetricEntity(final List<DynatraceMetric> metric)
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

	public void setTestRunning() {
		isRunning = true;
	}

	private long getUtcDate() {
		long timeInMillisSinceEpoch123 = System.currentTimeMillis();
		timeInMillisSinceEpoch123 -= 120000;
		return timeInMillisSinceEpoch123;
	}

	private List<DynatraceMetric> getTimeSeriesMetricData(final String timeSeries,
														  final String aggregate, final List<String> listEntityId)
			throws IOException, NoSuchAlgorithmException, URISyntaxException {
		JSONObject jsonApplication;

		final String url = getApiUrl() + DYNATRACE_TIMESERIES;
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
			jsonApplication = httpGenerator.executeAnGetJsonResponse();
			if (jsonApplication == null || !jsonApplication.has("result")) {
				return Collections.emptyList();
			}
			jsonApplication = jsonApplication.getJSONObject("result");
			if (jsonApplication.has("dataPoints")) {
				if (jsonApplication.has("entities")) {
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

	private String getApiUrl() {
		if (dynatraceManagedHostname != null) {
			return DYNATRACE_PROTOCOL + dynatraceManagedHostname + "/api/v1/";
		} else {
			return DYNATRACE_PROTOCOL + dynatraceId + DYNATRACE_URL;
		}
	}

	private void initHttpClient() {
		header = new HashMap<>();

		//	header.put("Authorization", "Api‐Token "+dynatraceApiKey);
		//header.put("Content-Type", "application/json");
	}
}
