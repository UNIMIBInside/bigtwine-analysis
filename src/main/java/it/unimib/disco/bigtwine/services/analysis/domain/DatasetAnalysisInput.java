package it.unimib.disco.bigtwine.services.analysis.domain;

public class DatasetAnalysisInput implements AnalysisInput {
    private String documentId;

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
}
