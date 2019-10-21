package it.unimib.disco.bigtwine.services.analysis.service;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisDefaultSetting;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisSetting;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisSettingResolved;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisDefaultSettingRepository;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing AnalysisSetting.
 */
@Service
public class AnalysisSettingService {

    private final Logger log = LoggerFactory.getLogger(AnalysisSettingService.class);

    private final AnalysisSettingRepository analysisSettingRepository;
    private final AnalysisDefaultSettingRepository analysisDefaultSettingRepository;

    public AnalysisSettingService(
        AnalysisSettingRepository analysisSettingRepository,
        AnalysisDefaultSettingRepository analysisDefaultSettingRepository) {
        this.analysisSettingRepository = analysisSettingRepository;
        this.analysisDefaultSettingRepository = analysisDefaultSettingRepository;
    }

    /**
     * Save a analysisSetting.
     *
     * @param analysisSetting the entity to save
     * @return the persisted entity
     */
    public AnalysisSetting save(AnalysisSetting analysisSetting) {
        log.debug("Request to save AnalysisSetting : {}", analysisSetting);
        return analysisSettingRepository.save(analysisSetting);
    }

    /**
     * Get all the analysisSettings.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    public Page<AnalysisSetting> findAll(Pageable pageable) {
        log.debug("Request to get all AnalysisSettings");
        return analysisSettingRepository.findAll(pageable);
    }

    /**
     * Get all the AnalysisSetting with eager load of many-to-many relationships.
     *
     * @return the list of entities
     */
    public Page<AnalysisSetting> findAllWithEagerRelationships(Pageable pageable) {
        return analysisSettingRepository.findAllWithEagerRelationships(pageable);
    }


    /**
     * Get one analysisSetting by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    public Optional<AnalysisSetting> findOne(String id) {
        log.debug("Request to get AnalysisSetting : {}", id);
        return analysisSettingRepository.findOneWithEagerRelationships(id);
    }

    /**
     * Find settings by analysis
     * @param analysis Analysis to filter
     * @return A list of settings related to the analysis
     */
    public List<AnalysisSetting> findByAnalysis(Analysis analysis) {
        return analysisSettingRepository.findByAnalysis(analysis.getType(), analysis.getInput().getType());
    }

    /**
     * Delete the analysisSetting by id.
     *
     * @param id the id of the entity
     */
    public void delete(String id) {
        log.debug("Request to delete AnalysisSetting : {}", id);
        analysisSettingRepository.deleteById(id);
    }

    /**
     * Restituisce le impostazioni predefinite per l'analisi e i ruoli utente indicati
     *
     * @param analysis L'analisi di cui si vogliono le impostazioni predefinite
     * @param userRoles I ruoli utente con cui filtrare le impostazioni
     * @return le impostazioni associate all'analisi
     */
    public List<AnalysisSettingResolved> resolveAnalysisSettings(Analysis analysis, List<String> userRoles, boolean includeHidden) {
        boolean isAnalysisEditable = analysis.getStatus() == AnalysisStatus.READY;
        List<AnalysisSetting> settings = this.findByAnalysis(analysis);
        List<AnalysisSettingResolved> resolvedSettings = new ArrayList<>();
        Set<String> distinctSettingNames = new HashSet<>();

        for (AnalysisSetting setting : settings) {
            if (!includeHidden && !setting.isUserVisible()) {
                continue;
            }

            if (distinctSettingNames.contains(setting.getName())) {
                continue;
            }

            AnalysisSettingResolved resolvedSetting = new AnalysisSettingResolved()
                .name(setting.getName())
                .editable(isAnalysisEditable)
                .type(setting.getType())
                .description(setting.getDescription())
                .choices(setting.getChoices());

            Optional<AnalysisDefaultSetting> defaultSetting = this.analysisDefaultSettingRepository
                .findOneBySettingAndRoles(setting, userRoles);

            if (defaultSetting.isPresent()) {
                resolvedSetting
                    .defaultValue(defaultSetting.get().getDefaultValue())
                    .editable(isAnalysisEditable && defaultSetting.get().isUserCanOverride());
            }

            if (analysis.getSettings() != null && analysis.getSettings().containsKey(setting.getName())) {
                resolvedSetting.setCurrentValue(analysis.getSettings().get(setting.getName()));
            }

            resolvedSettings.add(resolvedSetting);
            distinctSettingNames.add(setting.getName());
        }

        return resolvedSettings;
    }

    public Map<String, Object> getAnalysisSettingsDefaultValues(Analysis analysis, List<String> userRoles) {
        Map<String, Object> defaults = new HashMap<>();
        List<AnalysisSettingResolved> resolvedSettings = this.resolveAnalysisSettings(analysis, userRoles, true);

        for (AnalysisSettingResolved setting : resolvedSettings) {
            defaults.put(setting.getName(), setting.getDefaultValue());
        }

        return defaults;
    }

    public void cleanAnalysisSettings(Analysis analysis, List<String> userRoles) {
        if (analysis.getSettings() == null || analysis.getSettings().size() == 0) {
            return;
        }

        Map<String, AnalysisSettingResolved> settings = this
            .resolveAnalysisSettings(analysis, userRoles, true)
            .stream()
            .collect(Collectors.toMap(AnalysisSettingResolved::getName, Function.identity()));

        Map<String, Object> cleanedSettings = new HashMap<>(analysis.getSettings());
        for (Map.Entry<String, Object> option: analysis.getSettings().entrySet()) {
            if (settings.containsKey(option.getKey()) && !settings.get(option.getKey()).isEditable()) {
                cleanedSettings.replace(option.getKey(), settings.get(option.getKey()).getDefaultValue());
            }
        }

        analysis.setSettings(cleanedSettings);
    }
}
