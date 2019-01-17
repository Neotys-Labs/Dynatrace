package com.neotys.dynatrace.anomalieDetection.data;

import java.util.List;

public class DynatraceAnomalies {
    List<DynatraceAnomalie> dynatraceAnomalieList;

    public DynatraceAnomalies(List<DynatraceAnomalie> dynatraceAnomalieList) {
        this.dynatraceAnomalieList = dynatraceAnomalieList;
    }

    public List<DynatraceAnomalie> getDynatraceAnomalieList() {
        return dynatraceAnomalieList;
    }

    public void setDynatraceAnomalieList(List<DynatraceAnomalie> dynatraceAnomalieList) {
        this.dynatraceAnomalieList = dynatraceAnomalieList;
    }
}
