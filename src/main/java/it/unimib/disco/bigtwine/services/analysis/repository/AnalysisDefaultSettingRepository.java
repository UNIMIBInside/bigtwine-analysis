package it.unimib.disco.bigtwine.services.analysis.repository;

import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisDefaultSetting;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisSetting;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the AnalysisDefaultSetting entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AnalysisDefaultSettingRepository extends MongoRepository<AnalysisDefaultSetting, String> {
    @Query("{}")
    Page<AnalysisDefaultSetting> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<AnalysisDefaultSetting> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<AnalysisDefaultSetting> findOneWithEagerRelationships(String id);

    @Query("{$and:[{'setting.$id': ?0}, {$or:[{user_roles:[]}, {user_roles:{$in: ?1}}]}]}")
    Optional<AnalysisDefaultSetting> findOneBySettingAndRoles(ObjectId settingId, List<String> roles, Sort sort);

    default Optional<AnalysisDefaultSetting> findOneBySettingAndRoles(AnalysisSetting setting, List<String> roles) {
        return findOneBySettingAndRoles(new ObjectId(setting.getId()), roles, Sort.by(Sort.Direction.DESC, "priority"));
    }
}
