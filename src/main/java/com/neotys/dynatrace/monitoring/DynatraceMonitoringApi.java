package com.neotys.dynatrace.monitoring;

import com.neotys.dynatrace.monitoring.neoloadmetrics.DynatraceCustomMetric;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * https://www.dynatrace.com/support/help/dynatrace-api/custom-devices-and-metrics/what-does-the-custom-network-devices-and-metrics-api-provide/
 */
public interface DynatraceMonitoringApi {

    void registerCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws IOException, URISyntaxException;

    /**
     * displayName: The display name of the component that will be used within the UI.
     * ipAddresses: List of IP addresses that belong to the component.
     *              These addresses are used to automatically discover the horizontal communication relationship between this component and all other observed components within Smartscape.
     *              Once a connection is discovered, it is automatically mapped and shown within Smartscape.
     * listenPorts: More detailed information about how this component accepts communication from other components on the network-port level.
     *              This information is used to automatically connect process-to-process communication relationships with the custom component.
     * type: A defined software technology type for this custom component.
     *       Please make sure that you only report metrics where the technology type matches the component's technology type.
     * favIcon: A URL where we can fetch a grayscale or monochrome icon that will be used for your custom component.
     *          The icon will automatically invert the color when displayed in Smartscape.
     * configURL: A URL used to directly link to the configuration web page of the entity, such as a login page for a firewall or router.
     * tags: List of custom user labels that you want attached to your custom component.
     * properties: A list of key value pair properties that will be shown beneath the infographics of your custom component.
     * series: List of metric values that are reported for the custom component. A series object always contains the metric identifier, a timestamp in UTC milliseconds (reported as a number, for example: 1495520570871), and the metrics dimension information. The key of the metric dimension (for example, nic) must be defined earlier in the metric definition. The metrics are then sent for nic : ethernetcard1 and nic : ethernetcard2. One important constraint here is the fact that values can't be reported further than 2 hours into the past! A metric must be registered before you can report a metric value. Therefore, the timestamp for reporting a value must be after the registration time of the metric.
     * @param dynatraceCustomMetrics
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws DynatraceStatException
     */
    void reportCustomMetrics(final List<DynatraceCustomMetric> dynatraceCustomMetrics) throws IOException, URISyntaxException, DynatraceStatException;

    boolean hasCustomMetric(final DynatraceCustomMetric dynatraceCustomMetric) throws IOException, URISyntaxException;
}
