package it.unimib.disco.bigtwine.services.analysis.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.unimib.disco.bigtwine.commons.models.ProcessedTweet;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A NeelProcessedTweet.
 */
@Document(collection = "neel_processed_tweet")
public class NeelProcessedTweet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("save_date")
    private Instant saveDate;

    @DBRef
    @Field("analysis")
    @JsonIgnoreProperties("")
    private Analysis analysis;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    @NotNull
    @Field("tweet")
    private ProcessedTweet tweet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getSaveDate() {
        return saveDate;
    }

    public NeelProcessedTweet saveDate(Instant saveDate) {
        this.saveDate = saveDate;
        return this;
    }

    public void setSaveDate(Instant saveDate) {
        this.saveDate = saveDate;
    }

    public ProcessedTweet getTweet() {
        return tweet;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public NeelProcessedTweet analysis(Analysis analysis) {
        this.analysis = analysis;
        return this;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    public void setTweet(ProcessedTweet tweet) {
        this.tweet = tweet;
    }

    public NeelProcessedTweet tweet(ProcessedTweet tweet) {
        this.tweet = tweet;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NeelProcessedTweet neelProcessedTweet = (NeelProcessedTweet) o;
        if (neelProcessedTweet.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), neelProcessedTweet.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "NeelProcessedTweet{" +
            "id=" + getId() +
            ", saveDate='" + getSaveDate() + "'" +
            "}";
    }
}
