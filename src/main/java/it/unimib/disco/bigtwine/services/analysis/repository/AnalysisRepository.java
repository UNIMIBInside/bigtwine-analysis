package it.unimib.disco.bigtwine.services.analysis.repository;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;


/**
 * Spring Data MongoDB repository for the Analysis entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AnalysisRepository extends MongoRepository<Analysis, String> {
    List<Analysis> findByOwnerUid(String ownerId);
    Page<Analysis> findByOwnerUid(String ownerId, Pageable page);
    long countByOwnerUid(String owner);
    long countByStatus(AnalysisStatus status);
    Stream<Analysis> findByStatus(AnalysisStatus status, Pageable page);
}
