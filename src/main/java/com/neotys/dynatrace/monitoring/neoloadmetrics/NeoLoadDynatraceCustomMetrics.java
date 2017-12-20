package com.neotys.dynatrace.monitoring.neoloadmetrics;

import io.swagger.client.model.TestStatistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle time series from nlweb to send to dynatrace
 */
public class NeoLoadDynatraceCustomMetrics {

    public static final String REQUEST_COUNT = "REQUEST_COUNT";
    public static final String REQUEST_DURATION = "REQUEST_DURATION";
    public static final String TRANSACTION_AVG_DURATION = "TRANSACTION_AVG_DURATION";
    public static final String USER_LOAD = "USER_LOAD";
    public static final String COUNT_AVG_FAILURE = "COUNT_AVG_FAILURE";
    public static final String DOWNLOADED_BYTES = "DOWNLOADED_BYTES";
    public static final String DOWNLOADED_BYTES_RATE = "DOWNLOADED_BYTES_RATE";
    public static final String ITERATION_FAILURE = "ITERATION_FAILURE";
    public static final String ITERATION_SUCCESS = "ITERATION_SUCCESS";
    public static final String REQUESTS = "REQUESTS";
    public static final String REQUEST_SUCCESS = "REQUEST_SUCCESS";
    public static final String REQUEST_RATE = "REQUEST_RATE";
    public static final String REQUEST_FAILURE = "REQUEST_FAILURE";
    public static final String REQUEST_FAILURE_RATE = "REQUEST_FAILURE_RATE";
    public static final String TRANSACTION_FAILURE = "TRANSACTION_FAILURE";
    public static final String TRANSACTION_SUCCESS = "TRANSACTION_SUCCESS";
    public static final String TRANSACTION_FAILURE_RATE = "TRANSACTION_FAILURE_RATE";
    public static final String TRANSACTION_SUCCESS_RATE = "TRANSACTION_SUCCESS_RATE";
    public static final String TRANSACTIONS = "TRANSACTIONS";
    public static final String FAILURE_RATE = "FAILURE_RATE";

    private static Map<String, DynatraceCustomMetric> neoLoadDynatraceTimeseries = initTimeseries();


    private static Map<String, DynatraceCustomMetric> initTimeseries() {
        Map<String, DynatraceCustomMetric> timeserieMap = new HashMap<>();

        timeserieMap.put(REQUEST_DURATION, DynatraceCustomMetric.of(REQUEST_DURATION, "Request duration", "Second", Arrays.asList("Request.duration")));
        timeserieMap.put(REQUEST_COUNT, DynatraceCustomMetric.of(REQUEST_COUNT, "Request Count", "Request/Second", Arrays.asList("Request.Count")));
        timeserieMap.put(TRANSACTION_AVG_DURATION, DynatraceCustomMetric.of(TRANSACTION_AVG_DURATION, "AverageTransactionDuration", "Second", Arrays.asList("Transaction.Average.Duration")));
        timeserieMap.put(USER_LOAD, DynatraceCustomMetric.of(TRANSACTION_AVG_DURATION, "User Load", "Count", Arrays.asList("User.Load")));
        timeserieMap.put(COUNT_AVG_FAILURE, DynatraceCustomMetric.of(COUNT_AVG_FAILURE, "Number of Failure", "Count", Arrays.asList("Count.Average.Failure")));
        timeserieMap.put(DOWNLOADED_BYTES, DynatraceCustomMetric.of(DOWNLOADED_BYTES, "Downloaded Bytes", "Bytes", Arrays.asList("DowLoaded.Average.Bytes")));
        timeserieMap.put(DOWNLOADED_BYTES_RATE, DynatraceCustomMetric.of(DOWNLOADED_BYTES_RATE, "Downloaded Bytes Rate", "Bytes/Second", Arrays.asList("Downloaded.Average.Bytes.PerSecond")));
        timeserieMap.put(ITERATION_FAILURE, DynatraceCustomMetric.of(ITERATION_FAILURE, "Iteration in Failure", "Count", Arrays.asList("Iteration.Average.Failure")));
        timeserieMap.put(ITERATION_SUCCESS, DynatraceCustomMetric.of(ITERATION_SUCCESS, "Iteration in Success", "Count", Arrays.asList("Iteration.Average.Success")));
        timeserieMap.put(REQUESTS, DynatraceCustomMetric.of(REQUESTS, "Number of request", "Request/Second", Arrays.asList("Request.Average.Count")));
        timeserieMap.put(REQUEST_SUCCESS, DynatraceCustomMetric.of(REQUEST_SUCCESS, "Request in Success", "Count", Arrays.asList("Request.Average.Success")));
        timeserieMap.put(REQUEST_FAILURE, DynatraceCustomMetric.of(REQUEST_FAILURE, "Request in Failure", "Count", Arrays.asList("Request.Average.Failure")));
        timeserieMap.put(REQUEST_RATE, DynatraceCustomMetric.of(REQUEST_RATE, "Request in Success Per second", "Request/Second", Arrays.asList("Request.Sucess.PerSecond")));
        timeserieMap.put(REQUEST_FAILURE_RATE, DynatraceCustomMetric.of(REQUEST_FAILURE_RATE, "Request in Failure Per Second", "Count", Arrays.asList("Request.Failure.PerSeconds")));
        timeserieMap.put(TRANSACTION_FAILURE, DynatraceCustomMetric.of(TRANSACTION_FAILURE, "Transaction in Failure", "Count", Arrays.asList("Transaction.Average.Failure")));
        timeserieMap.put(TRANSACTION_SUCCESS, DynatraceCustomMetric.of(TRANSACTION_SUCCESS, "Transaction in Success", "Count", Arrays.asList("Iteration.Average.Success")));
        timeserieMap.put(TRANSACTION_FAILURE_RATE, DynatraceCustomMetric.of(TRANSACTION_FAILURE_RATE, "Transaction in Failure Per Second", "Transaction/Second", Arrays.asList("Transaction.Failure.PerSecond")));
        timeserieMap.put(TRANSACTION_SUCCESS_RATE, DynatraceCustomMetric.of(TRANSACTION_SUCCESS_RATE, "Transaction in Success Per Second", "Transaction/Second", Arrays.asList("Iteration.Average.Success")));
        timeserieMap.put(TRANSACTIONS, DynatraceCustomMetric.of(TRANSACTIONS, "Number of Transaction", "Count", Arrays.asList("Transaction.Average.Count")));
        timeserieMap.put(FAILURE_RATE, DynatraceCustomMetric.of(FAILURE_RATE, "Failure Rate", "Percentage", Arrays.asList("Failure.Rate")));

        return timeserieMap;
    }

    public static Map<String, DynatraceCustomMetric> getTimeseriesToSend() {
        return neoLoadDynatraceTimeseries;
    }

    public static void updateTimeseriesToSend(TestStatistics testStatistics) {
        neoLoadDynatraceTimeseries.get(REQUEST_COUNT).setValue(testStatistics.getLastRequestCountPerSecond());
        neoLoadDynatraceTimeseries.get(TRANSACTION_AVG_DURATION).setValue(testStatistics.getLastTransactionDurationAverage() / 1000);
        neoLoadDynatraceTimeseries.get(USER_LOAD).setValue(testStatistics.getLastVirtualUserCount());

        neoLoadDynatraceTimeseries.get(COUNT_AVG_FAILURE).updateToLastValue(testStatistics.getTotalGlobalCountFailure());
        neoLoadDynatraceTimeseries.get(DOWNLOADED_BYTES).updateToLastValue(testStatistics.getTotalGlobalDownloadedBytes());
        neoLoadDynatraceTimeseries.get(DOWNLOADED_BYTES_RATE).updateToLastValue(testStatistics.getTotalGlobalDownloadedBytesPerSecond());
        neoLoadDynatraceTimeseries.get(ITERATION_FAILURE).updateToLastValue(testStatistics.getTotalIterationCountFailure());
        neoLoadDynatraceTimeseries.get(ITERATION_SUCCESS).updateToLastValue(testStatistics.getTotalIterationCountSuccess());
        neoLoadDynatraceTimeseries.get(REQUEST_FAILURE).updateToLastValue(testStatistics.getTotalRequestCountFailure());
        neoLoadDynatraceTimeseries.get(REQUEST_RATE).updateToLastValue(testStatistics.getTotalRequestCountPerSecond());
        neoLoadDynatraceTimeseries.get(REQUEST_SUCCESS).updateToLastValue(testStatistics.getTotalRequestCountSuccess());
        neoLoadDynatraceTimeseries.get(TRANSACTION_FAILURE).updateToLastValue(testStatistics.getTotalTransactionCountFailure());
        neoLoadDynatraceTimeseries.get(TRANSACTION_SUCCESS_RATE).updateToLastValue(testStatistics.getTotalTransactionCountPerSecond());
        neoLoadDynatraceTimeseries.get(TRANSACTION_SUCCESS).updateToLastValue(testStatistics.getTotalTransactionCountSuccess());

        neoLoadDynatraceTimeseries.get(REQUEST_DURATION).setValue(testStatistics.getTotalRequestDurationAverage() / 1000);
    }
}
