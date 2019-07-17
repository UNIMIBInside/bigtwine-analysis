package it.unimib.disco.bigtwine.services.analysis.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = QueryAnalysisInput.class, name = AnalysisInputType.Constants.QUERY_VALUE),
    @JsonSubTypes.Type(value = DatasetAnalysisInput.class, name = AnalysisInputType.Constants.DATASET_VALUE),
})
public interface AnalysisInput {
    AnalysisInputType getType();
}
