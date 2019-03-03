package it.unimib.disco.bigtwine.services.analysis.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisErrorCode;

/**
 * A AnalysisStatusHistory.
 */
@Document(collection = "analysis_status_history")
public class AnalysisStatusHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("new_status")
    private AnalysisStatus newStatus;

    @Field("old_status")
    private AnalysisStatus oldStatus;

    @Field("user_id")
    private String userId;

    @Field("error_code")
    private AnalysisErrorCode errorCode;

    @Field("message")
    private String message;

    @NotNull
    @Field("date")
    private Instant date;

    @DBRef
    @Field("analysis")
    @JsonIgnoreProperties("")
    private Analysis analysis;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AnalysisStatus getNewStatus() {
        return newStatus;
    }

    public AnalysisStatusHistory newStatus(AnalysisStatus newStatus) {
        this.newStatus = newStatus;
        return this;
    }

    public void setNewStatus(AnalysisStatus newStatus) {
        this.newStatus = newStatus;
    }

    public AnalysisStatus getOldStatus() {
        return oldStatus;
    }

    public AnalysisStatusHistory oldStatus(AnalysisStatus oldStatus) {
        this.oldStatus = oldStatus;
        return this;
    }

    public void setOldStatus(AnalysisStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getUserId() {
        return userId;
    }

    public AnalysisStatusHistory userId(String userId) {
        this.userId = userId;
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AnalysisErrorCode getErrorCode() {
        return errorCode;
    }

    public AnalysisStatusHistory errorCode(AnalysisErrorCode errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public void setErrorCode(AnalysisErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public AnalysisStatusHistory message(String message) {
        this.message = message;
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getDate() {
        return date;
    }

    public AnalysisStatusHistory date(Instant date) {
        this.date = date;
        return this;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public AnalysisStatusHistory analysis(Analysis analysis) {
        this.analysis = analysis;
        return this;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
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
        AnalysisStatusHistory analysisStatusHistory = (AnalysisStatusHistory) o;
        if (analysisStatusHistory.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), analysisStatusHistory.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "AnalysisStatusHistory{" +
            "id=" + getId() +
            ", newStatus='" + getNewStatus() + "'" +
            ", oldStatus='" + getOldStatus() + "'" +
            ", userId='" + getUserId() + "'" +
            ", errorCode='" + getErrorCode() + "'" +
            ", message='" + getMessage() + "'" +
            ", date='" + getDate() + "'" +
            "}";
    }
}
