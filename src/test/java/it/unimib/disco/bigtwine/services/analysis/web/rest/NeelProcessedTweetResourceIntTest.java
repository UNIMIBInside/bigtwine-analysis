package it.unimib.disco.bigtwine.services.analysis.web.rest;

import it.unimib.disco.bigtwine.services.analysis.AnalysisApp;

import it.unimib.disco.bigtwine.services.analysis.domain.NeelProcessedTweet;
import it.unimib.disco.bigtwine.services.analysis.repository.NeelProcessedTweetRepository;
import it.unimib.disco.bigtwine.services.analysis.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;


import static it.unimib.disco.bigtwine.services.analysis.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the NeelProcessedTweetResource REST controller.
 *
 * @see NeelProcessedTweetResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnalysisApp.class)
public class NeelProcessedTweetResourceIntTest {

    private static final Instant DEFAULT_SAVE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_SAVE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private NeelProcessedTweetRepository neelProcessedTweetRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restNeelProcessedTweetMockMvc;

    private NeelProcessedTweet neelProcessedTweet;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final NeelProcessedTweetResource neelProcessedTweetResource = new NeelProcessedTweetResource(neelProcessedTweetRepository);
        this.restNeelProcessedTweetMockMvc = MockMvcBuilders.standaloneSetup(neelProcessedTweetResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NeelProcessedTweet createEntity() {
        NeelProcessedTweet neelProcessedTweet = new NeelProcessedTweet()
            .saveDate(DEFAULT_SAVE_DATE);
        return neelProcessedTweet;
    }

    @Before
    public void initTest() {
        neelProcessedTweetRepository.deleteAll();
        neelProcessedTweet = createEntity();
    }

    @Test
    public void createNeelProcessedTweet() throws Exception {
        int databaseSizeBeforeCreate = neelProcessedTweetRepository.findAll().size();

        // Create the NeelProcessedTweet
        restNeelProcessedTweetMockMvc.perform(post("/api/neel-processed-tweets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(neelProcessedTweet)))
            .andExpect(status().isCreated());

        // Validate the NeelProcessedTweet in the database
        List<NeelProcessedTweet> neelProcessedTweetList = neelProcessedTweetRepository.findAll();
        assertThat(neelProcessedTweetList).hasSize(databaseSizeBeforeCreate + 1);
        NeelProcessedTweet testNeelProcessedTweet = neelProcessedTweetList.get(neelProcessedTweetList.size() - 1);
        assertThat(testNeelProcessedTweet.getSaveDate()).isEqualTo(DEFAULT_SAVE_DATE);
    }

    @Test
    public void createNeelProcessedTweetWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = neelProcessedTweetRepository.findAll().size();

        // Create the NeelProcessedTweet with an existing ID
        neelProcessedTweet.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restNeelProcessedTweetMockMvc.perform(post("/api/neel-processed-tweets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(neelProcessedTweet)))
            .andExpect(status().isBadRequest());

        // Validate the NeelProcessedTweet in the database
        List<NeelProcessedTweet> neelProcessedTweetList = neelProcessedTweetRepository.findAll();
        assertThat(neelProcessedTweetList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void checkSaveDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = neelProcessedTweetRepository.findAll().size();
        // set the field null
        neelProcessedTweet.setSaveDate(null);

        // Create the NeelProcessedTweet, which fails.

        restNeelProcessedTweetMockMvc.perform(post("/api/neel-processed-tweets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(neelProcessedTweet)))
            .andExpect(status().isBadRequest());

        List<NeelProcessedTweet> neelProcessedTweetList = neelProcessedTweetRepository.findAll();
        assertThat(neelProcessedTweetList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllNeelProcessedTweets() throws Exception {
        // Initialize the database
        neelProcessedTweetRepository.save(neelProcessedTweet);

        // Get all the neelProcessedTweetList
        restNeelProcessedTweetMockMvc.perform(get("/api/neel-processed-tweets?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(neelProcessedTweet.getId())))
            .andExpect(jsonPath("$.[*].saveDate").value(hasItem(DEFAULT_SAVE_DATE.toString())));
    }
    
    @Test
    public void getNeelProcessedTweet() throws Exception {
        // Initialize the database
        neelProcessedTweetRepository.save(neelProcessedTweet);

        // Get the neelProcessedTweet
        restNeelProcessedTweetMockMvc.perform(get("/api/neel-processed-tweets/{id}", neelProcessedTweet.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(neelProcessedTweet.getId()))
            .andExpect(jsonPath("$.saveDate").value(DEFAULT_SAVE_DATE.toString()));
    }

    @Test
    public void getNonExistingNeelProcessedTweet() throws Exception {
        // Get the neelProcessedTweet
        restNeelProcessedTweetMockMvc.perform(get("/api/neel-processed-tweets/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateNeelProcessedTweet() throws Exception {
        // Initialize the database
        neelProcessedTweetRepository.save(neelProcessedTweet);

        int databaseSizeBeforeUpdate = neelProcessedTweetRepository.findAll().size();

        // Update the neelProcessedTweet
        NeelProcessedTweet updatedNeelProcessedTweet = neelProcessedTweetRepository.findById(neelProcessedTweet.getId()).get();
        updatedNeelProcessedTweet
            .saveDate(UPDATED_SAVE_DATE);

        restNeelProcessedTweetMockMvc.perform(put("/api/neel-processed-tweets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedNeelProcessedTweet)))
            .andExpect(status().isOk());

        // Validate the NeelProcessedTweet in the database
        List<NeelProcessedTweet> neelProcessedTweetList = neelProcessedTweetRepository.findAll();
        assertThat(neelProcessedTweetList).hasSize(databaseSizeBeforeUpdate);
        NeelProcessedTweet testNeelProcessedTweet = neelProcessedTweetList.get(neelProcessedTweetList.size() - 1);
        assertThat(testNeelProcessedTweet.getSaveDate()).isEqualTo(UPDATED_SAVE_DATE);
    }

    @Test
    public void updateNonExistingNeelProcessedTweet() throws Exception {
        int databaseSizeBeforeUpdate = neelProcessedTweetRepository.findAll().size();

        // Create the NeelProcessedTweet

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restNeelProcessedTweetMockMvc.perform(put("/api/neel-processed-tweets")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(neelProcessedTweet)))
            .andExpect(status().isBadRequest());

        // Validate the NeelProcessedTweet in the database
        List<NeelProcessedTweet> neelProcessedTweetList = neelProcessedTweetRepository.findAll();
        assertThat(neelProcessedTweetList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteNeelProcessedTweet() throws Exception {
        // Initialize the database
        neelProcessedTweetRepository.save(neelProcessedTweet);

        int databaseSizeBeforeDelete = neelProcessedTweetRepository.findAll().size();

        // Get the neelProcessedTweet
        restNeelProcessedTweetMockMvc.perform(delete("/api/neel-processed-tweets/{id}", neelProcessedTweet.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<NeelProcessedTweet> neelProcessedTweetList = neelProcessedTweetRepository.findAll();
        assertThat(neelProcessedTweetList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(NeelProcessedTweet.class);
        NeelProcessedTweet neelProcessedTweet1 = new NeelProcessedTweet();
        neelProcessedTweet1.setId("id1");
        NeelProcessedTweet neelProcessedTweet2 = new NeelProcessedTweet();
        neelProcessedTweet2.setId(neelProcessedTweet1.getId());
        assertThat(neelProcessedTweet1).isEqualTo(neelProcessedTweet2);
        neelProcessedTweet2.setId("id2");
        assertThat(neelProcessedTweet1).isNotEqualTo(neelProcessedTweet2);
        neelProcessedTweet1.setId(null);
        assertThat(neelProcessedTweet1).isNotEqualTo(neelProcessedTweet2);
    }
}
