package it.unimib.disco.bigtwine.services.analysis.service;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing Analysis.
 */
@Service
public class AnalysisService {

    private final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private final AnalysisRepository analysisRepository;

    public AnalysisService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    /**
     * Save a analysis.
     *
     * @param analysis the entity to save
     * @return the persisted entity
     */
    public Analysis save(Analysis analysis) {
        log.debug("Request to save Analysis : {}", analysis);
        return analysisRepository.save(analysis);
    }

    /**
     * Get all the analyses.
     *
     * @return the list of entities
     */
    public List<Analysis> findAll() {
        log.debug("Request to get all Analyses");
        return analysisRepository.findAll();
    }


    /**
     * Get one analysis by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    public Optional<Analysis> findOne(String id) {
        log.debug("Request to get Analysis : {}", id);
        return analysisRepository.findById(id);
    }

    /**
     * Delete the analysis by id.
     *
     * @param id the id of the entity
     */
    public void delete(String id) {
        log.debug("Request to delete Analysis : {}", id);
        analysisRepository.deleteById(id);
    }
}
