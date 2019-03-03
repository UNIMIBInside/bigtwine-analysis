package it.unimib.disco.bigtwine.services.analysis.web.rest;

import com.codahale.metrics.annotation.Timed;
import it.unimib.disco.bigtwine.services.analysis.domain.NeelProcessedTweet;
import it.unimib.disco.bigtwine.services.analysis.repository.NeelProcessedTweetRepository;
import it.unimib.disco.bigtwine.services.analysis.web.rest.errors.BadRequestAlertException;
import it.unimib.disco.bigtwine.services.analysis.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing NeelProcessedTweet.
 */
@RestController
@RequestMapping("/api")
public class NeelProcessedTweetResource {

    private final Logger log = LoggerFactory.getLogger(NeelProcessedTweetResource.class);

    private static final String ENTITY_NAME = "analysisNeelProcessedTweet";

    private final NeelProcessedTweetRepository neelProcessedTweetRepository;

    public NeelProcessedTweetResource(NeelProcessedTweetRepository neelProcessedTweetRepository) {
        this.neelProcessedTweetRepository = neelProcessedTweetRepository;
    }

    /**
     * POST  /neel-processed-tweets : Create a new neelProcessedTweet.
     *
     * @param neelProcessedTweet the neelProcessedTweet to create
     * @return the ResponseEntity with status 201 (Created) and with body the new neelProcessedTweet, or with status 400 (Bad Request) if the neelProcessedTweet has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/neel-processed-tweets")
    @Timed
    public ResponseEntity<NeelProcessedTweet> createNeelProcessedTweet(@Valid @RequestBody NeelProcessedTweet neelProcessedTweet) throws URISyntaxException {
        log.debug("REST request to save NeelProcessedTweet : {}", neelProcessedTweet);
        if (neelProcessedTweet.getId() != null) {
            throw new BadRequestAlertException("A new neelProcessedTweet cannot already have an ID", ENTITY_NAME, "idexists");
        }
        NeelProcessedTweet result = neelProcessedTweetRepository.save(neelProcessedTweet);
        return ResponseEntity.created(new URI("/api/neel-processed-tweets/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /neel-processed-tweets : Updates an existing neelProcessedTweet.
     *
     * @param neelProcessedTweet the neelProcessedTweet to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated neelProcessedTweet,
     * or with status 400 (Bad Request) if the neelProcessedTweet is not valid,
     * or with status 500 (Internal Server Error) if the neelProcessedTweet couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/neel-processed-tweets")
    @Timed
    public ResponseEntity<NeelProcessedTweet> updateNeelProcessedTweet(@Valid @RequestBody NeelProcessedTweet neelProcessedTweet) throws URISyntaxException {
        log.debug("REST request to update NeelProcessedTweet : {}", neelProcessedTweet);
        if (neelProcessedTweet.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        NeelProcessedTweet result = neelProcessedTweetRepository.save(neelProcessedTweet);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, neelProcessedTweet.getId().toString()))
            .body(result);
    }

    /**
     * GET  /neel-processed-tweets : get all the neelProcessedTweets.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of neelProcessedTweets in body
     */
    @GetMapping("/neel-processed-tweets")
    @Timed
    public List<NeelProcessedTweet> getAllNeelProcessedTweets() {
        log.debug("REST request to get all NeelProcessedTweets");
        return neelProcessedTweetRepository.findAll();
    }

    /**
     * GET  /neel-processed-tweets/:id : get the "id" neelProcessedTweet.
     *
     * @param id the id of the neelProcessedTweet to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the neelProcessedTweet, or with status 404 (Not Found)
     */
    @GetMapping("/neel-processed-tweets/{id}")
    @Timed
    public ResponseEntity<NeelProcessedTweet> getNeelProcessedTweet(@PathVariable String id) {
        log.debug("REST request to get NeelProcessedTweet : {}", id);
        Optional<NeelProcessedTweet> neelProcessedTweet = neelProcessedTweetRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(neelProcessedTweet);
    }

    /**
     * DELETE  /neel-processed-tweets/:id : delete the "id" neelProcessedTweet.
     *
     * @param id the id of the neelProcessedTweet to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/neel-processed-tweets/{id}")
    @Timed
    public ResponseEntity<Void> deleteNeelProcessedTweet(@PathVariable String id) {
        log.debug("REST request to delete NeelProcessedTweet : {}", id);

        neelProcessedTweetRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }
}
