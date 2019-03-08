package it.unimib.disco.bigtwine.services.analysis.service;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisStatusHistory;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisRepository;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisStatusHistoryRepository;
import it.unimib.disco.bigtwine.services.analysis.validation.AnalysisStatusValidator;
import it.unimib.disco.bigtwine.services.analysis.validation.InvalidAnalysisStatusException;
import it.unimib.disco.bigtwine.services.analysis.validation.InvalidAnalysisInputProvidedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
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
     *
     * @param analysis
     */
    private void setupAnalysisDefaults(Analysis analysis) {
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

    /**
     * Valida un'analisi e lancia eccezioni in caso di errori
     *
     * @param analysis Oggetto da validare
     * @throws InvalidAnalysisStatusException Eccezione lanciata in caso di update se lo status impostato non è valido
     * @throws InvalidAnalysisInputProvidedException Eccezione lanciata se non è stato fornito un input valido
     */
    private void validate(@NotNull Analysis analysis, Analysis oldAnalysis) {
        // Validate input
        switch (analysis.getInputType()) {
            case QUERY:
                if (analysis.getQuery() == null) {
                    throw new InvalidAnalysisInputProvidedException("Query not provided");
                }
                break;
            case DOCUMENT:
                if (analysis.getDocumentId() == null) {
                    throw new InvalidAnalysisInputProvidedException("Document id not provided");
                }
                break;
        }

        if (analysis.getQuery() != null && analysis.getDocumentId() != null) {
            throw new InvalidAnalysisInputProvidedException("Both query and document id provided");
        }

        // Validate status change
        if (oldAnalysis != null) {
            boolean isStatusChanged = oldAnalysis.getStatus() != analysis.getStatus();
            boolean statusChangeAllowed = this.analysisStatusValidator.validate(oldAnalysis.getStatus(), analysis.getStatus());
            if (isStatusChanged && !statusChangeAllowed) {
                throw new InvalidAnalysisStatusException(oldAnalysis.getStatus(), analysis.getStatus());
            }
        }
    }

    /**
     * Save a analysis.
     *
     * @param analysis the entity to save
     * @return the persisted entity
     * @throws InvalidAnalysisStatusException Lancia un errore se lo stato non è associabile all'analisi
     * @throws InvalidAnalysisInputProvidedException Lancia un errore se non è stato fornito un input valido
     */
    public Analysis save(Analysis analysis) {
        log.debug("Request to save Analysis : {}", analysis);
        Optional<Analysis> oldAnalysis = analysis.getId() != null ? this.findOne(analysis.getId()) : Optional.empty();
        boolean isUpdate = oldAnalysis.isPresent();

        if (!isUpdate) {
            this.setupAnalysisDefaults(analysis);
        }

        analysis.setUpdateDate(Instant.now());

        this.validate(analysis, oldAnalysis.orElse(null));

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

    /**
     * Restituisce la lista di tutti i cambi di stato dell'analisi indicata
     *
     * @param id the id of the entity
     * @return A change list of the analysis status
     */
    public List<AnalysisStatusHistory> getStatusHistory(String id) {
        return this.analysisStatusHistoryRepository.findByAnalysisId(id, Sort.by(Sort.Direction.DESC, "date"));
    }

}
