package it.unimib.disco.bigtwine.services.analysis.repository;

import it.unimib.disco.bigtwine.services.analysis.domain.NeelProcessedTweet;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


/**
 * Spring Data MongoDB repository for the NeelProcessedTweet entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NeelProcessedTweetRepository extends MongoRepository<NeelProcessedTweet, String> {

}
