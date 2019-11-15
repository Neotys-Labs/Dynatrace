package com.neotys.dynatrace.anomalieDetection.create;

import com.neotys.action.argument.ArgumentValidator;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.ActionParameter;

import static com.neotys.action.argument.DefaultArgumentValidator.ALWAYS_VALID;
import static com.neotys.action.argument.DefaultArgumentValidator.NON_EMPTY;
import static com.neotys.action.argument.Option.AppearsByDefault.False;
import static com.neotys.action.argument.Option.AppearsByDefault.True;
import static com.neotys.action.argument.Option.OptionalRequired.Optional;
import static com.neotys.action.argument.Option.OptionalRequired.Required;
import static com.neotys.extensions.action.ActionParameter.Type.TEXT;

enum DynatraceAnomalieDetectionOption implements Option {
    DynatraceId("dynatraceId", Required, True, TEXT,
            "Dynatrace ID",
                    "Dynatrace ID (section of your Dynatrace saas url).",
                NON_EMPTY),

    DynatraceApiKey("dynatraceApiKey", Required, True, TEXT,
            "Dynatrace API key",
                    "Dynatrace API key.",
                    NON_EMPTY),

    NeoLoadProxy("proxyName", Optional, False, TEXT,
            "",
                    "The NeoLoad proxy name to access Dynatrace.",
                 ALWAYS_VALID),

    DynatraceManagedHostname("dynatraceManagedHostname", Optional, False, TEXT,
            "",
                    "Hostname of your managed Dynatrace environment.",
                             ALWAYS_VALID),
    DynatraceTags("tags", Required, True, TEXT,
            "tag1,tag2",
                    "Dynatrace tags. Links the NeoLoad computed data to Dynatrace tags (format: tag1,tag2).",
                  NON_EMPTY),
    DynatraceMetricName("dynatraceMetricName", Optional, True, TEXT,
            "com.dynatrace.builtin:service.responsetime",
            "name of the dynatrace Timeserieid ( com.dynatrace.builtin:service.responsetime for response time, com.dynatrace.builtin:service.failurerate for failure rate,..etc.",
            NON_EMPTY),
    DynatraceOperator("operator", Optional, True, TEXT,
            "ABOVE",
            "Operator used for the rule. Value possible : ABOVE or BELOW",
            NON_EMPTY),
    DynatraceMericValue("value", Optional, True, TEXT,
            "150",
            "Value of the threshold",
            NON_EMPTY),
    DynatraceTypeofAnomalie("typeOfAnomalie", Optional, True, TEXT,
            "PERFORMANCE",
            "Value possible : AVAILABILITY, CUSTOM_ALERT, ERROR, PERFORMANCE, RESOURCE_CONTENTION",
            NON_EMPTY),
    JsonAnomalieDetection("jsonAnomalieDetection", Optional, False, TEXT,
            "",
            "Json string containing the different anomalies rules : example : {[{\n" +
                    "      \"dynatraceMetricName\":\"com.dynatrace.builtin:service.responsetime\",\n" +
                    "      \"operator\":\"ABOVE\",\n" +
                    "      \"value\": \"100\" ,\n" +
                    "      \"typeOfAnomalie\":\"PERFORMANCE\"\n" +
                    "    }]}",
            ALWAYS_VALID),
    JsonAnomalieDetectionFile("jsonAnomalieDetectionFile", Optional, False, TEXT,
            "",
            "path to the json file containing the anomalie detection rules",
            ALWAYS_VALID),
    TraceMode("traceMode", Optional, False, TEXT,
            "",
            "",
            ALWAYS_VALID);
    private final String name;
    private final Option.OptionalRequired optionalRequired;
    private final Option.AppearsByDefault appearsByDefault;
    private final ActionParameter.Type type;
    private final String defaultValue;
    private final String description;
    private final ArgumentValidator argumentValidator;


DynatraceAnomalieDetectionOption(final String name, final Option.OptionalRequired optionalRequired,
    final Option.AppearsByDefault appearsByDefault,
    final ActionParameter.Type type, final String defaultValue, final String description,
    final ArgumentValidator argumentValidator) {
        this.name = name;
        this.optionalRequired = optionalRequired;
        this.appearsByDefault = appearsByDefault;
        this.type = type;
        this.defaultValue = defaultValue;
        this.description = description;
        this.argumentValidator = argumentValidator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Option.OptionalRequired getOptionalRequired() {
        return optionalRequired;
    }

    @Override
    public Option.AppearsByDefault getAppearsByDefault() {
        return appearsByDefault;
    }

    @Override
    public ActionParameter.Type getType() {
        return type;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ArgumentValidator getArgumentValidator() {
        return argumentValidator;
    }

}
