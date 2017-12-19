package com.neotys.dynatrace.monitoring;

import com.neotys.dynatrace.monitoring.neoloadmetrics.DynatraceCustomMetric;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * https://www.dynatrace.com/support/help/dynatrace-api/custom-devices-and-metrics/what-does-the-custom-network-devices-and-metrics-api-provide/
 */
public interface DynatraceMonitoringApi {

    int registerCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws IOException, URISyntaxException;

    int reportCustomMetrics(final List<DynatraceCustomMetric> dynatraceCustomMetrics) throws IOException, URISyntaxException, DynatraceStatException;

    int hasCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws IOException, URISyntaxException;
}
