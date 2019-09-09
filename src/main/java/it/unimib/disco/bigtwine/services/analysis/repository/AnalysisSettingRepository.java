package it.unimib.disco.bigtwine.services.analysis.repository;

import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisSetting;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query("{$and:[{$or:[{user_roles:[]},{user_roles:{$in: ?0}}]},{$or:[{analysis_types:[]},{analysis_types:{$in:[?1]}}]},{$or:[{analysis_input_types:[]},{analysis_input_types:{$in:[?2]}}]}]}}")
    List<AnalysisSetting> findByRolesAndAnalysis(List<String> roles, AnalysisType analysisType, AnalysisInputType inputType);

    Optional<AnalysisSetting> findOneById(String id);

    default List<AnalysisSetting> findByRolesAndAnalysisDistinct(List<String> roles, AnalysisType analysisType, AnalysisInputType inputType) {
        List<AnalysisSetting> allSettings = this.findByRolesAndAnalysis(roles, analysisType, inputType)
            .stream()
            .sorted(Comparator.comparing(AnalysisSetting::getName).thenComparing(
                Comparator.comparing(AnalysisSetting::getPriority).reversed()))
            .collect(Collectors.toList());

        List<AnalysisSetting> settings = new ArrayList<>();
        String lastSettName = null;
        for (AnalysisSetting setting: allSettings) {
            if (setting.getName().equals(lastSettName)) {
                continue;
            }

            lastSettName = setting.getName();
            settings.add(setting);
        }

        return settings;
    }

}
