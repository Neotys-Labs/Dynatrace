# Dynatrace OneAgent	

## Overview

This bundle of advanced actions allows you to integrate [NeoLoad](https://www.neotys.com/neoload/overview) and Dynatrace OneAgent to improve the analysis of a load testing test.

This bundle has the following actions:  

* **DynatraceEvents:**
  Links a load testing event to all services used by an Application monitored by Dynatrace.
  Data sent: Neoload Project, Test and Scenario Name.
             NeoLoadWeb Frontend Url.
  
* **DynatraceMonitoring**   
    * **Dynatrace -> NeoLoad**: Retrieves infrastructure and service metrics from Dynatrace and inserts them in NeoLoad External Data so that
      you can correlate NeoLoad and Dynatrace metrics within NeoLoad.
      * Infrastructure metrics: 
      
        ```shell
        host.availability, host.cpu.idle, host.cpu.iowait, host.cpu.steal, host.cpu.system, host.cpu.user, host.disk.availablespace, host.disk.bytesread, host.disk.byteswritten, 
        host.disk.freespacepercentage, host.disk.queuelength, host.disk.readoperations, host.disk.readtime, host.disk.usedspace, host.disk.writeoperations, host.disk.writetime, 
        host.mem.available, host.mem.availablepercentage, host.mem.pagefaults, host.mem.used, host.nic.bytesreceived, host.nic.bytessent, host.nic.packetsreceived, pgi.cpu.usage, 
        pgi.jvm.committedmemory, pgi.jvm.garbagecollectioncount, pgi.jvm.garbagecollectiontime, pgi.jvm.threadcount, pgi.jvm.usedmemory, pgi.mem.usage, pgi.nic.bytesreceived, 
        pgi.nic.bytessent
        ```

      * Service metrics:
          ```shell
          service.clientsidefailurerate, service.errorcounthttp4xx, service.errorcounthttp5xx, service.failurerate, service.requestspermin, 
          service.responsetime, service.serversidefailurerate
          ```

    * **NeoLoad -> Dynatrace**: Sends the global statistics of the test to Dynatrace OneAgent so they can be used as custom metrics 
      in Dynatrace dashboards.
      * Custom metrics:
           ```shell
          Request.duration, Request.Count, Transaction.Average.Duration, User.Load, Count.Average.Failure, DowLoaded.Average.Bytes, Downloaded.Average.Bytes.PerSecond, 
          Iteration.Average.Failure, Iteration.Average.Success, Request.Average.Count, Request.Average.Success, Request.Average.Failure, Request.Sucess.PerSecond, 
          Request.Failure.PerSeconds, Transaction.Average.Failure, Iteration.Average.Success, Transaction.Failure.PerSecond, Iteration.Average.Success, Transaction.Average.Count, 
          Failure.Rate
          ```    
      
     
| Property | Value |
| -----| -------------- |
| Maturity | Stable |
| Author   | Neotys Partner Team |
| License  | [BSD Simplified](https://www.neotys.com/documents/legal/bsd-neotys.txt) |
| NeoLoad  | 6.3+ (Enterprise or Professional Edition w/ Integration & Advanced Usage and NeoLoad Web option required)|
| Requirements | NeoLoad Web |
| Bundled in NeoLoad | No
| Download Binaries | See the [latest release](https://github.com/Neotys-Labs/Dynatrace/releases/latest)

## Installation

1. Download the [latest release](https://github.com/Neotys-Labs/Dynatrace/releases/latest).
1. Read the NeoLoad documentation to see [How to install a custom Advanced Action](https://www.neotys.com/documents/doc/neoload/latest/en/html/#25928.htm).

## Set-up

Once installed, how to use in a given NeoLoad project:

1. Create a “Dynatrace” User Path.
1. Insert "DynatraceEvents" in the ‘End’ block.
1. Define a 30 seconds pacing on the ‘Actions’ block.
1. Insert "DynatraceMonitoring" in the ‘Actions’ block.
1. Create a “Dynatrace” Population that contains 100% of “Dynatrace” User Path.
1. In the **Runtime** section, select your scenario, select the “Dynatrace” population and define a constant load of 1 user.
   Do not use multiple load generators. Good practice should be to keep only the local one.

## Parameters for Dynatrace Events

| Name             | Description |
| -----            | ----- |
| dynatraceId      |  Id of your Dynatrace environment (http://<id>.live.dynatrace.com) |
| dynatraceApiKey  |  API key of your Dynatrace account |
| tags (optional)  |  Dynatrace tags. Links the NeoLoad computed data to Dynatrace tags (format: "tag1,tag2") |
| proxyName (Optional) |  The NeoLoad proxy name to access Dynatrace |
| dynatraceManagedHostname (Optional) | Hostname of your managed Dynatrace environment |

## Parameters for Dynatrace Monitoring

Tip: Get NeoLoad API information in NeoLoad preferences: Project Preferences / REST API.

| Name             | Description |
| -----            | ----- |
| dynatraceId      |  Id of your Dynatrace environment (http://<id>.live.dynatrace.com) |
| dynatraceApiKey  | API key of your dynatrace account |
| tags (optional)  | Dynatrace tags. Links the NeoLoad computed data to Dynatrace tags (format: "tag1,tag2") |
| dynatraceManagedHostname (Optional) | Hostname of your Dynatrace managed environment |
| dataExchangeApiUrl   | Where the DataExchange server is located. Typically the NeoLoad controller |
| dataExchangeApiKey  (Optional)  | API key of the DataExchange API |
| proxyName (Optional) |  The name of the NeoLoad proxy to access to Dynatrace |

## Check User Path

This bundle does not work with Check User Path mode.
A Bad context error should be raised.

## Status Codes

* Dynatrace event
    * NL-DYNATRACE_EVENT_ACTION-01: Could not parse arguments
    * NL-DYNATRACE_EVENT_ACTION-02: Technical Error
    * NL-DYNATRACE_EVENT_ACTION-03: Bad context
* Dynatrace monitoring
    * NL-DYNATRACE_MONITORING_ACTION-01: Could not parse arguments
    * NL-DYNATRACE_MONITORING_ACTION-02: Technical Error
    * NL-DYNATRACE_MONITORING_ACTION-03: Bad context
