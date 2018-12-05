package com.neotys.dynatrace.sanityCheck.jsonExport;

import com.neotys.dynatrace.common.data.DynatarceServiceData;


import java.util.List;


public class DynatraceSmartScapedata {

    private String ApplicationName;

    private List<DynatarceServiceData> serviceDataList;

    public DynatraceSmartScapedata(){

    }

    public DynatraceSmartScapedata(String applicationName, List<DynatarceServiceData> serviceDataList) {
        ApplicationName = applicationName;
        this.serviceDataList = serviceDataList;
    }


    public String getApplicationName() {
        return ApplicationName;
    }

    public void setApplicationName(String applicationName) {
        ApplicationName = applicationName;
    }

    public List<DynatarceServiceData> getServiceDataList() {
        return serviceDataList;
    }

    public void setServiceDataList(List<DynatarceServiceData> serviceDataList) {
        this.serviceDataList = serviceDataList;
    }


}
