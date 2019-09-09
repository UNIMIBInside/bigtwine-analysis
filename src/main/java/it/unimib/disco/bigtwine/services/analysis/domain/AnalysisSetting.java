package it.unimib.disco.bigtwine.services.analysis.domain;

import it.unimib.disco.bigtwine.services.analysis.config.Constants;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A AnalysisSetting.
 */
@Document(collection = Constants.ANALYSIS_SETTINGS_DB_COLLECTION)
public class AnalysisSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("default_value")
    private Object defaultValue;

    @Field("user_can_override")
    private Boolean userCanOverride;

    @Field("user_roles")
    private Set<String> userRoles = new HashSet<>();

    @Field("analysis_types")
    private Set<AnalysisType> analysisTypes = new HashSet<>();

    @Field("analysis_input_types")
    private Set<AnalysisInputType> analysisInputTypes = new HashSet<>();

    @Field("priority")
    private Integer priority;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public AnalysisSetting name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public AnalysisSetting defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean isUserCanOverride() {
        return userCanOverride;
    }

    public AnalysisSetting userCanOverride(Boolean userCanOverride) {
        this.userCanOverride = userCanOverride;
        return this;
    }

    public void setUserCanOverride(Boolean userCanOverride) {
        this.userCanOverride = userCanOverride;
    }

    public Set<String> getUserRoles() {
        return userRoles;
    }

    public AnalysisSetting userRoles(Set<String> authorities) {
        this.userRoles = authorities;
        return this;
    }

    public AnalysisSetting addUserRoles(String authority) {
        this.userRoles.add(authority);
        return this;
    }

    public AnalysisSetting removeUserRoles(String authority) {
        this.userRoles.remove(authority);
        return this;
    }

    public void setUserRoles(Set<String> authorities) {
        this.userRoles = authorities;
    }

    public Set<AnalysisType> getAnalysisTypes() {
        return analysisTypes;
    }

    public AnalysisSetting analysisTypes(Set<AnalysisType> analysisTypes) {
        this.analysisTypes = analysisTypes;
        return this;
    }

    public AnalysisSetting addAnalysisTypes(AnalysisType analysisType) {
        this.analysisTypes.add(analysisType);
        return this;
    }

    public AnalysisSetting removeAnalysisTypes(AnalysisType analysisType) {
        this.analysisTypes.remove(analysisType);
        return this;
    }

    public void setAnalysisTypes(Set<AnalysisType> analysisTypes) {
        this.analysisTypes = analysisTypes;
    }

    public Set<AnalysisInputType> getAnalysisInputTypes() {
        return analysisInputTypes;
    }

    public AnalysisSetting analysisInputTypes(Set<AnalysisInputType> analysisInputTypes) {
        this.analysisInputTypes = analysisInputTypes;
        return this;
    }

    public AnalysisSetting addAnalysisInputTypes(AnalysisInputType analysisInputType) {
        this.analysisInputTypes.add(analysisInputType);
        return this;
    }

    public AnalysisSetting removeAnalysisInputTypes(AnalysisInputType analysisInputType) {
        this.analysisInputTypes.remove(analysisInputType);
        return this;
    }

    public void setAnalysisInputTypes(Set<AnalysisInputType> analysisInputTypes) {
        this.analysisInputTypes = analysisInputTypes;
    }

    public Integer getPriority() {
        return priority;
    }

    public AnalysisSetting priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnalysisSetting analysisSetting = (AnalysisSetting) o;
        if (analysisSetting.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), analysisSetting.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "AnalysisSetting{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", defaultValue='" + getDefaultValue() + "'" +
            ", userCanOverride='" + isUserCanOverride() + "'" +
            "}";
    }
}
