package com.neotys.dynatrace.monitoring.timeseries;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.*;
import com.neotys.dynatrace.common.data.DynatarceServiceData;
import com.neotys.dynatrace.common.data.DynatraceService;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import com.neotys.rest.dataexchange.util.Entries;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.text.html.Option;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.neotys.dynatrace.common.Constants.DYNATRACE_HOST_SUFFIX;
import static com.neotys.dynatrace.common.Constants.DYNATRACE_SERVICE_SUFFIX;
import static com.neotys.dynatrace.common.HTTPGenerator.HTTP_GET_METHOD;

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
    private static final String PROCESS = "Process";
    private static final Map<String, String> TIMESERIES_INFRA_MAP = new HashMap<>();
    private static final Map<String,String> TIMESERIES_PGI_MAP=new HashMap<>();
    private static final Map<String, String> TIMESERIES_SERVICES_MAP = new HashMap<>();
    private  Map<String, String> CUSTOM_TIMESERIES_SERVICES_MAP = new HashMap<>();
    private DynatraceContext dynatraceContext;

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


        //----moinitoring of the entire services ( logic based on process group instance)
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.cpu.usage", "AVG");
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.mem.usage", "AVG");
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.nic.bytesreceived", "AVG");
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.nic.bytessent", "AVG");
        //----------------------------------------------------------------------------------

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
    private final Optional<List<String>>  dynatraceCustomTimeseries;
    private final Optional<String> dynatraceAggregationType;
    private static final Optional<Long> diff=Optional.absent();
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
                                  final Optional<List<String>> dynatraceCustomTimeseries,
                                  final Optional<String> dynatraceAggregateType,
                                  final long startTs,
                                  final boolean traceMode) throws Exception {
        this.context = context;
	    this.dynatraceApiKey = dynatraceApiKey;
	    this.dynatraceId = dynatraceId;
	    this.dynatraceApplication = getDynatracetag(dynatraceTags);
	    this.dataExchangeApiClient = dataExchangeAPIClient;
	    this.proxyName = proxyName;
	    this.dynatraceManagedHostname = dynatraceManagedHostname;
	    this.startTS = startTs;
	    this.traceMode = traceMode;

        this.dynatraceCustomTimeseries=dynatraceCustomTimeseries;
        this.dynatraceAggregationType=dynatraceAggregateType;

	    this.isRunning = true;
	    this.header = new HashMap<>();
	    this.dynatraceApplicationHostIds = new ArrayList<>();

        dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceId, getDynatracetag(dynatraceTags), header);
	    this.dynatraceApplicationServiceIds = DynatraceUtils.getApplicationEntityIds(context,dynatraceContext , proxyName, traceMode);

	    initHostsFromProcessGroup();
        initHosts();
        generateCustomTimeserices();
    }

    private Optional<String> getDynatracetag(Optional<String> tag)
    {
        Optional<String> result;
        if(tag.isPresent()) {
            result = Optional.of(tag.get().replaceAll(":", ":NeoLoad-"));
            if(!result.get().contains(":"))
                result=Optional.of("NeoLoad-"+tag.get());

            if(result.get().startsWith("["))
            {
                String[] tagcontext=result.get().split("]");
                if(tagcontext.length>1)
                {
                    result=Optional.of(tagcontext[1]);
                }
            }

            if(result.get().contains(":"))
            {
                String[] tagkey=result.get().split(":");
                if(tagkey.length>1)
                {
                    result=Optional.of(tagkey[1]);
                }
            }
        }
        else
            result=Optional.absent();

        return result;
    }
    private void generateCustomTimeserices()
    {
        if(dynatraceCustomTimeseries.isPresent())
        {
            if(dynatraceCustomTimeseries.get().size()>0)
                dynatraceCustomTimeseries.get().stream().filter(key->!TIMESERIES_INFRA_MAP.containsKey(key)).filter(key->!TIMESERIES_SERVICES_MAP.containsKey(key)).forEach(timeseries->CUSTOM_TIMESERIES_SERVICES_MAP.put(timeseries,dynatraceAggregationType.get()));
        }

    }
    public void processDynatraceData() throws Exception {
        if (isRunning) {
            List<com.neotys.rest.dataexchange.model.Entry> serviceEntries = processtServices(dynatraceApplicationServiceIds,TIMESERIES_SERVICES_MAP);
            List<com.neotys.rest.dataexchange.model.Entry> infraEntries = processInfrastructures(dynatraceApplicationHostIds,TIMESERIES_INFRA_MAP);
            List<com.neotys.rest.dataexchange.model.Entry> smarcapedate=getSmarscapeData();
            List<com.neotys.rest.dataexchange.model.Entry> entryList = Stream.concat(Stream.concat(serviceEntries.stream(), infraEntries.stream()),smarcapedate.stream())
                    .collect(Collectors.toList());

            if(CUSTOM_TIMESERIES_SERVICES_MAP.size()>0)
            {
                //---add the custom metrics-------
                List<com.neotys.rest.dataexchange.model.Entry> servicecustomEntries = processtServices(dynatraceApplicationServiceIds,CUSTOM_TIMESERIES_SERVICES_MAP);
                List<com.neotys.rest.dataexchange.model.Entry> infracustomEntries = processInfrastructures(dynatraceApplicationHostIds,CUSTOM_TIMESERIES_SERVICES_MAP);

                entryList=Stream.concat(Stream.concat(entryList.stream(),servicecustomEntries.stream()),infracustomEntries.stream()).collect(Collectors.toList());
            }

            if(!entryList.isEmpty()){
                //Send merged Entries
                dataExchangeApiClient.addEntries(entryList);
            }
        }
    }

    public List<com.neotys.rest.dataexchange.model.Entry> getCustomTimeSeries()
    {
        List<DynatarceServiceData> dynatarceServiceDataList=new ArrayList<>();

        dynatarceServiceDataList=dynatraceApplicationServiceIds.stream().map((serviceid) -> {
            try {
                return DynatraceUtils.getListProcessGroupInstanceFromServiceId(context, dynatraceContext, serviceid, proxyName, traceMode);
            }
            catch (Exception e)
            {
                return null;
            }
        } ).filter(Objects::nonNull).map(dynatraceService -> getServiceMonitoringData(dynatraceService)).collect(Collectors.toList());

        dynatarceServiceDataList=dynatarceServiceDataList.stream().filter(dynatarceServiceData -> dynatarceServiceData != null && dynatarceServiceData.getDate()>0).collect(Collectors.toList());

        return dynatarceServiceDataList.stream().map(dynatarceServiceData -> dynatraceServiceDataTOEntry(dynatarceServiceData)).flatMap(list->list.stream()).collect(Collectors.toList());
    }

    public List<com.neotys.rest.dataexchange.model.Entry> getSmarscapeData()
    {
        List<DynatarceServiceData> dynatarceServiceDataList=new ArrayList<>();

        dynatarceServiceDataList=dynatraceApplicationServiceIds.stream().map((serviceid) -> {
            try {
                return DynatraceUtils.getListProcessGroupInstanceFromServiceId(context, dynatraceContext, serviceid, proxyName, traceMode);
            }
            catch (Exception e)
            {
                return null;
            }
        } ).filter(Objects::nonNull).map(dynatraceService -> getServiceMonitoringData(dynatraceService)).collect(Collectors.toList());

        dynatarceServiceDataList=dynatarceServiceDataList.stream().filter(dynatarceServiceData -> dynatarceServiceData != null && dynatarceServiceData.getDate()>0).collect(Collectors.toList());

        return dynatarceServiceDataList.stream().map(dynatarceServiceData -> dynatraceServiceDataTOEntry(dynatarceServiceData)).flatMap(list->list.stream()).collect(Collectors.toList());
    }

    private List<com.neotys.rest.dataexchange.model.Entry> dynatraceServiceDataTOEntry(DynatarceServiceData dynatarceServiceData)
    {
        List<com.neotys.rest.dataexchange.model.Entry> entries = new ArrayList<>();
        List<String> path=new ArrayList<>();
        path.add(DYNATRACE);
        path.add(dynatarceServiceData.getServiceName());
        path.add(PROCESS);

        entries.add(new EntryBuilder(  Stream.concat(path.stream(),Arrays.asList(dynatarceServiceData.NUMBER_PROCESS).stream()).collect(Collectors.toList()),dynatarceServiceData.getDate())
                .unit(dynatarceServiceData.NUMBER_PROCESS)
                .value(dynatarceServiceData.getNumber_ofprocess())
                .build());
        entries.add(new EntryBuilder(  Stream.concat(path.stream(),Arrays.asList(dynatarceServiceData.CPU).stream()).collect(Collectors.toList()),dynatarceServiceData.getDate())
                .unit(dynatarceServiceData.CPU_Unit)
                .value(dynatarceServiceData.getCpu())
                .build());
        entries.add(new EntryBuilder(  Stream.concat(path.stream(),Arrays.asList(dynatarceServiceData.Memory).stream()).collect(Collectors.toList()),dynatarceServiceData.getDate())
                .unit(dynatarceServiceData.Memory_Unit)
                .value(dynatarceServiceData.getMemory())
                .build());
        entries.add(new EntryBuilder(  Stream.concat(path.stream(),Arrays.asList(dynatarceServiceData.NETWORK_RECEIVED).stream()).collect(Collectors.toList()),dynatarceServiceData.getDate())
                .unit(dynatarceServiceData.Network_UNit)
                .value(dynatarceServiceData.getNetworkreceived())
                .build());
        entries.add(new EntryBuilder(  Stream.concat(path.stream(),Arrays.asList(dynatarceServiceData.NETWORK_SENT).stream()).collect(Collectors.toList()),dynatarceServiceData.getDate())
                .unit(dynatarceServiceData.Network_UNit)
                .value(dynatarceServiceData.getNetworksent())
                .build());

        return entries;
    }
    private DynatarceServiceData getServiceMonitoringData(DynatraceService dynatraceService)
    {
        DynatarceServiceData data=new DynatarceServiceData(dynatraceService.getDisplayName(),dynatraceService.getServiceid(),dynatraceService.getNumber_ofprocess());
        try {
            for (Map.Entry<String, String> m : TIMESERIES_PGI_MAP.entrySet()) {
                List<DynatraceMetric> dynatraceMetrics = (List<DynatraceMetric>) DynatraceUtils.getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceService.getProcessPGIlist(), startTS, context, dynatraceContext, proxyName, traceMode, diff,Optional.absent());
                if(dynatraceMetrics.size()>0)
                {
                    double total = dynatraceMetrics.stream().mapToDouble(metric -> metric.getValue()).sum();
                    addSumTodata(data, m.getKey(), total);
                    data.setDate(dynatraceMetrics.stream().findFirst().get().getTime());
                }
            }
            return data;
        }
        catch (Exception e)
        {
            context.getLogger().error("Technical Error retrieving monitoring ",e);
        }
        return null;
    }

    private void addSumTodata(DynatarceServiceData data,String metricname,double sum)
    {

        if(metricname.contains("cpu"))
            data.setCpu(sum);

        if(metricname.contains("mem"))
            data.setMemory(sum);

        if(metricname.contains("bytesreceived"))
            data.setNetworkreceived(sum);

        if(metricname.contains("bytessent"))
            data.setNetworksent(sum);
    }
    private List<com.neotys.rest.dataexchange.model.Entry> processInfrastructures(final List<String> dynatraceApplicationIds,Map<String,String> dynatracemetrics) throws Exception {

        List<com.neotys.rest.dataexchange.model.Entry> entries = new ArrayList<>();
        if (dynatraceApplicationIds != null && !dynatraceApplicationIds.isEmpty()) {
            for (Entry<String, String> m : dynatracemetrics.entrySet()) {
                if (isRunning) {
                    final List<DynatraceMetric> dynatraceMetrics = (List<DynatraceMetric>) DynatraceUtils.getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceApplicationIds.stream().filter(s -> s.startsWith(DYNATRACE_HOST_SUFFIX)).collect(Collectors.toList()),startTS,context,dynatraceContext,proxyName,traceMode, diff, Optional.absent());
                    entries.addAll(toEntries(dynatraceMetrics));
                }
            }
        }
        return entries;
    }

    private List<com.neotys.rest.dataexchange.model.Entry> processtServices(final List<String> dynatraceApplicationIds,Map<String,String> dynatracemetrics) throws Exception {
        List<com.neotys.rest.dataexchange.model.Entry> entries = new ArrayList<>();
        if (dynatraceApplicationIds != null && !dynatraceApplicationIds.isEmpty()) {
            for (Entry<String, String> m : dynatracemetrics.entrySet()) {
                if (isRunning) {
                    final List<DynatraceMetric> dynatraceMetrics = DynatraceUtils.getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceApplicationIds.stream().filter(s -> s.startsWith(DYNATRACE_SERVICE_SUFFIX)).collect(Collectors.toList()),startTS,context,dynatraceContext,proxyName,traceMode,diff,Optional.absent());
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

    private void initHosts() throws Exception {
        final String url = DynatraceUtils.getDynatraceApiUrl(dynatraceManagedHostname, dynatraceId) + DYNATRACE_HOSTS;
        final Map<String, String> parameters = DynatraceUtils.generateGetTagParameter(dynatraceApplication,true);

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
        final Map<String, String> parameters = DynatraceUtils.generateGetTagParameter(dynatraceApplication,true);

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

}


