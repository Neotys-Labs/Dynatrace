package com.neotys.dynatrace.sanityCheck.xmlExport;

import com.neotys.dynatrace.common.data.DynatarceServiceData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@XmlRootElement
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

    public static void main(String[] args) throws JAXBException {
        DynatraceSmartScapedata smartScapedata = new DynatraceSmartScapedata("name", Collections.emptyList());
        JAXBContext context = JAXBContext.newInstance(DynatraceSmartScapedata.class);
        Marshaller mar= context.createMarshaller();
        mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        mar.marshal(smartScapedata, new File("test.txt"));
    }
}
