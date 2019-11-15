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
    public static final String DTAPI_ENV1_EP_SERVICE           = "/api/v1/entity/services";
    public static final String DTAPI_ENV1_EP_PROCESS           = "/api/v1/entity/infrastructure/processes";
    public static final String DTAPI_ENV1_EP_PROCESSGROUP      = "/api/v1/entity/infrastructure/process-groups";
    public static final String DTAPI_ENV1_EP_HOST              = "/api/v1/entity/infrastructure/hosts";
    public static final String DTAPI_ENV1_EP_CUSTOMDEVICE      = "/api/v1/entity/infrastructure/custom/";
    public static final String DTAPI_ENV1_EP_TIMESERIES        = "/api/v1/timeseries";
    public static final String DTAPI_ENV1_EP_TIMESERIES_CUSTOM = "/api/v1/timeseries/custom:";
	public static final String DTAPI_ENV1_EP_EVENTS            = "/api/v1/events";
    /*** 	Environment V2 ***/
    /*** 	Configuration  ***/
    public static final String DTAPI_CFG_EP_ANOMALIE_METRIC    = "/api/config/v1/anomalyDetection/metricEvents";
    public static final String DTAPI_CFG_EP_REQUEST_ATTRIBUTE  = "/api/config/v1/service/requestAttributes";
    public static final String DTAPI_CFG_EP_REQUEST_NAMING     = "/api/config/v1/service/requestNaming";
    
    /*** Dynatrace API request parameters ***/
	public static final String PARAM_TAG="tag";
	public static final String PARAM_ENTITY="entity";
	
    public static final String DYNATRACE_PROTOCOL = "https://";
    public static final long   DYNATRACE_DEFAULT_DIFF=120000;
    
    
}
