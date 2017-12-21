package com.neotys.dynatrace.monitoring;

import com.neotys.action.argument.ArgumentValidator;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.network.HostnameHolder;

import static com.neotys.action.argument.DefaultArgumentValidator.ALWAYS_VALID;
import static com.neotys.action.argument.DefaultArgumentValidator.NON_EMPTY;
import static com.neotys.action.argument.Option.AppearsByDefault.False;
import static com.neotys.action.argument.Option.AppearsByDefault.True;
import static com.neotys.action.argument.Option.OptionalRequired.Optional;
import static com.neotys.action.argument.Option.OptionalRequired.Required;
import static com.neotys.extensions.action.ActionParameter.Type.TEXT;

/**
 *
 */
public enum DynatraceMonitoringOption implements Option {
    DynatraceId("dynatraceId", Required, True, TEXT,
            "Dynatrace ID ( part of the url of your Dynatace saas)",
            "Dynatrace id",
            NON_EMPTY),

    DynatraceApiKey("dynatraceApiKey", Required, True, TEXT,
            "Dynatrace API key",
            "Dynatrace API key",
            NON_EMPTY),

    DynatraceTags("tags", Optional, True, TEXT,
            "tag1,tag2",
            "Dynatrace tags. Link the sending monitoring data to Dynatrace tags (format: tag1,tag2)",
            ALWAYS_VALID),

    DynatraceManagedHostname("dynatraceManagedHostname", Optional, False, TEXT,
            "",
            "Hostname of your managed Dynatrace environement",
            ALWAYS_VALID),

    NeoLoadDataExchangeApiUrl("dataExchangeApiUrl", Required, True, TEXT,
            "http://localhost:7400/DataExchange/v1/Service.svc/",
            "where the DataExchange server is located. Typically the NeoLoad controller. For example, http://localhost:7400/DataExchange/v1/Service.svc/",
            NON_EMPTY),

    NeoLoadDataExchangeApiKey("dataExchangeApiKey", Optional, True, TEXT,
            "",
            "identification key specified in NeoLoad",
            ALWAYS_VALID),

    NeoLoadProxy("proxyName", Optional, False, TEXT,
            "",
            "The name of the NeoLoad proxy to access to Dynatrace.",
            ALWAYS_VALID);


    private final String name;
    private final Option.OptionalRequired optionalRequired;
    private final Option.AppearsByDefault appearsByDefault;
    private final ActionParameter.Type type;
    private final String defaultValue;
    private final String description;
    private final ArgumentValidator argumentValidator;

    DynatraceMonitoringOption(final String name, final Option.OptionalRequired optionalRequired,
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
