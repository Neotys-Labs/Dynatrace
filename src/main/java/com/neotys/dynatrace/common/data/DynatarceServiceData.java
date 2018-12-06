package com.neotys.dynatrace.common.data;

import com.google.common.base.Optional;

public class DynatarceServiceData {

    public static final String NUMBER_PROCESS="Number of Process";
    public static final String CPU="total cpu usage";
    public static final String Memory="total memory usage";
    public static final String NETWORK_SENT="total network sent";
    public static final String NETWORK_RECEIVED="total network received";
    public static final String CPU_Unit="%";
    public static final String Memory_Unit="Byte";
    public static final String Network_UNit="BytePerSecond (B/s)";

    private String serviceID;

    private String serviceName;

    private double number_ofprocess;

    private double cpu;

    private double memory;

    private double networksent;

    private double networkreceived;

    public String getServiceID() {
        return serviceID;
    }



    private Optional<Long> date;

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getNumber_ofprocess() {
        return number_ofprocess;
    }

    public void setNumber_ofprocess(double number_ofprocess) {
        this.number_ofprocess = number_ofprocess;
    }

    public DynatarceServiceData(String serviceName, String serivceID, int number_ofprocess) {
        this.serviceName = serviceName;
        this.number_ofprocess = number_ofprocess;
        this.serviceID=serivceID;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public double getMemory() {
        return memory;
    }

    public void setMemory(double memory) {
        this.memory = memory;
    }

    public double getNetworksent() {
        return networksent;
    }

    public void setNetworksent(double networksent) {
        this.networksent = networksent;
    }

    public double getNetworkreceived() {
        return networkreceived;
    }

    public void setNetworkreceived(double networkreceived) {
        this.networkreceived = networkreceived;
    }

    public void setDate(long date) {
        this.date=Optional.of(new Long(date));

    }

    public long getDate() {
        if(date==null)
            return 0;
        else {
            if (date.isPresent())
                return date.get();
            else
                return 0;
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DynatarceServiceData{");
        sb.append("serviceid=").append(serviceID);
        sb.append(", servicename='").append(serviceName).append('\'');
        sb.append(", number_ofprocess='").append(number_ofprocess).append('\'');
        sb.append(", cpu='").append(cpu).append('\'');
        sb.append(", memory='").append(memory).append('\'');
        sb.append(", networksent='").append(networksent).append('\'');
        sb.append(", networkreceived='").append(networkreceived).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
