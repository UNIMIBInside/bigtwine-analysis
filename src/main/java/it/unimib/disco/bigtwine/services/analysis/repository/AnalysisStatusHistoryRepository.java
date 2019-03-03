package it.unimib.disco.bigtwine.services.analysis.repository;

import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisStatusHistory;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


/**
 * Spring Data MongoDB repository for the AnalysisStatusHistory entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AnalysisStatusHistoryRepository extends MongoRepository<AnalysisStatusHistory, String> {

}
