package it.unimib.disco.bigtwine.services.analysis.web.rest;

import it.unimib.disco.bigtwine.services.analysis.AnalysisApp;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisRepository;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
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

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
/**
 * Test class for the AnalysisResource REST controller.
 *
 * @see AnalysisResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnalysisApp.class)
public class AnalysisResourceIntTest {

    private static final AnalysisType DEFAULT_TYPE = AnalysisType.TWITTER_NEEL;
    private static final AnalysisType UPDATED_TYPE = AnalysisType.TWITTER_NEEL;

    private static final AnalysisInputType DEFAULT_INPUT_TYPE = AnalysisInputType.QUERY;
    private static final AnalysisInputType UPDATED_INPUT_TYPE = AnalysisInputType.DOCUMENT;

    private static final AnalysisStatus DEFAULT_STATUS = AnalysisStatus.READY;
    private static final AnalysisStatus UPDATED_STATUS = AnalysisStatus.STARTED;

    private static final AnalysisVisibility DEFAULT_VISIBILITY = AnalysisVisibility.PRIVATE;
    private static final AnalysisVisibility UPDATED_VISIBILITY = AnalysisVisibility.PUBLIC;

    private static final String DEFAULT_OWNER_ID = "AAAAAAAAAA";
    private static final String UPDATED_OWNER_ID = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_QUERY = "AAAAAAAAAA";
    private static final String UPDATED_QUERY = "BBBBBBBBBB";

    private static final String DEFAULT_DOCUMENT_ID = "AAAAAAAAAA";
    private static final String UPDATED_DOCUMENT_ID = "BBBBBBBBBB";

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restAnalysisMockMvc;

    private Analysis analysis;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AnalysisResource analysisResource = new AnalysisResource(analysisService);
        this.restAnalysisMockMvc = MockMvcBuilders.standaloneSetup(analysisResource)
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
    public static Analysis createEntity() {
        Analysis analysis = new Analysis()
            .type(DEFAULT_TYPE)
            .inputType(DEFAULT_INPUT_TYPE)
            .status(DEFAULT_STATUS)
            .visibility(DEFAULT_VISIBILITY)
            .owner(DEFAULT_OWNER_ID)
            .createDate(DEFAULT_CREATE_DATE)
            .updateDate(DEFAULT_UPDATE_DATE)
            .query(DEFAULT_QUERY)
            .documentId(DEFAULT_DOCUMENT_ID);
        return analysis;
    }

    @Before
    public void initTest() {
        analysisRepository.deleteAll();
        analysis = createEntity();
    }

    @Test
    public void createAnalysis() throws Exception {
        int databaseSizeBeforeCreate = analysisRepository.findAll().size();

        // Create the Analysis
        restAnalysisMockMvc.perform(post("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isCreated());

        // Validate the Analysis in the database
        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeCreate + 1);
        Analysis testAnalysis = analysisList.get(analysisList.size() - 1);
        assertThat(testAnalysis.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAnalysis.getInputType()).isEqualTo(DEFAULT_INPUT_TYPE);
        assertThat(testAnalysis.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testAnalysis.getVisibility()).isEqualTo(DEFAULT_VISIBILITY);
        assertThat(testAnalysis.getOwner()).isEqualTo(DEFAULT_OWNER_ID);
        assertThat(testAnalysis.getCreateDate()).isEqualTo(DEFAULT_CREATE_DATE);
        assertThat(testAnalysis.getUpdateDate()).isEqualTo(DEFAULT_UPDATE_DATE);
        assertThat(testAnalysis.getQuery()).isEqualTo(DEFAULT_QUERY);
        assertThat(testAnalysis.getDocumentId()).isEqualTo(DEFAULT_DOCUMENT_ID);
    }

    @Test
    public void createAnalysisWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = analysisRepository.findAll().size();

        // Create the Analysis with an existing ID
        analysis.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restAnalysisMockMvc.perform(post("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());

        // Validate the Analysis in the database
        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisRepository.findAll().size();
        // set the field null
        analysis.setType(null);

        // Create the Analysis, which fails.

        restAnalysisMockMvc.perform(post("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());

        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkInputTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisRepository.findAll().size();
        // set the field null
        analysis.setInputType(null);

        // Create the Analysis, which fails.

        restAnalysisMockMvc.perform(post("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());

        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisRepository.findAll().size();
        // set the field null
        analysis.setStatus(null);

        // Create the Analysis, which fails.

        restAnalysisMockMvc.perform(post("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());

        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkVisibilityIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisRepository.findAll().size();
        // set the field null
        analysis.setVisibility(null);

        // Create the Analysis, which fails.

        restAnalysisMockMvc.perform(post("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());

        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkOwnerIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisRepository.findAll().size();
        // set the field null
        analysis.setOwner(null);

        // Create the Analysis, which fails.

        restAnalysisMockMvc.perform(post("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());

        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkCreateDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisRepository.findAll().size();
        // set the field null
        analysis.setCreateDate(null);

        // Create the Analysis, which fails.

        restAnalysisMockMvc.perform(post("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());

        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkUpdateDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisRepository.findAll().size();
        // set the field null
        analysis.setUpdateDate(null);

        // Create the Analysis, which fails.

        restAnalysisMockMvc.perform(post("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());

        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllAnalyses() throws Exception {
        // Initialize the database
        analysisRepository.save(analysis);

        // Get all the analysisList
        restAnalysisMockMvc.perform(get("/api/analyses?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(analysis.getId())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].inputType").value(hasItem(DEFAULT_INPUT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].visibility").value(hasItem(DEFAULT_VISIBILITY.toString())))
            .andExpect(jsonPath("$.[*].owner").value(hasItem(DEFAULT_OWNER_ID.toString())))
            .andExpect(jsonPath("$.[*].createDate").value(hasItem(DEFAULT_CREATE_DATE.toString())))
            .andExpect(jsonPath("$.[*].updateDate").value(hasItem(DEFAULT_UPDATE_DATE.toString())))
            .andExpect(jsonPath("$.[*].query").value(hasItem(DEFAULT_QUERY.toString())))
            .andExpect(jsonPath("$.[*].documentId").value(hasItem(DEFAULT_DOCUMENT_ID.toString())));
    }
    
    @Test
    public void getAnalysis() throws Exception {
        // Initialize the database
        analysisRepository.save(analysis);

        // Get the analysis
        restAnalysisMockMvc.perform(get("/api/analyses/{id}", analysis.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(analysis.getId()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.inputType").value(DEFAULT_INPUT_TYPE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.visibility").value(DEFAULT_VISIBILITY.toString()))
            .andExpect(jsonPath("$.owner").value(DEFAULT_OWNER_ID.toString()))
            .andExpect(jsonPath("$.createDate").value(DEFAULT_CREATE_DATE.toString()))
            .andExpect(jsonPath("$.updateDate").value(DEFAULT_UPDATE_DATE.toString()))
            .andExpect(jsonPath("$.query").value(DEFAULT_QUERY.toString()))
            .andExpect(jsonPath("$.documentId").value(DEFAULT_DOCUMENT_ID.toString()));
    }

    @Test
    public void getNonExistingAnalysis() throws Exception {
        // Get the analysis
        restAnalysisMockMvc.perform(get("/api/analyses/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateAnalysis() throws Exception {
        // Initialize the database
        analysisService.save(analysis);

        int databaseSizeBeforeUpdate = analysisRepository.findAll().size();

        // Update the analysis
        Analysis updatedAnalysis = analysisRepository.findById(analysis.getId()).get();
        updatedAnalysis
            .type(UPDATED_TYPE)
            .inputType(UPDATED_INPUT_TYPE)
            .status(UPDATED_STATUS)
            .visibility(UPDATED_VISIBILITY)
            .owner(UPDATED_OWNER_ID)
            .createDate(UPDATED_CREATE_DATE)
            .updateDate(UPDATED_UPDATE_DATE)
            .query(UPDATED_QUERY)
            .documentId(UPDATED_DOCUMENT_ID);

        restAnalysisMockMvc.perform(put("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAnalysis)))
            .andExpect(status().isOk());

        // Validate the Analysis in the database
        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeUpdate);
        Analysis testAnalysis = analysisList.get(analysisList.size() - 1);
        assertThat(testAnalysis.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAnalysis.getInputType()).isEqualTo(UPDATED_INPUT_TYPE);
        assertThat(testAnalysis.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testAnalysis.getVisibility()).isEqualTo(UPDATED_VISIBILITY);
        assertThat(testAnalysis.getOwner()).isEqualTo(UPDATED_OWNER_ID);
        assertThat(testAnalysis.getCreateDate()).isEqualTo(UPDATED_CREATE_DATE);
        assertThat(testAnalysis.getUpdateDate()).isEqualTo(UPDATED_UPDATE_DATE);
        assertThat(testAnalysis.getQuery()).isEqualTo(UPDATED_QUERY);
        assertThat(testAnalysis.getDocumentId()).isEqualTo(UPDATED_DOCUMENT_ID);
    }

    @Test
    public void updateNonExistingAnalysis() throws Exception {
        int databaseSizeBeforeUpdate = analysisRepository.findAll().size();

        // Create the Analysis

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnalysisMockMvc.perform(put("/api/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());

        // Validate the Analysis in the database
        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteAnalysis() throws Exception {
        // Initialize the database
        analysisService.save(analysis);

        int databaseSizeBeforeDelete = analysisRepository.findAll().size();

        // Get the analysis
        restAnalysisMockMvc.perform(delete("/api/analyses/{id}", analysis.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Analysis.class);
        Analysis analysis1 = new Analysis();
        analysis1.setId("id1");
        Analysis analysis2 = new Analysis();
        analysis2.setId(analysis1.getId());
        assertThat(analysis1).isEqualTo(analysis2);
        analysis2.setId("id2");
        assertThat(analysis1).isNotEqualTo(analysis2);
        analysis1.setId(null);
        assertThat(analysis1).isNotEqualTo(analysis2);
    }
}
