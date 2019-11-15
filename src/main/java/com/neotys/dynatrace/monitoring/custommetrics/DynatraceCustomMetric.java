package com.neotys.dynatrace.monitoring.custommetrics;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class DynatraceCustomMetric {

    static DecimalFormat decimalFormat = new DecimalFormat("#.##########");
    private static final String NEOLOAD = "NeoLoad";

    private String displayName;
    private String unit;
    private List<String> dimensions;
    private List<String> types;

    private String value;

    private boolean created;
    private boolean valued;


    public DynatraceCustomMetric(final String displayName, final String unit,
                                 final List<String> dimensions, final List<String> types,
                                 final String value) {
        this.displayName = displayName;
        this.unit = unit;
        this.dimensions = dimensions;
        this.types = types;
        this.value = value;
        this.created = false;
        this.valued = false;
    }

    /**
     * Default dimension: Neoload
     * Default value: null
     * @param displayName
     * @param unit
     * @param dimensions
     * @return
     */
    public static DynatraceCustomMetric of(final String name, final String displayName, final String unit,
                                           final List<String> dimensions){
        return new DynatraceCustomMetric(displayName, unit, dimensions,
                Arrays.asList(NEOLOAD), null);
    }


    public String updateToLastValue(final Number value) {
        Number newValue = 0;
        if (this.value == null) {
            newValue = value;
            setValued(true);
        } else {
            if (value instanceof Long) {
                newValue = (Long) value - Long.valueOf(this.value);
            } else if (value instanceof Float) {
                newValue = (Float) value - Float.valueOf(this.value);
            } else if (value instanceof Double) {
                newValue = (Double) value - Double.valueOf(this.value);
            } else if (value instanceof Integer) {
                newValue = (Integer) value - Integer.valueOf(this.value);
            }
        }

        if (newValue != null) {
            String formatedNewValue = decimalFormat.format(newValue);
            this.value = formatedNewValue;
        }

        return this.value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<String> dimensions) {
        this.dimensions = dimensions;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(Number value) {
        if (value != null) {
            this.value = value + "";
            setValued(true);
        }
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public void setValued(boolean valued) {
        this.valued = valued;
    }

    public boolean isCreated() {
        return created;
    }

    public boolean isValued() {
        return valued;
    }
}
