package com.neotys.dynatrace.anomalieDetection.data;

public class DynatraceAnomalie {
    public String dynatraceMetricName;
    public String operator;
    public String typeOfAnomalie;
    public String value;

    public DynatraceAnomalie(String dynatraceMetricName, String operator, String typeOfAnomalie, String value) {
        this.dynatraceMetricName = dynatraceMetricName;
        this.operator = operator;
        this.typeOfAnomalie = typeOfAnomalie;
        this.value = value;
    }

    public String getDynatraceMetricName() {
        return dynatraceMetricName;
    }

    public void setDynatraceMetricName(String dynatraceMetricName) {
        this.dynatraceMetricName = dynatraceMetricName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getTypeOfAnomalie() {
        return typeOfAnomalie;
    }

    public void setTypeOfAnomalie(String typeOfAnomalie) {
        this.typeOfAnomalie = typeOfAnomalie;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
