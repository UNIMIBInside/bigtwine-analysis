package it.unimib.disco.bigtwine.services.analysis.domain.enumeration;

import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisInput;
import it.unimib.disco.bigtwine.services.analysis.domain.DatasetAnalysisInput;
import it.unimib.disco.bigtwine.services.analysis.domain.QueryAnalysisInput;

/**
 * The AnalysisInputType enumeration.
 */
public enum AnalysisInputType {
    QUERY(Constants.QUERY_VALUE, QueryAnalysisInput.class),
    DATASET(Constants.DATASET_VALUE, DatasetAnalysisInput.class);

    public final String value;
    public final Class<? extends AnalysisInput> inputClass;

    AnalysisInputType(String value, Class<? extends AnalysisInput> inputClass) {
        this.value = value;
        this.inputClass = inputClass;
    }

    public static class Constants {
        public static final String QUERY_VALUE = "QUERY";
        public static final String DATASET_VALUE = "DATASET";
    }
}
