package com.neotys.dynatrace.sanityCheck.jsonExport;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.neotys.dynatrace.common.DynatraceContext;
import com.neotys.dynatrace.common.DynatraceException;
import com.neotys.dynatrace.common.DynatraceUtils;
import com.neotys.dynatrace.common.HTTPGenerator;
import com.neotys.dynatrace.common.data.DynatarceServiceData;
import com.neotys.dynatrace.common.data.DynatraceService;
import com.neotys.dynatrace.monitoring.timeseries.DynatraceMetric;
import com.neotys.extensions.action.engine.Context;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DynatracePGIMetrics {
    private static final Map<String,String> PGI_TIMESERIES_MAP=new HashMap<>();
    private final Optional<String> proxyName;
    private final String dynatraceApiKey;
    private final String dynatraceId;
    private static final long DYNATRACE_SMARTSCAPE_DIFF=300000;
    private final Optional<String> dynatraceManagedHostname;
    private final Optional<String> dynatraceApplication;
    private static final Optional<Long> diff=Optional.of(DYNATRACE_SMARTSCAPE_DIFF);
    private HTTPGenerator httpGenerator;
    private Map<String, String> header = null;
    private boolean isRunning = true;
    private final Context context;
    private long startTS;
    private boolean traceMode;
    private List<String> dynatraceApplicationServiceIds;
    private String applicationName;
    private final static double COMPARAISON_RATIO=0.10;

    static
    {
        //----moinitoring of the entire services ( logic based on process group instance)
        PGI_TIMESERIES_MAP.put("com.dynatrace.builtin:pgi.cpu.usage", "AVG");
        PGI_TIMESERIES_MAP.put("com.dynatrace.builtin:pgi.mem.usage", "AVG");
        PGI_TIMESERIES_MAP.put("com.dynatrace.builtin:pgi.nic.bytesreceived", "AVG");
        PGI_TIMESERIES_MAP.put("com.dynatrace.builtin:pgi.nic.bytessent", "AVG");
        //----------------------------------------------------------------------------------
    }
    private DynatraceContext dynatraceContext;

    public DynatracePGIMetrics( String dynatraceApiKey, String dynatraceId, final Optional<String> dynatraceTags, Optional<String> dynatraceManagedHostname, Optional<String> dynatraceApplication,Optional<String> proxyName, Context context,long startTS, boolean traceMode) throws Exception {
        this.proxyName = proxyName;
        this.dynatraceApiKey = dynatraceApiKey;
        this.dynatraceId = dynatraceId;
        this.dynatraceManagedHostname = dynatraceManagedHostname;
        this.dynatraceApplication = dynatraceApplication;
        this.context = context;
        this.traceMode = traceMode;
        this.startTS=startTS;
        dynatraceContext=new DynatraceContext(dynatraceApiKey, dynatraceManagedHostname, dynatraceId, dynatraceTags, header);

        if(dynatraceApplication.isPresent()) {
            this.dynatraceApplicationServiceIds = DynatraceUtils.getListServicesFromApplicationName(context, dynatraceContext, dynatraceApplication.get(), proxyName, traceMode,false);
            applicationName=dynatraceApplication.get();
        }
        else {
            this.dynatraceApplicationServiceIds = DynatraceUtils.getApplicationEntityIds(context, dynatraceContext, proxyName, traceMode);
            if(dynatraceTags.isPresent())
                applicationName=dynatraceTags.get();
            else
                applicationName="DYNATRACE";
        }
    }

    public void marshal(String filename , List<DynatarceServiceData> dynatarceServiceDataList) throws  IOException {
        DynatraceSmartScapedata smartScapedata = new DynatraceSmartScapedata(applicationName,dynatarceServiceDataList);
        Writer writer = new FileWriter(filename);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String strJson = gson.toJson(smartScapedata);
        context.getLogger().info(strJson);
        writer.write(strJson);
        writer.close();

    }

    public void sanityCheck(String jsonfile) throws IOException, DynatraceException {
        List<DynatarceServiceData> serviceDataList=getSmarscapeData();
        File xmlfile=new File(jsonfile);


        if(xmlfile.exists())
        {
            //----compare with baseline------
            if(jsonfile.contains(".json"))
            {
                DynatraceSmartScapedata imported_architecture=unmarshall(jsonfile);
                if(imported_architecture.getServiceDataList().size()>serviceDataList.size()) {
                    context.getLogger().error("There is less services running on the application");
                    throw new DynatraceException("There is less services running on the application");

                }else
                {
                    List<String> listoferrors=new ArrayList<>();
                    imported_architecture.getServiceDataList().stream().forEach(services->{
                        DynatarceServiceData relatedata=getDynatraceServiceData(services,serviceDataList);
                        if(relatedata!=null)
                        {
                            if(services.getNumber_ofprocess()>relatedata.getNumber_ofprocess())
                            {
                                context.getLogger().error("There are less process running on " + services.getServiceName());
                                listoferrors.add("There are less process running on " + services.getServiceName());
                            }
                            else
                            {
                                //------check the ressoruces----------------
                                if(!compareMonitoringData(services.getCpu(),relatedata.getCpu()))
                                {
                                    context.getLogger().error("The process are consuming more CPU"+services.getServiceName());
                                    listoferrors.add("There are less process running on " + services.getServiceName());
                                }
                                else
                                {
                                    if(!compareMonitoringData(services.getMemory(),relatedata.getMemory()))
                                    {
                                        context.getLogger().error("The process are consuming more Memory"+services.getServiceName());
                                        listoferrors.add("There are less process running on " + services.getServiceName());
                                    }

                                }
                                //------------------------------------------
                            }
                        }
                        else
                        {
                            context.getLogger().error("Service is missing "+services.getServiceName());
                        }
                    });
                    if(listoferrors.size()==0)
                    {
                        //----if no error then store the new reference in the xml file
                        marshal(jsonfile,serviceDataList);
                    }
                    else
                    {
                        throw new DynatraceException(listoferrors.stream().limit(1).collect(Collectors.joining(",")));
                    }
                }
            }
        }
        else
        {
            //---export current data to xml----
            marshal(jsonfile,serviceDataList);
        }
    }

    private boolean compareMonitoringData(double oldvalue,double newValue)
    {
        double minvalue=oldvalue-oldvalue*COMPARAISON_RATIO;
        double maxvalue=oldvalue+oldvalue*COMPARAISON_RATIO;

        if(newValue<=maxvalue)
            return true;
        else
            return false;
    }

    private DynatarceServiceData getDynatraceServiceData(DynatarceServiceData data, List<DynatarceServiceData> serviceDataList)
    {
        java.util.Optional<DynatarceServiceData> findservicedata=serviceDataList.stream().filter(dynatarceServiceData ->( dynatarceServiceData.getServiceName().equals(data.getServiceName())||dynatarceServiceData.getServiceID().equals(data.getServiceID()))).findFirst();
        if(findservicedata.isPresent())
            return findservicedata.get();
        else
            return null;
    }
    public DynatraceSmartScapedata unmarshall(String filename) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(filename));
        return (DynatraceSmartScapedata) gson.fromJson(reader, DynatraceSmartScapedata.class);
    }

    public List<DynatarceServiceData> getSmarscapeData()
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
        } ).filter(Objects::nonNull).map(dynatraceService -> getServiceMonitoringData(dynatraceService)).filter(Objects::nonNull).collect(Collectors.toList());

        return dynatarceServiceDataList;
    }

    private DynatarceServiceData getServiceMonitoringData(DynatraceService dynatraceService)
    {
        DynatarceServiceData data=new DynatarceServiceData(dynatraceService.getDisplayName(),dynatraceService.getServiceid(),dynatraceService.getNumber_ofprocess());
        try {
            for (Map.Entry<String, String> m : PGI_TIMESERIES_MAP.entrySet()) {
                List<DynatraceMetric> dynatraceMetrics = (List<DynatraceMetric>) DynatraceUtils.getTimeSeriesMetricData(m.getKey(), m.getValue(), dynatraceService.getProcessPGIlist(), startTS, context, dynatraceContext, proxyName, traceMode, diff,Optional.of(true));
                double total = dynatraceMetrics.stream().mapToDouble(metric->metric.getValue()).sum();
                addSumTodata(data,m.getKey(),total);
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



}
