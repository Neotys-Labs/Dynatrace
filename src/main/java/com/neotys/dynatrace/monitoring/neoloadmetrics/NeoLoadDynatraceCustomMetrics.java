package com.neotys.dynatrace.monitoring.neoloadmetrics;

import io.swagger.client.model.TestStatistics;

import java.util.HashMap;
import java.util.Map;

import static com.neotys.dynatrace.monitoring.neoloadmetrics.DynatraceCustomMetric.of;
import static java.util.Arrays.asList;

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
        Map<String, DynatraceCustomMetric> map = new HashMap<>();

        map.put(REQUEST_DURATION, of(REQUEST_DURATION, "Request duration", "Second", asList("Request.duration")));
        map.put(REQUEST_COUNT, of(REQUEST_COUNT, "Request Count", "Request/Second", asList("Request.Count")));
        map.put(TRANSACTION_AVG_DURATION, of(TRANSACTION_AVG_DURATION, "AverageTransactionDuration", "Second", asList("Transaction.Average.Duration")));
        map.put(USER_LOAD, of(TRANSACTION_AVG_DURATION, "User Load", "Count", asList("User.Load")));
        map.put(COUNT_AVG_FAILURE, of(COUNT_AVG_FAILURE, "Number of Failure", "Count", asList("Count.Average.Failure")));
        map.put(DOWNLOADED_BYTES, of(DOWNLOADED_BYTES, "Downloaded Bytes", "Bytes", asList("DowLoaded.Average.Bytes")));
        map.put(DOWNLOADED_BYTES_RATE, of(DOWNLOADED_BYTES_RATE, "Downloaded Bytes Rate", "Bytes/Second", asList("Downloaded.Average.Bytes.PerSecond")));
        map.put(ITERATION_FAILURE, of(ITERATION_FAILURE, "Iteration in Failure", "Count", asList("Iteration.Average.Failure")));
        map.put(ITERATION_SUCCESS, of(ITERATION_SUCCESS, "Iteration in Success", "Count", asList("Iteration.Average.Success")));
        map.put(REQUESTS, of(REQUESTS, "Number of request", "Request/Second", asList("Request.Average.Count")));
        map.put(REQUEST_SUCCESS, of(REQUEST_SUCCESS, "Request in Success", "Count", asList("Request.Average.Success")));
        map.put(REQUEST_FAILURE, of(REQUEST_FAILURE, "Request in Failure", "Count", asList("Request.Average.Failure")));
        map.put(REQUEST_RATE, of(REQUEST_RATE, "Request in Success Per second", "Request/Second", asList("Request.Sucess.PerSecond")));
        map.put(REQUEST_FAILURE_RATE, of(REQUEST_FAILURE_RATE, "Request in Failure Per Second", "Count", asList("Request.Failure.PerSeconds")));
        map.put(TRANSACTION_FAILURE, of(TRANSACTION_FAILURE, "Transaction in Failure", "Count", asList("Transaction.Average.Failure")));
        map.put(TRANSACTION_SUCCESS, of(TRANSACTION_SUCCESS, "Transaction in Success", "Count", asList("Iteration.Average.Success")));
        map.put(TRANSACTION_FAILURE_RATE, of(TRANSACTION_FAILURE_RATE, "Transaction in Failure Per Second", "Transaction/Second", asList("Transaction.Failure.PerSecond")));
        map.put(TRANSACTION_SUCCESS_RATE, of(TRANSACTION_SUCCESS_RATE, "Transaction in Success Per Second", "Transaction/Second", asList("Iteration.Average.Success")));
        map.put(TRANSACTIONS, of(TRANSACTIONS, "Number of Transaction", "Count", asList("Transaction.Average.Count")));
        map.put(FAILURE_RATE, of(FAILURE_RATE, "Failure Rate", "Percentage", asList("Failure.Rate")));

        return map;
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
