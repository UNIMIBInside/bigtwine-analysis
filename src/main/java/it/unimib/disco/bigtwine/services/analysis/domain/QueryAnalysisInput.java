package it.unimib.disco.bigtwine.services.analysis.domain;

import java.util.List;

public class QueryAnalysisInput implements AnalysisInput {
    private List<String> tokens;
    private JoinOperator joinOperator;

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
}
