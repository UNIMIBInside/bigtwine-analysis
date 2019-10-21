package it.unimib.disco.bigtwine.services.analysis.domain;

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisSettingType;

import java.util.List;

public class AnalysisSettingResolved {
    private String name;
    private AnalysisSettingType type;
    private boolean editable;
    private String description;
    private List<AnalysisSettingChoice> choices;
    private Object defaultValue;
    private Object currentValue;

    public AnalysisSettingResolved() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AnalysisSettingResolved name(String name) {
        this.setName(name);
        return this;
    }

    public AnalysisSettingType getType() {
        return type;
    }

    public void setType(AnalysisSettingType type) {
        this.type = type;
    }

    public AnalysisSettingResolved type(AnalysisSettingType type) {
        this.setType(type);
        return this;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public AnalysisSettingResolved editable(boolean editable) {
        this.setEditable(editable);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AnalysisSettingResolved description(String description) {
        this.setDescription(description);
        return this;
    }

    public List<AnalysisSettingChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<AnalysisSettingChoice> choices) {
        this.choices = choices;
    }

    public AnalysisSettingResolved choices(List<AnalysisSettingChoice> choices) {
        this.setChoices(choices);
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public AnalysisSettingResolved defaultValue(Object defaultValue) {
        this.setDefaultValue(defaultValue);
        return this;
    }

    public Object getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Object currentValue) {
        this.currentValue = currentValue;
    }

    public AnalysisSettingResolved currentValue(Object currentValue) {
        this.setCurrentValue(currentValue);
        return this;
    }

    @Override
    public String toString() {
        return "AnalysisSettingResolved{" +
            "name='" + name + '\'' +
            ", type=" + type +
            ", editable=" + editable +
            ", choices=" + choices +
            ", defaultValue=" + defaultValue +
            ", currentValue=" + currentValue +
            '}';
    }
}
