package com.neotys.dynatrace.DynatraceMonitoring;

public class DynatraceMetric {

	private String Unit;
	private double Value;
	private long time;
	private String MetricName;
	private String TimeSeries;
	private String EntityId;
	
	public String getUnit() {
		return Unit;
	}

	public void setUnit(String unit) {
		Unit = unit;
	}

	public double getValue() {
		return Value;
	}

	public void setValue(double value) {
		Value = value;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getMetricName() {
		return MetricName;
	}

	public void setMetricName(String metricName) {
		MetricName = metricName;
	}
	public String getTimeseries() {
		return TimeSeries;
	}

	public void setTimeSeries(String metricName) {
		TimeSeries = metricName;
	}

	public String getEntityID() {
		return EntityId;
	}

	public void setEntityId(String metricName) {
		EntityId = metricName;
	}
	public DynatraceMetric(String unit, double value, long time, String metricName, String  strTimeSeries, String Entity) {
		super();
		Unit = unit;
		Value = value;
		this.time = time;
		MetricName = metricName;
		TimeSeries=strTimeSeries;
		EntityId=Entity;
	}
	public DynatraceMetric(String unit, String  strTimeSeries,  String metricName) {
		super();
		Unit = unit;
		MetricName = metricName;
		TimeSeries=strTimeSeries;
	}
	
}
