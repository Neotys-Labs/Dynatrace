package com.neotys.dynatrace.common;

public final class Constants {

	/*** Dynatrace ***/
	public static final String DYNATRACE = "Dynatrace";

	/*** NeoLoad context (Data Exchange API) ***/
	public static final String NEOLOAD_CONTEXT_HARDWARE = DYNATRACE;
	public static final String NEOLOAD_CONTEXT_LOCATION = DYNATRACE;
	public static final String NEOLOAD_CONTEXT_SOFTWARE = DYNATRACE;
	
	/*** NeoLoad Current Virtual user context (Keep object in cache cross iterations) ***/
	public static final String NL_DATA_EXCHANGE_API_CLIENT = "NLDataExchangeAPIClient";
	public static final String DYNATRACE_LAST_EXECUTION_TIME = "DynatraceLastExecutionTime";

    public static final String TRACE_MODE = "traceMode";


    public static final String DYNATRACE_SERVICE_SUFFIX="SERVICE";
	public static final String DYNATRACE_PROCESSGROUP_SUFFIX="PROCESS_GROUP";
	public static final String DYNATRACE_HOST_SUFFIX="HOST";
}
