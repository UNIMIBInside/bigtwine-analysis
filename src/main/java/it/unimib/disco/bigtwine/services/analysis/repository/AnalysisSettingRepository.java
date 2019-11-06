package it.unimib.disco.bigtwine.services.analysis.repository;

import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisSetting;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Spring Data MongoDB repository for the AnalysisSetting entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AnalysisSettingRepository extends MongoRepository<AnalysisSetting, String> {
    @Query("{}")
    Page<AnalysisSetting> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<AnalysisSetting> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<AnalysisSetting> findOneWithEagerRelationships(String id);

    Optional<AnalysisSetting> findOneById(String id);

    List<AnalysisSetting> findByNameAndGlobalIsTrue(String name);

    @Query("{$and: [{$or: [{analysis_type: null}, {analysis_type: ?0}]}, {$or: [{analysis_input_types: []}, {analysis_input_types: {$in: [ ?1 ]}}]}]}")
    List<AnalysisSetting> findByAnalysis(AnalysisType analysisType, AnalysisInputType inputType, Sort sort);

    default List<AnalysisSetting> findByAnalysis(AnalysisType analysisType, AnalysisInputType inputType) {
        return findByAnalysis(analysisType, inputType, Sort.by(Sort.Direction.DESC, "analysis_type"));
    }

    default List<AnalysisSetting> findVisibleByAnalysis(AnalysisType analysisType, AnalysisInputType inputType) {
        return this.findByAnalysis(analysisType, inputType).stream()
            .filter(AnalysisSetting::isUserVisible)
            .collect(Collectors.toList());
    }
}
