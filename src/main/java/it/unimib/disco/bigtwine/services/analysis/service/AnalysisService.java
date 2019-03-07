package it.unimib.disco.bigtwine.services.analysis.service;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisStatusHistory;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisRepository;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisStatusHistoryRepository;
import it.unimib.disco.bigtwine.services.analysis.validation.AnalysisStatusValidator;
import it.unimib.disco.bigtwine.services.analysis.validation.InvalidAnalysisStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Service Implementation for managing Analysis.
 */
@Service
public class AnalysisService {

    private final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private final AnalysisRepository analysisRepository;
    private final AnalysisStatusHistoryRepository analysisStatusHistoryRepository;

    private final AnalysisStatusValidator analysisStatusValidator;

    public AnalysisService(AnalysisRepository analysisRepository, AnalysisStatusHistoryRepository analysisStatusHistoryRepository, AnalysisStatusValidator analysisStatusValidator) {
        this.analysisRepository = analysisRepository;
        this.analysisStatusHistoryRepository = analysisStatusHistoryRepository;
        this.analysisStatusValidator = analysisStatusValidator;
    }

    /**
     * Save a analysis.
     *
     * @param analysis the entity to save
     * @return the persisted entity
     * @throws InvalidAnalysisStatusException Restituisce errore se lo stato non è associabile all'analisi
     */
    public Analysis save(Analysis analysis) {
        log.debug("Request to save Analysis : {}", analysis);
        Optional<Analysis> oldAnalysis = analysis.getId() != null ? this.findOne(analysis.getId()) : Optional.empty();
        boolean isUpdate = oldAnalysis.isPresent();

        if (isUpdate) {
            boolean isStatusChanged = oldAnalysis.get().getStatus() != analysis.getStatus();
            boolean statusChangeAllowed = this.analysisStatusValidator.validate(oldAnalysis.get().getStatus(), analysis.getStatus());
            if (isStatusChanged && !statusChangeAllowed) {
                throw new InvalidAnalysisStatusException(oldAnalysis.get().getStatus(), analysis.getStatus());
            }
        }else {
            if (analysis.getCreateDate() == null) {
                analysis.setCreateDate(Instant.now());
            }

            if (analysis.getStatus() == null) {
                analysis.setStatus(Analysis.DEFAULT_STATUS);
            }

            if (analysis.getVisibility() == null) {
                analysis.setVisibility(Analysis.DEFAULT_VISIBILITY);
            }
        }

        analysis.setUpdateDate(Instant.now());


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
     * Get all the analyses of an user.
     *
     * @return the list of entities
     */
    public List<Analysis> findByOwner(String owner) {
        log.debug("Request to get all Analyses of an user");
        return analysisRepository.findByOwner(owner);
    }

    /**
     * Get all the analyses of an user (paged).
     *
     * @return the list of entities
     */
    public Page<Analysis> findByOwner(String owner, Pageable page) {
        log.debug("Request to get all Analyses of an user");
        return analysisRepository.findByOwner(owner, page);
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
