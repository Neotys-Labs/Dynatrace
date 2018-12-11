<p align="center"><img src="/screenshots/dynatrace_logo.png" width="40%" alt="Dynatrace Logo" /></p>

# Dynatrace	Integration for NeoLoad

## Overview

These Advanced Actions allows you to integrate [NeoLoad](https://www.neotys.com/neoload/overview) with [Dynatrace](https://www.dynatrace.com/) in order to correlate data from one tool to another.

This bundle provides inbound and an outbound integration:  
* **DynatraceEvents:**
  Links a load testing event to all services used by an application monitored by Dynatrace.
  Data sent: Neoload Project, Test and Scenario Name.
             NeoLoadWeb Frontend Url.
  
* **DynatraceMonitoring**   
    * **Dynatrace &rarr; NeoLoad**: Retrieves infrastructure and service metrics from Dynatrace and inserts them in NeoLoad External Data so that
      you can correlate NeoLoad and Dynatrace metrics within NeoLoad. Since NeoLoad GUI 6.8 version, these metrics are sent to NeoLoad Web.
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
| Download Binaries | <ul><li>[latest release](https://github.com/Neotys-Labs/Dynatrace/releases/latest) is only compatible with NeoLoad from version 6.7</li><li> Use this [release](https://github.com/Neotys-Labs/Dynatrace/releases/tag/Neotys-Labs%2FDynatrace.git-2.0.10) for previous NeoLoad versions</li></ul>|

## Installation

1. Download the [latest release](https://github.com/Neotys-Labs/Dynatrace/releases/latest) for NeoLoad from version 6.7 or this [release](https://github.com/Neotys-Labs/Dynatrace/releases/tag/Neotys-Labs%2FDynatrace.git-2.0.10) for previous NeoLoad versions.
1. Read the NeoLoad documentation to see [How to install a custom Advanced Action](https://www.neotys.com/documents/doc/neoload/latest/en/html/#25928.htm).

<p align="center"><img src="/screenshots/dynatrace_advanced_action.png" alt="New Relic Advanced Action" /></p>

## NeoLoad Set-up

Once installed, how to use in a given NeoLoad project:

1. Create a “Dynatrace” User Path.
1. Insert "DynatraceEvents" in the ‘End’ block.
1. Insert "DynatraceMonitoring" in the ‘Actions’ block.
   <p align="center"><img src="/screenshots/dynatrace_user_path.png" alt="Dynatrace User Path" /></p>
1. Select the **Actions** container and set a pacing duration of 30 seconds.
   <p align="center"><img src="/screenshots/actions_container_pacing.png" alt="Action's Pacing" /></p>
1. Select the **Actions** container and set the "Reset user session and emulate new browser between each iteration" runtime parameters to "No".
   <p align="center"><img src="/screenshots/actions_container_reset_iteration_no.png" alt="Action's Runtime parameters" /></p>
1. Create a "PopulationDynatrace" Population that contains 100% of "Dynatrace" User Path.
   <p align="center"><img src="/screenshots/dynatrace_population.png" alt="Dynatrace Population" /></p>
1. In the **Runtime** section, select your scenario, select the "PopulationDynatrace" population and define a constant load of 1 user for the full duration of the load test.
   <p align="center"><img src="/screenshots/dynatrace_load_variation_policy.png" alt="Load Variation Policy" /></p>
1. Do not use multiple load generators. Good practice should be to keep only the local one.
1. Verify that NeoLoad Web data transfer is properly configured on the Controller preferences (see **Preferences** / **General settings** / **NeoLoad Web**).
   <p align="center"><img src="/screenshots/nlweb_preferences.png" alt="NeoLoad Web Preferences" /></p>
1. Verify to have a license with "Integration & Advanced Usage".
   <p align="center"><img src="/screenshots/license_integration_and_advanced_usage.png" alt="License with Integration & Advanced Usage" /></p>

## Dynatrace Set-up

On the Dynatrace interface

1. Create (or retrieve) a Dynatrace API key from menu **Settings/Intregration/Dynatrace API**.
   <p align="center"><img src="/screenshots/dynatrace_api_key.png" alt="Dynatrace API key" /></p>
1. Search for the application being tested.
   <p align="center"><img src="/screenshots/dynatrace_application.png" alt="Dynatrace Application" /></p>
1. Apply a tag on the application being tested.
   <p align="center"><img src="/screenshots/dynatrace_tag.png" alt="Dynatrace Tag" /></p>
   
## Parameters for Dynatrace Events

| Name             | Description |
| -----            | ----- |
| dynatraceId      | Identifier of your Dynatrace environment: <ul><li>for live environment, identifier part of URL http://&lt;identifier&gt;.live.dynatrace.com (for example identifier is pk12475 for URL https://pk12475.live.dynatrace.com)</li><li>for managed environment, identifier is a UUID (Universal Unique Identifier), for example 123e4567-e89b-12d3-a456-426655440000</li></ul> |
| dynatraceApiKey  |  API key of your Dynatrace account |
| tags (optional)  |  Dynatrace tags. Links the NeoLoad computed data to Dynatrace tags (format: tag1,tag2) |
| proxyName (Optional) |  The NeoLoad proxy name to access Dynatrace |
| dynatraceManagedHostname (Optional) | <ul><li>for managed environment, it is the hostname of your managed Dynatrace environment (for example preprod.neotys.com, do not include neither protocol nor API path)</li><li>for live environment, this parameter should be removed</li></ul> |


## Parameters for Dynatrace Monitoring

Tip: Get NeoLoad API information in NeoLoad preferences: Project Preferences / REST API.

| Name             | Description |
| -----            | ----- |
| dynatraceId      | Identifier of your Dynatrace environment:<ul><li>for live environment, identifier part of URL http://&lt;identifier&gt;.live.dynatrace.com (for example identifier is pk12475 for URL https://pk12475.live.dynatrace.com)</li><li>for managed environment, identifier is a UUID (Universal Unique Identifier), for example 123e4567-e89b-12d3-a456-426655440000</li></ul> |
| dynatraceApiKey  | API key of your dynatrace account |
| tags (optional)  | Dynatrace tags. Links the NeoLoad computed data to Dynatrace tags (format: tag1,tag2) |
| dynatraceManagedHostname (Optional) | <ul><li>for managed environment, it is the hostname of your managed Dynatrace environment (for example preprod.neotys.com, do not include neither protocol nor API path)</li><li>for live environment, this parameter should be removed</li></ul> |
| dataExchangeApiUrl (Optional)  | Where the DataExchange server is located. Optional, by default it is: http://${NL-ControllerIp}:7400/DataExchange/v1/Service.svc/ |
| dataExchangeApiKey (Optional)  | API key of the DataExchange API |
| proxyName (Optional) |  The name of the NeoLoad proxy to access to Dynatrace |

## Analyze results in NeoLoad

All the metrics retrieved from Dynatrace are available on the NeoLoad Controller (live during the test, and after the test is executed), in the **External Data** tab.

<p align="center"><img src="/screenshots/neoload_external_data_graphs.png" alt="NeoLoad Graphs External Data" /></p>

## Analyze results in Dynatrace

1. Create a custom chart with NeoLoad data.
   <p align="center"><img src="/screenshots/dynatrace_custom_chart.png" alt="Dynatrace custom chart" /></p>
1. Analyze NeoLoad metrics.

    Click on the custom metric:
    <p align="center"><img src="/screenshots/dynatrace_select_metric.png" alt="Dynatrace select custom neoload metric" /></p>
    Analyze all the NeoLoad metrics sent:
    <p align="center"><img src="/screenshots/dynatrace_neoload_data.png" alt="NeoLoad Data" /></p>
1. Consult NeoLoad events on the tested application.
   <p align="center"><img src="/screenshots/dynatrace_consult_event.png" alt="Dynatrace consult NeoLoad Event" /></p>

## Check User Path

This bundle does not work with the Check User Path mode.
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
