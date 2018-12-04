package com.neotys.dynatrace.sanityCheck.xmlExport;

import com.neotys.dynatrace.common.data.DynatarceServiceData;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class DynatraceSmartScapedata {

    private String ApplicationName;

    private List<DynatarceServiceData> serviceDataList;

    public DynatraceSmartScapedata(String applicationName, List<DynatarceServiceData> serviceDataList) {
        ApplicationName = applicationName;
        this.serviceDataList = serviceDataList;
    }

    @XmlElement
    public String getApplicationName() {
        return ApplicationName;
    }

    public void setApplicationName(String applicationName) {
        ApplicationName = applicationName;
    }
    @XmlElement
    public List<DynatarceServiceData> getServiceDataList() {
        return serviceDataList;
    }

    public void setServiceDataList(List<DynatarceServiceData> serviceDataList) {
        this.serviceDataList = serviceDataList;
    }
}
