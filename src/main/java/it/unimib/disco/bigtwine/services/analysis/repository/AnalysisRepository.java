package it.unimib.disco.bigtwine.services.analysis.repository;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data MongoDB repository for the Analysis entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AnalysisRepository extends MongoRepository<Analysis, String> {
    List<Analysis> findByOwner(String owner);
    Page<Analysis> findByOwner(String owner, Pageable page);
    long countByOwner(String owner);
}
