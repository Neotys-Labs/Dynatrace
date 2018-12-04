package com.neotys.dynatrace.common.data;

import java.util.List;

public class DynatraceService {
    String serviceid;
    String displayName;
    List<String> processPGIlist;
    int number_ofprocess;


    public DynatraceService(String serviceid, String displayName, List<String> processPGIlist) {
        this.serviceid = serviceid;
        this.displayName = displayName;
        this.processPGIlist = processPGIlist;
        number_ofprocess=processPGIlist.size();
    }

    public String getServiceid() {
        return serviceid;
    }

    public void setServiceid(String serviceid) {
        this.serviceid = serviceid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getProcessPGIlist() {
        return processPGIlist;
    }

    public void setProcessPGIlist(List<String> processPGIlist) {
        this.processPGIlist = processPGIlist;
    }

    public int getNumber_ofprocess() {
        return number_ofprocess;
    }

    public void setNumber_ofprocess(int number_ofprocess) {
        this.number_ofprocess = number_ofprocess;
    }
}
