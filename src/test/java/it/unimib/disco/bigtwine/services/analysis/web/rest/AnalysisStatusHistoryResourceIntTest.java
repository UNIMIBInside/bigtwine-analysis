package it.unimib.disco.bigtwine.services.analysis.web.rest;

import it.unimib.disco.bigtwine.services.analysis.AnalysisApp;

import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisStatusHistory;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisStatusHistoryRepository;
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

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisErrorCode;
/**
 * Test class for the AnalysisStatusHistoryResource REST controller.
 *
 * @see AnalysisStatusHistoryResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnalysisApp.class)
public class AnalysisStatusHistoryResourceIntTest {

    private static final AnalysisStatus DEFAULT_NEW_STATUS = AnalysisStatus.READY;
    private static final AnalysisStatus UPDATED_NEW_STATUS = AnalysisStatus.STARTED;

    private static final AnalysisStatus DEFAULT_OLD_STATUS = AnalysisStatus.READY;
    private static final AnalysisStatus UPDATED_OLD_STATUS = AnalysisStatus.STARTED;

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final AnalysisErrorCode DEFAULT_ERROR_CODE = AnalysisErrorCode.GENERIC;
    private static final AnalysisErrorCode UPDATED_ERROR_CODE = AnalysisErrorCode.GENERIC;

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private AnalysisStatusHistoryRepository analysisStatusHistoryRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restAnalysisStatusHistoryMockMvc;

    private AnalysisStatusHistory analysisStatusHistory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AnalysisStatusHistoryResource analysisStatusHistoryResource = new AnalysisStatusHistoryResource(analysisStatusHistoryRepository);
        this.restAnalysisStatusHistoryMockMvc = MockMvcBuilders.standaloneSetup(analysisStatusHistoryResource)
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
    public static AnalysisStatusHistory createEntity() {
        AnalysisStatusHistory analysisStatusHistory = new AnalysisStatusHistory()
            .newStatus(DEFAULT_NEW_STATUS)
            .oldStatus(DEFAULT_OLD_STATUS)
            .userId(DEFAULT_USER_ID)
            .errorCode(DEFAULT_ERROR_CODE)
            .message(DEFAULT_MESSAGE)
            .date(DEFAULT_DATE);
        return analysisStatusHistory;
    }

    @Before
    public void initTest() {
        analysisStatusHistoryRepository.deleteAll();
        analysisStatusHistory = createEntity();
    }

    @Test
    public void createAnalysisStatusHistory() throws Exception {
        int databaseSizeBeforeCreate = analysisStatusHistoryRepository.findAll().size();

        // Create the AnalysisStatusHistory
        restAnalysisStatusHistoryMockMvc.perform(post("/api/analysis-status-histories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisStatusHistory)))
            .andExpect(status().isCreated());

        // Validate the AnalysisStatusHistory in the database
        List<AnalysisStatusHistory> analysisStatusHistoryList = analysisStatusHistoryRepository.findAll();
        assertThat(analysisStatusHistoryList).hasSize(databaseSizeBeforeCreate + 1);
        AnalysisStatusHistory testAnalysisStatusHistory = analysisStatusHistoryList.get(analysisStatusHistoryList.size() - 1);
        assertThat(testAnalysisStatusHistory.getNewStatus()).isEqualTo(DEFAULT_NEW_STATUS);
        assertThat(testAnalysisStatusHistory.getOldStatus()).isEqualTo(DEFAULT_OLD_STATUS);
        assertThat(testAnalysisStatusHistory.getUserId()).isEqualTo(DEFAULT_USER_ID);
        assertThat(testAnalysisStatusHistory.getErrorCode()).isEqualTo(DEFAULT_ERROR_CODE);
        assertThat(testAnalysisStatusHistory.getMessage()).isEqualTo(DEFAULT_MESSAGE);
        assertThat(testAnalysisStatusHistory.getDate()).isEqualTo(DEFAULT_DATE);
    }

    @Test
    public void createAnalysisStatusHistoryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = analysisStatusHistoryRepository.findAll().size();

        // Create the AnalysisStatusHistory with an existing ID
        analysisStatusHistory.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restAnalysisStatusHistoryMockMvc.perform(post("/api/analysis-status-histories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisStatusHistory)))
            .andExpect(status().isBadRequest());

        // Validate the AnalysisStatusHistory in the database
        List<AnalysisStatusHistory> analysisStatusHistoryList = analysisStatusHistoryRepository.findAll();
        assertThat(analysisStatusHistoryList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void checkNewStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisStatusHistoryRepository.findAll().size();
        // set the field null
        analysisStatusHistory.setNewStatus(null);

        // Create the AnalysisStatusHistory, which fails.

        restAnalysisStatusHistoryMockMvc.perform(post("/api/analysis-status-histories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisStatusHistory)))
            .andExpect(status().isBadRequest());

        List<AnalysisStatusHistory> analysisStatusHistoryList = analysisStatusHistoryRepository.findAll();
        assertThat(analysisStatusHistoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisStatusHistoryRepository.findAll().size();
        // set the field null
        analysisStatusHistory.setDate(null);

        // Create the AnalysisStatusHistory, which fails.

        restAnalysisStatusHistoryMockMvc.perform(post("/api/analysis-status-histories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisStatusHistory)))
            .andExpect(status().isBadRequest());

        List<AnalysisStatusHistory> analysisStatusHistoryList = analysisStatusHistoryRepository.findAll();
        assertThat(analysisStatusHistoryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllAnalysisStatusHistories() throws Exception {
        // Initialize the database
        analysisStatusHistoryRepository.save(analysisStatusHistory);

        // Get all the analysisStatusHistoryList
        restAnalysisStatusHistoryMockMvc.perform(get("/api/analysis-status-histories?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(analysisStatusHistory.getId())))
            .andExpect(jsonPath("$.[*].newStatus").value(hasItem(DEFAULT_NEW_STATUS.toString())))
            .andExpect(jsonPath("$.[*].oldStatus").value(hasItem(DEFAULT_OLD_STATUS.toString())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.toString())))
            .andExpect(jsonPath("$.[*].errorCode").value(hasItem(DEFAULT_ERROR_CODE.toString())))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE.toString())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())));
    }
    
    @Test
    public void getAnalysisStatusHistory() throws Exception {
        // Initialize the database
        analysisStatusHistoryRepository.save(analysisStatusHistory);

        // Get the analysisStatusHistory
        restAnalysisStatusHistoryMockMvc.perform(get("/api/analysis-status-histories/{id}", analysisStatusHistory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(analysisStatusHistory.getId()))
            .andExpect(jsonPath("$.newStatus").value(DEFAULT_NEW_STATUS.toString()))
            .andExpect(jsonPath("$.oldStatus").value(DEFAULT_OLD_STATUS.toString()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID.toString()))
            .andExpect(jsonPath("$.errorCode").value(DEFAULT_ERROR_CODE.toString()))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE.toString()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()));
    }

    @Test
    public void getNonExistingAnalysisStatusHistory() throws Exception {
        // Get the analysisStatusHistory
        restAnalysisStatusHistoryMockMvc.perform(get("/api/analysis-status-histories/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateAnalysisStatusHistory() throws Exception {
        // Initialize the database
        analysisStatusHistoryRepository.save(analysisStatusHistory);

        int databaseSizeBeforeUpdate = analysisStatusHistoryRepository.findAll().size();

        // Update the analysisStatusHistory
        AnalysisStatusHistory updatedAnalysisStatusHistory = analysisStatusHistoryRepository.findById(analysisStatusHistory.getId()).get();
        updatedAnalysisStatusHistory
            .newStatus(UPDATED_NEW_STATUS)
            .oldStatus(UPDATED_OLD_STATUS)
            .userId(UPDATED_USER_ID)
            .errorCode(UPDATED_ERROR_CODE)
            .message(UPDATED_MESSAGE)
            .date(UPDATED_DATE);

        restAnalysisStatusHistoryMockMvc.perform(put("/api/analysis-status-histories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAnalysisStatusHistory)))
            .andExpect(status().isOk());

        // Validate the AnalysisStatusHistory in the database
        List<AnalysisStatusHistory> analysisStatusHistoryList = analysisStatusHistoryRepository.findAll();
        assertThat(analysisStatusHistoryList).hasSize(databaseSizeBeforeUpdate);
        AnalysisStatusHistory testAnalysisStatusHistory = analysisStatusHistoryList.get(analysisStatusHistoryList.size() - 1);
        assertThat(testAnalysisStatusHistory.getNewStatus()).isEqualTo(UPDATED_NEW_STATUS);
        assertThat(testAnalysisStatusHistory.getOldStatus()).isEqualTo(UPDATED_OLD_STATUS);
        assertThat(testAnalysisStatusHistory.getUserId()).isEqualTo(UPDATED_USER_ID);
        assertThat(testAnalysisStatusHistory.getErrorCode()).isEqualTo(UPDATED_ERROR_CODE);
        assertThat(testAnalysisStatusHistory.getMessage()).isEqualTo(UPDATED_MESSAGE);
        assertThat(testAnalysisStatusHistory.getDate()).isEqualTo(UPDATED_DATE);
    }

    @Test
    public void updateNonExistingAnalysisStatusHistory() throws Exception {
        int databaseSizeBeforeUpdate = analysisStatusHistoryRepository.findAll().size();

        // Create the AnalysisStatusHistory

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnalysisStatusHistoryMockMvc.perform(put("/api/analysis-status-histories")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisStatusHistory)))
            .andExpect(status().isBadRequest());

        // Validate the AnalysisStatusHistory in the database
        List<AnalysisStatusHistory> analysisStatusHistoryList = analysisStatusHistoryRepository.findAll();
        assertThat(analysisStatusHistoryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteAnalysisStatusHistory() throws Exception {
        // Initialize the database
        analysisStatusHistoryRepository.save(analysisStatusHistory);

        int databaseSizeBeforeDelete = analysisStatusHistoryRepository.findAll().size();

        // Get the analysisStatusHistory
        restAnalysisStatusHistoryMockMvc.perform(delete("/api/analysis-status-histories/{id}", analysisStatusHistory.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<AnalysisStatusHistory> analysisStatusHistoryList = analysisStatusHistoryRepository.findAll();
        assertThat(analysisStatusHistoryList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AnalysisStatusHistory.class);
        AnalysisStatusHistory analysisStatusHistory1 = new AnalysisStatusHistory();
        analysisStatusHistory1.setId("id1");
        AnalysisStatusHistory analysisStatusHistory2 = new AnalysisStatusHistory();
        analysisStatusHistory2.setId(analysisStatusHistory1.getId());
        assertThat(analysisStatusHistory1).isEqualTo(analysisStatusHistory2);
        analysisStatusHistory2.setId("id2");
        assertThat(analysisStatusHistory1).isNotEqualTo(analysisStatusHistory2);
        analysisStatusHistory1.setId(null);
        assertThat(analysisStatusHistory1).isNotEqualTo(analysisStatusHistory2);
    }
}
