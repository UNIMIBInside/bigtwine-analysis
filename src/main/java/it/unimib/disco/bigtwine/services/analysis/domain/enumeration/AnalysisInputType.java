package it.unimib.disco.bigtwine.services.analysis.domain.enumeration;

/**
 * The AnalysisInputType enumeration.
 */
public enum AnalysisInputType {
    QUERY, DATASET;

    public static class Constants {
        public static final String QUERY_VALUE = "QUERY";
        public static final String DATASET_VALUE = "DATASET";
    }
}
