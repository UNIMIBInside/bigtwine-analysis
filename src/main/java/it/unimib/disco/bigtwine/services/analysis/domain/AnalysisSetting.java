package it.unimib.disco.bigtwine.services.analysis.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import it.unimib.disco.bigtwine.services.analysis.config.Constants;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.*;

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisSettingType;

/**
 * A AnalysisSetting.
 */
@Document(collection = Constants.ANALYSIS_SETTINGS_DB_COLLECTION)
public class AnalysisSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    /**
     * Setting name, should be alphanumeric and lowercase
     */
    @NotNull
    @Pattern(regexp = "[a-z0-9-]+")
    @ApiModelProperty(value = "Setting name, should be alphanumeric and lowercase", required = true)
    @Field("name")
    private String name;

    @NotNull
    @Field("type")
    private AnalysisSettingType type;

    @Field("description")
    private String description;

    @Field("user_visible")
    private Boolean userVisible;

    /**
     * Each option on separated line with the following format: <value>:<name>
     */
    @ApiModelProperty(value = "Each option on separated line with the following format: <value>:<name>")
    @Transient
    @JsonSerialize
    @JsonDeserialize
    private String options;

    @Field("choices")
    @JsonIgnore
    @AccessType(AccessType.Type.PROPERTY)
    private List<AnalysisSettingChoice> choices = new ArrayList<>();

    @Field("analysis_type")
    private AnalysisType analysisType;

    @Field("analysis_input_types")
    private Set<AnalysisInputType> analysisInputTypes = new HashSet<>();

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

    public String getDescription() {
        return description;
    }

    public AnalysisSetting description(String description) {
        this.description = description;
        return this;
    }

    public AnalysisSetting setDescription(String description) {
        this.description = description;
        return this;
    }

    public AnalysisSettingType getType() {
        return type;
    }

    public AnalysisSetting type(AnalysisSettingType type) {
        this.type = type;
        return this;
    }

    public void setType(AnalysisSettingType type) {
        this.type = type;
    }

    public Boolean isUserVisible() {
        return userVisible;
    }

    public AnalysisSetting userVisible(Boolean userVisible) {
        this.userVisible = userVisible;
        return this;
    }

    public void setUserVisible(Boolean userVisible) {
        this.userVisible = userVisible;
    }

    public String getOptions() {
        return options;
    }

    public AnalysisSetting options(String options) {
        this.setOptions(options);
        return this;
    }

    public void setOptions(String options) {
        this.options = options;
        this.rebuildChoices();
    }

    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public AnalysisSetting analysisType(AnalysisType analysisType) {
        this.analysisType = analysisType;
        return this;
    }

    public void setAnalysisType(AnalysisType analysisType) {
        this.analysisType = analysisType;
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
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public List<AnalysisSettingChoice> getChoices() {
        return this.choices;
    }

    public AnalysisSetting choices(List<AnalysisSettingChoice> choices) {
        this.setChoices(choices);
        return this;
    }

    public void setChoices(List<AnalysisSettingChoice> choices) {
        this.choices = choices;
        this.rebuildOptions();
    }

    private void rebuildChoices() {
        if (StringUtils.isBlank(this.options)) {
            this.choices = Collections.emptyList();
        } else {
            List<AnalysisSettingChoice> choices = new ArrayList<>();
            String[] lines = this.options.split("\\\\r?\\\\n");

            for (String line: lines) {
                String[] parts = line.split(":");

                if (parts.length != 2 || StringUtils.isBlank(parts[0]) || StringUtils.isBlank(parts[1])) {
                    continue;
                }

                choices.add(new AnalysisSettingChoice()
                    .value(parts[0].trim())
                    .name(parts[1].trim()));
            }

            this.choices = choices;
        }
    }

    private void rebuildOptions() {
        if (choices != null && choices.size() > 0) {
            StringBuilder optionsBuilder = new StringBuilder();
            for (AnalysisSettingChoice choice : choices) {
                optionsBuilder.append(String.format("%s:%s\n", choice.getValue(), choice.getName()));
            }

            this.options = optionsBuilder.toString();
        } else {
            this.options = "";
        }
    }

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
            ", type='" + getType() + "'" +
            ", userVisible='" + isUserVisible() + "'" +
            ", options='" + getOptions() + "'" +
            "}";
    }
}
