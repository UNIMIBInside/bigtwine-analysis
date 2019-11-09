package it.unimib.disco.bigtwine.services.analysis.config.dbmigrations;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import it.unimib.disco.bigtwine.services.analysis.config.AnalysisSettingConstants;
import it.unimib.disco.bigtwine.services.analysis.config.Constants;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisDefaultSetting;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisSetting;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import it.unimib.disco.bigtwine.services.analysis.security.AuthoritiesConstants;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeLog(order = "001")
public class SettingsMigration {
    @ChangeSet(order = "01", id = "addDefaultSettings", author = "fausto")
    public void addDefaultSettings(MongoTemplate mongoTemplate) {
        /*AnalysisSetting maxConcurrentAnalysesAll = new AnalysisSetting()
            .name(AnalysisSettingConstants.MAX_CONCURRENT_ANALYSES)
            .description("Max number of concurrent analysis started from an user (-1 to not limit)")
            .global(true)
            .userVisible(false);

        AnalysisSetting maxConcurrentAnalysesTNeelDataset = new AnalysisSetting()
            .name(AnalysisSettingConstants.MAX_CONCURRENT_ANALYSES)
            .description("Max number of concurrent analysis started from an user (-1 to not limit). Only for  Twitter NEEL analyses with DATASET input")
            .global(true)
            .userVisible(false)
            .analysisType(AnalysisType.TWITTER_NEEL)
            .addAnalysisInputTypes(AnalysisInputType.DATASET);

        mongoTemplate.save(maxConcurrentAnalysesAll);

        AnalysisDefaultSetting maxConcurrentAnalysesAllDefault = new AnalysisDefaultSetting()
            .setting(maxConcurrentAnalysesAll)
            .defaultValue(AnalysisSettingConstants.DEFAULT_MAX_CONCURRENT_ANALYSES)
            .userCanOverride(false);

        AnalysisDefaultSetting maxConcurrentAnalysesTNeelDatasetAdmin = new AnalysisDefaultSetting()
            .setting(maxConcurrentAnalysesTNeelDataset)
            .defaultValue(-1)
            .addUserRoles(AuthoritiesConstants.ADMIN)
            .userCanOverride(false);

        mongoTemplate.save(maxConcurrentAnalysesAllDefault);
        mongoTemplate.save(maxConcurrentAnalysesTNeelDatasetAdmin);*/
    }
}
