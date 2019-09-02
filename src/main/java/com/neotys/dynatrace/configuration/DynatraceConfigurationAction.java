package com.neotys.dynatrace.configuration;

import com.google.common.base.Optional;
import com.neotys.action.argument.Arguments;
import com.neotys.action.argument.Option;
import com.neotys.dynatrace.common.DynatraceUtils;

import com.neotys.extensions.action.Action;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DynatraceConfigurationAction implements Action {
    private static final String BUNDLE_NAME = "com.neotys.dynatrace.configuration.bundle";
    private static final String DISPLAY_NAME = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayName");
    private static final String DISPLAY_PATH = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayPath");

    @java.lang.Override
    public java.lang.String getType() {
        return "DynatraceConfiguration";
    }

    @java.lang.Override
    public List<ActionParameter> getDefaultActionParameters() {
        final ArrayList<ActionParameter> parameters = new ArrayList<>();

        for (final DynatraceConfigurationOption option : DynatraceConfigurationOption.values()) {
            if (Option.AppearsByDefault.True.equals(option.getAppearsByDefault())) {
                parameters.add(new ActionParameter(option.getName(), option.getDefaultValue(),
                        option.getType()));
            }
        }

        return parameters;
    }

    @java.lang.Override
    public java.lang.Class<? extends ActionEngine> getEngineClass() {
        return DynatraceConfigurationActionEngine.class;
    }

    @java.lang.Override
    public boolean getDefaultIsHit() {
        return false;
    }

    @java.lang.Override
    public javax.swing.Icon getIcon() {
        return  DynatraceUtils.getDynatraceIcon();
    }

    @java.lang.Override
    public java.lang.String getDescription() {
        return "create automatically the request attributest requried for the integration.\n\n" + Arguments.getArgumentDescriptions(DynatraceConfigurationOption.values());
    }

    @java.lang.Override
    public java.lang.String getDisplayName() {
        return DISPLAY_NAME;
    }

    @java.lang.Override
    public java.lang.String getDisplayPath() {
        return DISPLAY_PATH;
    }

    @java.lang.Override
    public Optional<java.lang.String> getMinimumNeoLoadVersion() {
        return Optional.of("6.3");
    }

    @java.lang.Override
    public Optional<java.lang.String> getMaximumNeoLoadVersion() {
        return Optional.of("6.9");
    }
}
