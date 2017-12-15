package com.neotys.dynatrace.monitoring;

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

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}
	public String getTimeseries() {
		return timeSeries;
	}

	public void setTimeSeries(String metricName) {
		timeSeries = metricName;
	}

	public String getEntityID() {
		return entityId;
	}

	public void setEntityId(String metricName) {
		entityId = metricName;
	}
	public DynatraceMetric(String unit, double value, long time, String metricName, String  strTimeSeries, String Entity) {
		super();
		this.unit = unit;
		this.value = value;
		this.time = time;
		this.metricName = metricName;
		timeSeries =strTimeSeries;
		entityId =Entity;
	}
	public DynatraceMetric(String unit, String  strTimeSeries,  String metricName) {
		super();
		this.unit = unit;
		this.metricName = metricName;
		timeSeries =strTimeSeries;
	}
	
}
