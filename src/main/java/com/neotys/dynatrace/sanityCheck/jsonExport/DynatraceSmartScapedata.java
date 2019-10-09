package com.neotys.dynatrace.sanityCheck.jsonExport;

import com.neotys.dynatrace.common.data.DynatraceServiceData;


import java.util.List;


public class DynatraceSmartScapedata {

    private String ApplicationName;

    private List<DynatraceServiceData> serviceDataList;

    public DynatraceSmartScapedata(){

    }

    public DynatraceSmartScapedata(String applicationName, List<DynatraceServiceData> serviceDataList) {
        ApplicationName = applicationName;
        this.serviceDataList = serviceDataList;
    }


    public String getApplicationName() {
        return ApplicationName;
    }

    public void setApplicationName(String applicationName) {
        ApplicationName = applicationName;
    }

    public List<DynatraceServiceData> getServiceDataList() {
        return serviceDataList;
    }

    public void setServiceDataList(List<DynatraceServiceData> serviceDataList) {
        this.serviceDataList = serviceDataList;
    }


}
