package it.unimib.disco.bigtwine.services.analysis.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;

/**
 * A Analysis.
 */
@Document(collection = "analysis")
public class Analysis implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final AnalysisStatus DEFAULT_STATUS = AnalysisStatus.READY;
    public static final AnalysisVisibility DEFAULT_VISIBILITY = AnalysisVisibility.PUBLIC;

    @Id
    private String id;

    @NotNull
    @Field("type")
    private AnalysisType type;

    @NotNull
    @Field("input_type")
    private AnalysisInputType inputType;

    @NotNull
    @Field("status")
    private AnalysisStatus status;

    @NotNull
    @Field("visibility")
    private AnalysisVisibility visibility;

    @NotNull
    @Field("owner")
    private String owner;

    @NotNull
    @Field("create_date")
    private Instant createDate;

    @NotNull
    @Field("update_date")
    private Instant updateDate;

    @Field("input")
    private AnalysisInput input;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AnalysisType getType() {
        return type;
    }

    public Analysis type(AnalysisType type) {
        this.type = type;
        return this;
    }

    public void setType(AnalysisType type) {
        this.type = type;
    }

    public AnalysisInputType getInputType() {
        return inputType;
    }

    public Analysis inputType(AnalysisInputType inputType) {
        this.inputType = inputType;
        return this;
    }

    public void setInputType(AnalysisInputType inputType) {
        this.inputType = inputType;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public Analysis status(AnalysisStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status;
    }

    public AnalysisVisibility getVisibility() {
        return visibility;
    }

    public Analysis visibility(AnalysisVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public void setVisibility(AnalysisVisibility visibility) {
        this.visibility = visibility;
    }

    public String getOwner() {
        return owner;
    }

    public Analysis owner(String ownerId) {
        this.owner = ownerId;
        return this;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Instant getCreateDate() {
        return createDate;
    }

    public Analysis createDate(Instant createDate) {
        this.createDate = createDate;
        return this;
    }

    public void setCreateDate(Instant createDate) {
        this.createDate = createDate;
    }

    public Instant getUpdateDate() {
        return updateDate;
    }

    public Analysis updateDate(Instant updateDate) {
        this.updateDate = updateDate;
        return this;
    }

    public void setUpdateDate(Instant updateDate) {
        this.updateDate = updateDate;
    }

    public AnalysisInput getInput() {
        return input;
    }

    public Analysis input(AnalysisInput input) {
        this.setInput(input);
        return this;
    }

    public void setInput(AnalysisInput input) {
        this.input = input;
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
        Analysis analysis = (Analysis) o;
        if (analysis.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), analysis.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Analysis{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", inputType='" + getInputType() + "'" +
            ", status='" + getStatus() + "'" +
            ", visibility='" + getVisibility() + "'" +
            ", owner='" + getOwner() + "'" +
            ", createDate='" + getCreateDate() + "'" +
            ", updateDate='" + getUpdateDate() + "'" +
            ", input='" + getInput() + "'" +
            "}";
    }
}
