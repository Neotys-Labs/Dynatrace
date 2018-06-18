package com.neotys.dynatrace.monitoring.timeseries;

public class DynatraceMetric {

	private String unit;
	private double value;
	private long time;
	private String metricName;
	private String timeSeries;
	private String entityId;
	
	public String getUnit() {
		return unit;
	}

	public double getValue() {
		return value;
	}

	public long getTime() {
		return time;
	}

	public String getMetricName() {
		return metricName;
	}

	public String getTimeseries() {
		return timeSeries;
	}


	public DynatraceMetric(String unit, double value, long time, String metricName, String  strTimeSeries, String entity) {
		super();
		this.unit = unit;
		this.value = value;
		this.time = time;
		this.metricName = metricName;
		timeSeries =strTimeSeries;
		entityId =entity;
	}
}
