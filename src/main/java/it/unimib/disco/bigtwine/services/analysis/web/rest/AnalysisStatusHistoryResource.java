package it.unimib.disco.bigtwine.services.analysis.web.rest;

import com.codahale.metrics.annotation.Timed;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisStatusHistory;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisStatusHistoryRepository;
import it.unimib.disco.bigtwine.services.analysis.security.AuthoritiesConstants;
import it.unimib.disco.bigtwine.services.analysis.web.rest.errors.BadRequestAlertException;
import it.unimib.disco.bigtwine.services.analysis.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing AnalysisStatusHistory.
 */
@RestController
@RequestMapping("/api")
public class AnalysisStatusHistoryResource {

    private final Logger log = LoggerFactory.getLogger(AnalysisStatusHistoryResource.class);

    private static final String ENTITY_NAME = "analysisAnalysisStatusHistory";

    private final AnalysisStatusHistoryRepository analysisStatusHistoryRepository;

    public AnalysisStatusHistoryResource(AnalysisStatusHistoryRepository analysisStatusHistoryRepository) {
        this.analysisStatusHistoryRepository = analysisStatusHistoryRepository;
    }

    /**
     * POST  /analysis-status-histories : Create a new analysisStatusHistory.
     *
     * @param analysisStatusHistory the analysisStatusHistory to create
     * @return the ResponseEntity with status 201 (Created) and with body the new analysisStatusHistory, or with status 400 (Bad Request) if the analysisStatusHistory has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/analysis-status-histories")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AnalysisStatusHistory> createAnalysisStatusHistory(@Valid @RequestBody AnalysisStatusHistory analysisStatusHistory) throws URISyntaxException {
        log.debug("REST request to save AnalysisStatusHistory : {}", analysisStatusHistory);
        if (analysisStatusHistory.getId() != null) {
            throw new BadRequestAlertException("A new analysisStatusHistory cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AnalysisStatusHistory result = analysisStatusHistoryRepository.save(analysisStatusHistory);
        return ResponseEntity.created(new URI("/api/analysis-status-histories/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /analysis-status-histories : Updates an existing analysisStatusHistory.
     *
     * @param analysisStatusHistory the analysisStatusHistory to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated analysisStatusHistory,
     * or with status 400 (Bad Request) if the analysisStatusHistory is not valid,
     * or with status 500 (Internal Server Error) if the analysisStatusHistory couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/analysis-status-histories")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AnalysisStatusHistory> updateAnalysisStatusHistory(@Valid @RequestBody AnalysisStatusHistory analysisStatusHistory) throws URISyntaxException {
        log.debug("REST request to update AnalysisStatusHistory : {}", analysisStatusHistory);
        if (analysisStatusHistory.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AnalysisStatusHistory result = analysisStatusHistoryRepository.save(analysisStatusHistory);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, analysisStatusHistory.getId().toString()))
            .body(result);
    }

    /**
     * GET  /analysis-status-histories : get all the analysisStatusHistories.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of analysisStatusHistories in body
     */
    @GetMapping("/analysis-status-histories")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<AnalysisStatusHistory> getAllAnalysisStatusHistories() {
        log.debug("REST request to get all AnalysisStatusHistories");
        return analysisStatusHistoryRepository.findAll();
    }

    /**
     * GET  /analysis-status-histories/:id : get the "id" analysisStatusHistory.
     *
     * @param id the id of the analysisStatusHistory to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the analysisStatusHistory, or with status 404 (Not Found)
     */
    @GetMapping("/analysis-status-histories/{id}")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AnalysisStatusHistory> getAnalysisStatusHistory(@PathVariable String id) {
        log.debug("REST request to get AnalysisStatusHistory : {}", id);
        Optional<AnalysisStatusHistory> analysisStatusHistory = analysisStatusHistoryRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(analysisStatusHistory);
    }

    /**
     * DELETE  /analysis-status-histories/:id : delete the "id" analysisStatusHistory.
     *
     * @param id the id of the analysisStatusHistory to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/analysis-status-histories/{id}")
    @Timed
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteAnalysisStatusHistory(@PathVariable String id) {
        log.debug("REST request to delete AnalysisStatusHistory : {}", id);

        analysisStatusHistoryRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }
}
