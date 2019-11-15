package com.neotys.dynatrace.monitoring.timeseries;

import com.google.common.base.Optional;
import com.neotys.dynatrace.common.*;
import com.neotys.dynatrace.common.data.DynatraceServiceData;
import com.neotys.dynatrace.common.topology.DynatraceTopologyCache;
import com.neotys.dynatrace.common.topology.DynatraceTopologyWalker;
import com.neotys.dynatrace.common.data.DynatraceService;
import com.neotys.extensions.action.engine.Context;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.model.EntryBuilder;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.neotys.dynatrace.common.Constants.*;

public class DynatraceGetTimeSeries {
    private static final String AVG = "AVG";
    private static final String COUNT = "COUNT";
    private static final String NONE = null;

    private static final Map<String, String> TIMESERIES_INFRA_MAP    = new HashMap<>();
    private static final Map<String,String>  TIMESERIES_PGI_MAP      = new HashMap<>();
    private static final Map<String, String> TIMESERIES_SERVICES_MAP = new HashMap<>();

    static {
        ///------requesting only infrastructure and services metrics-------------//
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.availability", NONE);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.cpu.idle", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.cpu.iowait", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.cpu.steal", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.cpu.system", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.cpu.user", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.availablespace", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.bytesread", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.byteswritten", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.freespacepercentage", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.queuelength", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.readoperations", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.readtime", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.usedspace", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.writeoperations", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.disk.writetime", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.mem.available", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.mem.availablepercentage", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.mem.pagefaults", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.mem.used", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.nic.bytesreceived", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.nic.bytessent", AVG);
        TIMESERIES_INFRA_MAP.put("com.dynatrace.builtin:host.nic.packetsreceived", AVG);
        
        
        //----monitoring of the entire services ( logic based on process group instance)
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.cpu.usage", AVG);
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.jvm.committedmemory", AVG);
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.jvm.garbagecollectioncount", AVG);
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.jvm.garbagecollectiontime", AVG);
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.jvm.threadcount", AVG);
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.jvm.usedmemory", AVG);
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.mem.usage", AVG);
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.nic.bytesreceived", AVG);
        TIMESERIES_PGI_MAP.put("com.dynatrace.builtin:pgi.nic.bytessent", AVG);


        //----------------------------------------------------------------------------------

        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.clientsidefailurerate", AVG);
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.errorcounthttp4xx", NONE);
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.errorcounthttp5xx", NONE);
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.failurerate", AVG);
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.requestspermin", COUNT);
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.responsetime", AVG);
        TIMESERIES_SERVICES_MAP.put("com.dynatrace.builtin:service.serversidefailurerate", AVG);
    }

    private  Map<String, String> CUSTOM_TIMESERIES_SERVICES_MAP = new HashMap<>();
    private final DataExchangeAPIClient dataExchangeApiClient;
    private final Optional<List<String>>  dynatraceCustomTimeseries;
    private final Optional<String> dynatraceAggregationType;
    private static final Optional<Long> diff=Optional.absent();
    private boolean isRunning = true;
    private final Context context;
    private final DynatraceContext dynatraceContext;
    private long startTS;
    private boolean traceMode;
    private DynatraceTopologyWalker dtw;

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
	    this.dataExchangeApiClient = dataExchangeAPIClient;
	    this.startTS = startTs;
	    this.traceMode = traceMode;

        this.dynatraceCustomTimeseries=dynatraceCustomTimeseries;
        this.dynatraceAggregationType=dynatraceAggregateType;

	    this.isRunning = true;

        this.dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceId, proxyName, /*getDynatracetag(*/dynatraceTags/*)*/, new HashMap<>());
        this.dtw=new DynatraceTopologyWalker(context,dynatraceContext,traceMode);
        dtw.executeDiscovery();
                
        generateCustomTimeseries();
    }

	private void generateCustomTimeseries() {
		if (dynatraceCustomTimeseries.isPresent()) {
			if (dynatraceCustomTimeseries.get().size() > 0)
				dynatraceCustomTimeseries.get().stream().filter(key -> !TIMESERIES_INFRA_MAP.containsKey(key))
						.filter(key -> !TIMESERIES_SERVICES_MAP.containsKey(key))
						.forEach(timeseries -> CUSTOM_TIMESERIES_SERVICES_MAP.put(timeseries,
								dynatraceAggregationType.get()));
		}
	}
    
    public void processDynatraceData() throws Exception {
        if (isRunning) {
            List<com.neotys.rest.dataexchange.model.Entry> serviceEntries = processServices(dtw.getDiscoveredData().getServices(),TIMESERIES_SERVICES_MAP);
            List<com.neotys.rest.dataexchange.model.Entry> infraEntries = processInfrastructures(dtw.getDiscoveredData().getHosts(),TIMESERIES_INFRA_MAP);
            List<com.neotys.rest.dataexchange.model.Entry> smartscapedate=getSmartscapeData();
            List<com.neotys.rest.dataexchange.model.Entry> entryList = Stream.concat(Stream.concat(serviceEntries.stream(), infraEntries.stream()),smartscapedate.stream())
                    .collect(Collectors.toList());

            if(CUSTOM_TIMESERIES_SERVICES_MAP.size()>0) {
                //---add the custom metrics-------
                List<com.neotys.rest.dataexchange.model.Entry> servicecustomEntries = processServices(dtw.getDiscoveredData().getServices(),CUSTOM_TIMESERIES_SERVICES_MAP);
                List<com.neotys.rest.dataexchange.model.Entry> infracustomEntries = processInfrastructures(dtw.getDiscoveredData().getHosts(),CUSTOM_TIMESERIES_SERVICES_MAP);

                entryList=Stream.concat(Stream.concat(entryList.stream(),servicecustomEntries.stream()),infracustomEntries.stream()).collect(Collectors.toList());
            }

            if(!entryList.isEmpty()){
                //Send merged Entries
                dataExchangeApiClient.addEntries(entryList);
            }
        }
    }

    public List<com.neotys.rest.dataexchange.model.Entry> getSmartscapeData() {
        List<DynatraceServiceData> dynatraceServiceDataList=new ArrayList<>();
        DynatraceTopologyCache dtcache=dtw.getDiscoveredData();
        Set<String> dynatraceServiceEntityIds=dtcache.getServices();

        dynatraceServiceDataList=dynatraceServiceEntityIds.stream().map((serviceid) -> {
        	try { 
        		return new DynatraceService(serviceid,dtcache.lookupServiceName(serviceid),dtw.findPgInstancesForService(serviceid));
        	} catch (Exception e) {
        		return null;
        	}
        }).filter(Objects::nonNull).map(dynatraceService -> getServiceMonitoringData(dynatraceService)).collect(Collectors.toList());

        dynatraceServiceDataList=dynatraceServiceDataList.stream().filter(dynatraceServiceData -> dynatraceServiceData != null && dynatraceServiceData.getDate()>0).collect(Collectors.toList());
        return dynatraceServiceDataList.stream().map(dynatraceServiceData -> dynatraceServiceDataTOEntry(dynatraceServiceData)).flatMap(list->list.stream()).collect(Collectors.toList());
    }

    private List<com.neotys.rest.dataexchange.model.Entry> dynatraceServiceDataTOEntry(DynatraceServiceData dynatraceServiceData) {
        List<com.neotys.rest.dataexchange.model.Entry> entries = new ArrayList<>();
        List<String> path=new ArrayList<>();
        path.add(DYNATRACE);
        path.add(dynatraceServiceData.getServiceName());
        path.add(PROCESS);

        entries.add(new EntryBuilder(  Stream.concat(path.stream(),Arrays.asList(DynatraceServiceData.NUMBER_PROCESS).stream()).collect(Collectors.toList()),dynatraceServiceData.getDate())
                .unit(DynatraceServiceData.NUMBER_PROCESS)
                .value(dynatraceServiceData.getNumber_ofprocess())
                .build());
        entries.add(new EntryBuilder(  Stream.concat(path.stream(),Arrays.asList(DynatraceServiceData.CPU).stream()).collect(Collectors.toList()),dynatraceServiceData.getDate())
                .unit(DynatraceServiceData.CPU_UNIT)
                .value(dynatraceServiceData.getCpu())
                .build());
        entries.add(new EntryBuilder(  Stream.concat(path.stream(),Arrays.asList(DynatraceServiceData.MEMORY).stream()).collect(Collectors.toList()),dynatraceServiceData.getDate())
                .unit(DynatraceServiceData.MEMORY_UNIT)
                .value(dynatraceServiceData.getMemory())
                .build());
        entries.add(new EntryBuilder(  Stream.concat(path.stream(),Arrays.asList(DynatraceServiceData.NETWORK_RECEIVED).stream()).collect(Collectors.toList()),dynatraceServiceData.getDate())
                .unit(DynatraceServiceData.NETWORK_UNIT)
                .value(dynatraceServiceData.getNetworkreceived())
                .build());
        entries.add(new EntryBuilder(  Stream.concat(path.stream(),Arrays.asList(DynatraceServiceData.NETWORK_SENT).stream()).collect(Collectors.toList()),dynatraceServiceData.getDate())
                .unit(DynatraceServiceData.NETWORK_UNIT)
                .value(dynatraceServiceData.getNetworksent())
                .build());

        return entries;
    }
    
    private DynatraceServiceData getServiceMonitoringData(DynatraceService dynatraceService) {
        DynatraceServiceData data=new DynatraceServiceData(dynatraceService.getDisplayName(),dynatraceService.getServiceid(),dynatraceService.getNumber_ofprocess());
        try {
            for (Map.Entry<String, String> m : TIMESERIES_PGI_MAP.entrySet()) {
                List<DynatraceMetric> dynatraceMetrics = (List<DynatraceMetric>) DynatraceUtils.getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceService.getProcessPGIlist(), startTS, context, dynatraceContext, traceMode, diff,Optional.absent());
                if(dynatraceMetrics.size()>0) {
                    double total = dynatraceMetrics.stream().mapToDouble(metric -> metric.getValue()).sum();
                    addSumTodata(data, m.getKey(), total);
                    data.setDate(dynatraceMetrics.stream().findFirst().get().getTime());
                }
            }
            return data;
        }
        catch (Exception e) {
            context.getLogger().error("Technical Error retrieving monitoring ",e);
        }
        return null;
    }

    private void addSumTodata(DynatraceServiceData data,String metricname,double sum) {

        if(metricname.contains("cpu"))
            data.setCpu(sum);

        if(metricname.contains("mem"))
            data.setMemory(sum);

        if(metricname.contains("bytesreceived"))
            data.setNetworkreceived(sum);

        if(metricname.contains("bytessent"))
            data.setNetworksent(sum);
    }
    
    private List<com.neotys.rest.dataexchange.model.Entry> processInfrastructures(final Set<String> dynatraceHostEntityIds,Map<String,String> dynatracemetrics) throws Exception {

        List<com.neotys.rest.dataexchange.model.Entry> entries = new ArrayList<>();
        if (dynatraceHostEntityIds != null && !dynatraceHostEntityIds.isEmpty()) {
            for (Entry<String, String> m : dynatracemetrics.entrySet()) {
                if (isRunning) {
                    final List<DynatraceMetric> dynatraceMetrics = (List<DynatraceMetric>) DynatraceUtils.getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceHostEntityIds,startTS,context,dynatraceContext,traceMode, diff, Optional.absent());
                    entries.addAll(toEntries(dynatraceMetrics));
                }
            }
        }
        return entries;
    }

    private List<com.neotys.rest.dataexchange.model.Entry> processServices(final Set<String> dynatraceServiceEntityIds,Map<String,String> dynatracemetrics) throws Exception {
        List<com.neotys.rest.dataexchange.model.Entry> entries = new ArrayList<>();
        if (dynatraceServiceEntityIds != null && !dynatraceServiceEntityIds.isEmpty()) {
            for (Entry<String, String> m : dynatracemetrics.entrySet()) {
                if (isRunning) {
                    final List<DynatraceMetric> dynatraceMetrics = DynatraceUtils.getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceServiceEntityIds,startTS,context,dynatraceContext,traceMode,diff,Optional.absent());
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
    
    public void setTestToStop() {
        isRunning = false;
    }

}


