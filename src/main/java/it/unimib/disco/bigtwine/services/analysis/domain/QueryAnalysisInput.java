package it.unimib.disco.bigtwine.services.analysis.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.Objects;

public class QueryAnalysisInput implements AnalysisInput {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Transient
    private final AnalysisInputType type = AnalysisInputType.QUERY;
    private List<String> tokens;
    private JoinOperator joinOperator;

    public QueryAnalysisInput() {
    }

    public AnalysisInputType getType() {
        return this.type;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public QueryAnalysisInput tokens(List<String> tokens) {
        this.setTokens(tokens);
        return this;
    }

    public JoinOperator getJoinOperator() {
        return joinOperator;
    }

    public void setJoinOperator(JoinOperator joinOperator) {
        this.joinOperator = joinOperator;
    }

    public QueryAnalysisInput joinOperator(JoinOperator joinOperator) {
        this.setJoinOperator(joinOperator);
        return this;
    }

    public enum JoinOperator {
        AND, OR
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getTokens(), this.getJoinOperator());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryAnalysisInput input = (QueryAnalysisInput) o;
        if (input.getTokens() == null || getTokens() == null ||
            input.getJoinOperator() == null || getJoinOperator() == null) {
            return false;
        }
        return Objects.equals(getTokens(), input.getTokens()) &&
            Objects.equals(getJoinOperator(), input.getJoinOperator());
    }
}
