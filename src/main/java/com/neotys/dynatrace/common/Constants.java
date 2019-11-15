package com.neotys.dynatrace.common;

public final class Constants {

	/*** Dynatrace ***/
	public static final String DYNATRACE = "Dynatrace";
    public static final String PROCESS   = "Process";

	/*** NeoLoad context (Data Exchange API) ***/
	public static final String NEOLOAD_CONTEXT_HARDWARE = DYNATRACE;
	public static final String NEOLOAD_CONTEXT_LOCATION = DYNATRACE;
	public static final String NEOLOAD_CONTEXT_SOFTWARE = DYNATRACE;
	
	/*** NeoLoad Current Virtual user context (Keep object in cache cross iterations) ***/
	public static final String NL_DATA_EXCHANGE_API_CLIENT   = "NLDataExchangeAPIClient";
	public static final String DYNATRACE_LAST_EXECUTION_TIME = "DynatraceLastExecutionTime";
	public static final String DYNATRACE_ANOMALIES           = "DynatraceAnomalyList";

    public static final String TRACE_MODE = "traceMode";
    
    /*** Dynatrace API EndPoints ***/
    /*** 	Environment V1 ***/
    @Deprecated
    public static final String DTAPI_ENV1_PREFIX               = "/api/v1/";
    public static final String DTAPI_ENV1_EP_SERVICE           = "entity/services";
    public static final String DTAPI_ENV1_EP_PROCESS           = "entity/infrastructure/processes";
    public static final String DTAPI_ENV1_EP_PROCESSGROUP      = "entity/infrastructure/process-groups";
    public static final String DTAPI_ENV1_EP_HOST              = "entity/infrastructure/hosts";
    public static final String DTAPI_ENV1_EP_CUSTOMDEVICE      = "entity/infrastructure/custom/";
    public static final String DTAPI_ENV1_EP_TIMESERIES        = "timeseries";
    public static final String DTAPI_ENV1_EP_TIMESERIES_CUSTOM = "timeseries/custom:";
	public static final String DTAPI_ENV1_EP_EVENTS            = "events";
    /*** 	Environment V2 ***/
    @Deprecated
    public static final String DTAPI_ENV2_PREFIX               = "/api/v2/";
    /*** 	Configuration  ***/
    @Deprecated
    public static final String DTAPI_CFG_PREFIX                = "/api/config/v1/";
    public static final String DTAPI_CFG_EP_ANOMALIE_METRIC    = "anomalyDetection/metricEvents";
    public static final String DTAPI_CFG_EP_REQUEST_ATTRIBUTE  = "service/requestAttributes";
    

    /*** Dynatrace API request parameters ***/
	public static final String PARAM_TAG="tag";
	public static final String PARAM_ENTITY="entity";
	
    public static final String DYNATRACE_PROTOCOL = "https://";
    public static final long   DYNATRACE_DEFAULT_DIFF=120000;
    
    
}
