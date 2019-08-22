package it.unimib.disco.bigtwine.services.analysis.service;

import it.unimib.disco.bigtwine.commons.messaging.AnalysisStatusChangeRequestedEvent;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisStatusHistory;
import it.unimib.disco.bigtwine.services.analysis.domain.User;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisStatusMapper;
import it.unimib.disco.bigtwine.services.analysis.messaging.AnalysisStatusChangeRequestProducerChannel;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisRepository;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisResultsRepository;
import it.unimib.disco.bigtwine.services.analysis.validation.AnalysisStatusValidator;
import it.unimib.disco.bigtwine.services.analysis.validation.InvalidAnalysisStatusException;
import it.unimib.disco.bigtwine.services.analysis.validation.analysis.input.AnalysisInputValidatorLocator;
import it.unimib.disco.bigtwine.services.analysis.validation.analysis.input.InvalidAnalysisInputProvidedException;
import it.unimib.disco.bigtwine.services.analysis.web.api.util.AnalysisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
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
    private final AnalysisResultsRepository analysisResultsRepository;

    private final AnalysisStatusValidator analysisStatusValidator;
    private final AnalysisInputValidatorLocator inputValidatorLocator;

    private final MessageChannel statusChangeRequestsChannel;

    public AnalysisService(
        AnalysisRepository analysisRepository,
        AnalysisResultsRepository analysisResultsRepository,
        AnalysisStatusValidator analysisStatusValidator,
        AnalysisInputValidatorLocator inputValidatorLocator,
        AnalysisStatusChangeRequestProducerChannel channel) {
        this.analysisRepository = analysisRepository;
        this.analysisResultsRepository = analysisResultsRepository;
        this.analysisStatusValidator = analysisStatusValidator;
        this.inputValidatorLocator = inputValidatorLocator;
        this.statusChangeRequestsChannel = channel.analysisStatusChangeRequestsChannel();
    }

    /**
     * Imposta i parametri predefiniti dell'analisis in caso siano null
     *
     * @param analysis L'analisi da impostare
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
        if (analysis.getInput() == null || analysis.getInput().getType() == null) {
            throw new InvalidAnalysisInputProvidedException("Input not provided");
        }

        this.inputValidatorLocator
            .getValidator(analysis.getInput().getType())
            .validate(analysis.getInput());

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
     * Salva un analisi, registra l'eventuale cambio di stato e lo notifica con l'invio di un evento.
     *
     * @param analysis L'analisis da salvare
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
     * Get all the analyses (paged).
     *
     * @return the list of entities
     */
    public Page<Analysis> findAll(Pageable page) {
        log.debug("Request to get all Analyses");
        return analysisRepository.findAll(page);
    }

    /**
     * Return the total number of analysis in db
     *
     * @return Total number of analysis in db
     */
    public Long countAll() {
        return this.analysisRepository.count();
    }

    /**
     * Get all the analyses of an user.
     *
     * @return the list of entities
     */
    public List<Analysis> findByOwner(String owner) {
        log.debug("Request to get all Analyses of an user");
        return analysisRepository.findByOwnerUid(owner);
    }

    /**
     * Get all the analyses of an user (paged).
     *
     * @return the list of entities
     */
    public Page<Analysis> findByOwner(String owner, Pageable page) {
        log.debug("Request to get all Analyses of an user");
        return analysisRepository.findByOwnerUid(owner, page);
    }

    /**
     * Return the number of analyses owned by indicated user
     *
     * @param owner The owner of the analyses
     * @return the number of analyses owned by indicated user
     */
    public Long countByOwner(String owner) {
        return analysisRepository.countByOwnerUid(owner);
    }


    /**
     * Get one analysis by uid.
     *
     * @param id the uid of the entity
     * @return the entity
     */
    public Optional<Analysis> findOne(String id) {
        log.debug("Request to get Analysis : {}", id);
        return analysisRepository.findById(id);
    }

    /**
     * Delete the analysis by uid.
     *
     * @param id the uid of the entity
     */
    public void delete(String id) {
        log.debug("Request to delete Analysis : {}", id);
        analysisRepository.deleteById(id);
    }

    /**
     * Restituisce la lista di tutti i cambi di stato dell'analisi indicata
     *
     * @param id the uid of the entity
     * @return A change list of the analysis status
     */
    public List<AnalysisStatusHistory> getStatusHistory(String id) {
        Analysis analysis = this.findOne(id).orElse(null);
        if (analysis == null) {
            return null;
        } else {
            return analysis.getStatusHistory();
        }
    }

    public void requestStatusChange(@NotNull Analysis analysis,@NotNull AnalysisStatus newStatus, boolean userRequested) {
        if (analysis.getId() == null) {
            throw new IllegalArgumentException("analysis hasn't an uid");
        }

        if (!this.analysisStatusValidator.validate(analysis.getStatus(), newStatus)) {
            throw new InvalidAnalysisStatusException(analysis.getStatus(), newStatus);
        }

        if (analysis.getStatus() == newStatus) {
            return;
        }

        User user = null;
        if (userRequested) {
            user = AnalysisUtil.getCurrentUser().orElse(null);
        }

        AnalysisStatusChangeRequestedEvent event = new AnalysisStatusChangeRequestedEvent();
        event.setAnalysisId(analysis.getId());
        event.setDesiredStatus(AnalysisStatusMapper.INSTANCE.analysisStatusEventEnumFromDomain(newStatus));
        event.setUser(user);

        Message<AnalysisStatusChangeRequestedEvent> message = MessageBuilder
            .withPayload(event)
            .build();

        this.statusChangeRequestsChannel.send(message);
    }

    public Analysis saveAnalysisStatusChange(String analysisId, AnalysisStatus newStatus, User user, String message) {
        Optional<Analysis> analysisOpt = this.findOne(analysisId);

        if(!analysisOpt.isPresent()) {
            return null;
        }

        Analysis analysis = analysisOpt.get();
        AnalysisStatus oldStatus = analysis.getStatus();

        if (newStatus == null) {
            // Status unchanged
            newStatus = oldStatus;
        }

        if (newStatus != oldStatus) {
            analysis.setStatus(newStatus);

            if (newStatus != AnalysisStatus.STARTED) {
                long resultsCount = this.analysisResultsRepository.countByAnalysisId(analysisId);
                analysis.setResultsCount(resultsCount);
            }
        }

        AnalysisStatusHistory statusChange = new AnalysisStatusHistory()
            .oldStatus(oldStatus)
            .newStatus(newStatus)
            .date(Instant.now())
            .user(user)
            .message(message);

        analysis.addStatusChange(statusChange);

        try {
            analysis = this.save(analysis);
        } catch (ValidationException e) {
            log.error("Cannot save status change", e);
            return null;
        }

        return analysis;
    }

    public Analysis saveAnalysisProgressUpdate(String analysisId, double progress) {
        Optional<Analysis> analysisOpt = this.findOne(analysisId);

        if(!analysisOpt.isPresent()) {
            return null;
        }

        Analysis analysis = analysisOpt.get();
        analysis.setProgress(progress);

        try {
            return this.save(analysis);
        } catch (ValidationException e) {
            log.error("Cannot save progress update", e);
            return null;
        }
    }
}
