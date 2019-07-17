package it.unimib.disco.bigtwine.services.analysis.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;

import java.util.Objects;

public class DatasetAnalysisInput implements AnalysisInput {
    private String documentId;

    public DatasetAnalysisInput() {
    }

    @JsonProperty("type")
    public AnalysisInputType getType() {
        return AnalysisInputType.DATASET;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public DatasetAnalysisInput documentId(String documentId) {
        this.setDocumentId(documentId);
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getDocumentId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DatasetAnalysisInput input = (DatasetAnalysisInput) o;
        if (input.getDocumentId() == null || getDocumentId() == null) {
            return false;
        }
        return Objects.equals(getDocumentId(), input.getDocumentId());
    }
}
