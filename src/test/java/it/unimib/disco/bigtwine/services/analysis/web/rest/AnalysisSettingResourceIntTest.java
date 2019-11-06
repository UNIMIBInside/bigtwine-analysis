package it.unimib.disco.bigtwine.services.analysis.web.rest;

import it.unimib.disco.bigtwine.services.analysis.AnalysisApp;

import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisSetting;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisSettingRepository;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisSettingService;
import it.unimib.disco.bigtwine.services.analysis.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;


import static it.unimib.disco.bigtwine.services.analysis.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisSettingType;
/**
 * Test class for the AnalysisSettingResource REST controller.
 *
 * @see AnalysisSettingResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnalysisApp.class)
public class AnalysisSettingResourceIntTest {

    private static final String DEFAULT_NAME = "sp";
    private static final String UPDATED_NAME = "v";

    private static final AnalysisSettingType DEFAULT_TYPE = AnalysisSettingType.NUMBER;
    private static final AnalysisSettingType UPDATED_TYPE = AnalysisSettingType.STRING;

    private static final Boolean DEFAULT_USER_VISIBLE = false;
    private static final Boolean UPDATED_USER_VISIBLE = true;

    private static final Boolean DEFAULT_GLOBAL = false;
    private static final Boolean UPDATED_GLOBAL = true;

    private static final String DEFAULT_OPTIONS = "AAAAAAAAAA";
    private static final String UPDATED_OPTIONS = "BBBBBBBBBB";

    @Autowired
    private AnalysisSettingRepository analysisSettingRepository;

    @Mock
    private AnalysisSettingRepository analysisSettingRepositoryMock;

    @Mock
    private AnalysisSettingService analysisSettingServiceMock;

    @Autowired
    private AnalysisSettingService analysisSettingService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restAnalysisSettingMockMvc;

    private AnalysisSetting analysisSetting;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final AnalysisSettingResource analysisSettingResource = new AnalysisSettingResource(analysisSettingService);
        this.restAnalysisSettingMockMvc = MockMvcBuilders.standaloneSetup(analysisSettingResource)
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
    public static AnalysisSetting createEntity() {
        AnalysisSetting analysisSetting = new AnalysisSetting()
            .name(DEFAULT_NAME)
            .type(DEFAULT_TYPE)
            .userVisible(DEFAULT_USER_VISIBLE)
            .global(DEFAULT_GLOBAL)
            .options(DEFAULT_OPTIONS);
        // Add required entity
        AnalysisType analysisType = AnalysisType.TWITTER_NEEL;
        analysisSetting.setAnalysisType(analysisType);
        return analysisSetting;
    }

    @Before
    public void initTest() {
        analysisSettingRepository.deleteAll();
        analysisSetting = createEntity();
    }

    @Test
    public void createAnalysisSetting() throws Exception {
        int databaseSizeBeforeCreate = analysisSettingRepository.findAll().size();

        // Create the AnalysisSetting
        restAnalysisSettingMockMvc.perform(post("/api/analysis-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisSetting)))
            .andExpect(status().isCreated());

        // Validate the AnalysisSetting in the database
        List<AnalysisSetting> analysisSettingList = analysisSettingRepository.findAll();
        assertThat(analysisSettingList).hasSize(databaseSizeBeforeCreate + 1);
        AnalysisSetting testAnalysisSetting = analysisSettingList.get(analysisSettingList.size() - 1);
        assertThat(testAnalysisSetting.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAnalysisSetting.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testAnalysisSetting.isUserVisible()).isEqualTo(DEFAULT_USER_VISIBLE);
        assertThat(testAnalysisSetting.isGlobal()).isEqualTo(DEFAULT_GLOBAL);
        assertThat(testAnalysisSetting.getOptions()).isEqualTo(DEFAULT_OPTIONS);
    }

    @Test
    public void createAnalysisSettingWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = analysisSettingRepository.findAll().size();

        // Create the AnalysisSetting with an existing ID
        analysisSetting.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        restAnalysisSettingMockMvc.perform(post("/api/analysis-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisSetting)))
            .andExpect(status().isBadRequest());

        // Validate the AnalysisSetting in the database
        List<AnalysisSetting> analysisSettingList = analysisSettingRepository.findAll();
        assertThat(analysisSettingList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisSettingRepository.findAll().size();
        // set the field null
        analysisSetting.setName(null);

        // Create the AnalysisSetting, which fails.

        restAnalysisSettingMockMvc.perform(post("/api/analysis-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisSetting)))
            .andExpect(status().isBadRequest());

        List<AnalysisSetting> analysisSettingList = analysisSettingRepository.findAll();
        assertThat(analysisSettingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void checkTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = analysisSettingRepository.findAll().size();
        // set the field null
        analysisSetting.setType(null);

        // Create the AnalysisSetting, which fails.

        restAnalysisSettingMockMvc.perform(post("/api/analysis-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisSetting)))
            .andExpect(status().isBadRequest());

        List<AnalysisSetting> analysisSettingList = analysisSettingRepository.findAll();
        assertThat(analysisSettingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllAnalysisSettings() throws Exception {
        // Initialize the database
        analysisSettingRepository.save(analysisSetting);

        // Get all the analysisSettingList
        restAnalysisSettingMockMvc.perform(get("/api/analysis-settings?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(analysisSetting.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].userVisible").value(hasItem(DEFAULT_USER_VISIBLE.booleanValue())))
            .andExpect(jsonPath("$.[*].global").value(hasItem(DEFAULT_GLOBAL.booleanValue())))
            .andExpect(jsonPath("$.[*].options").value(hasItem(DEFAULT_OPTIONS.toString())));
    }

    @SuppressWarnings({"unchecked"})
    public void getAllAnalysisSettingsWithEagerRelationshipsIsEnabled() throws Exception {
        AnalysisSettingResource analysisSettingResource = new AnalysisSettingResource(analysisSettingServiceMock);
        when(analysisSettingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restAnalysisSettingMockMvc = MockMvcBuilders.standaloneSetup(analysisSettingResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();

        restAnalysisSettingMockMvc.perform(get("/api/analysis-settings?eagerload=true"))
        .andExpect(status().isOk());

        verify(analysisSettingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({"unchecked"})
    public void getAllAnalysisSettingsWithEagerRelationshipsIsNotEnabled() throws Exception {
        AnalysisSettingResource analysisSettingResource = new AnalysisSettingResource(analysisSettingServiceMock);
            when(analysisSettingServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
            MockMvc restAnalysisSettingMockMvc = MockMvcBuilders.standaloneSetup(analysisSettingResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();

        restAnalysisSettingMockMvc.perform(get("/api/analysis-settings?eagerload=true"))
        .andExpect(status().isOk());

            verify(analysisSettingServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    public void getAnalysisSetting() throws Exception {
        // Initialize the database
        analysisSettingRepository.save(analysisSetting);

        // Get the analysisSetting
        restAnalysisSettingMockMvc.perform(get("/api/analysis-settings/{id}", analysisSetting.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(analysisSetting.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.userVisible").value(DEFAULT_USER_VISIBLE.booleanValue()))
            .andExpect(jsonPath("$.global").value(DEFAULT_GLOBAL.booleanValue()))
            .andExpect(jsonPath("$.options").value(DEFAULT_OPTIONS.toString()));
    }

    @Test
    public void getNonExistingAnalysisSetting() throws Exception {
        // Get the analysisSetting
        restAnalysisSettingMockMvc.perform(get("/api/analysis-settings/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateAnalysisSetting() throws Exception {
        // Initialize the database
        analysisSettingService.save(analysisSetting);

        int databaseSizeBeforeUpdate = analysisSettingRepository.findAll().size();

        // Update the analysisSetting
        AnalysisSetting updatedAnalysisSetting = analysisSettingRepository.findById(analysisSetting.getId()).get();
        updatedAnalysisSetting
            .name(UPDATED_NAME)
            .type(UPDATED_TYPE)
            .userVisible(UPDATED_USER_VISIBLE)
            .global(UPDATED_GLOBAL)
            .options(UPDATED_OPTIONS);

        restAnalysisSettingMockMvc.perform(put("/api/analysis-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedAnalysisSetting)))
            .andExpect(status().isOk());

        // Validate the AnalysisSetting in the database
        List<AnalysisSetting> analysisSettingList = analysisSettingRepository.findAll();
        assertThat(analysisSettingList).hasSize(databaseSizeBeforeUpdate);
        AnalysisSetting testAnalysisSetting = analysisSettingList.get(analysisSettingList.size() - 1);
        assertThat(testAnalysisSetting.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAnalysisSetting.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testAnalysisSetting.isUserVisible()).isEqualTo(UPDATED_USER_VISIBLE);
        assertThat(testAnalysisSetting.isGlobal()).isEqualTo(UPDATED_GLOBAL);
        assertThat(testAnalysisSetting.getOptions()).isEqualTo(UPDATED_OPTIONS);
    }

    @Test
    public void updateNonExistingAnalysisSetting() throws Exception {
        int databaseSizeBeforeUpdate = analysisSettingRepository.findAll().size();

        // Create the AnalysisSetting

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAnalysisSettingMockMvc.perform(put("/api/analysis-settings")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisSetting)))
            .andExpect(status().isBadRequest());

        // Validate the AnalysisSetting in the database
        List<AnalysisSetting> analysisSettingList = analysisSettingRepository.findAll();
        assertThat(analysisSettingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    public void deleteAnalysisSetting() throws Exception {
        // Initialize the database
        analysisSettingService.save(analysisSetting);

        int databaseSizeBeforeDelete = analysisSettingRepository.findAll().size();

        // Get the analysisSetting
        restAnalysisSettingMockMvc.perform(delete("/api/analysis-settings/{id}", analysisSetting.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<AnalysisSetting> analysisSettingList = analysisSettingRepository.findAll();
        assertThat(analysisSettingList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AnalysisSetting.class);
        AnalysisSetting analysisSetting1 = new AnalysisSetting();
        analysisSetting1.setId("id1");
        AnalysisSetting analysisSetting2 = new AnalysisSetting();
        analysisSetting2.setId(analysisSetting1.getId());
        assertThat(analysisSetting1).isEqualTo(analysisSetting2);
        analysisSetting2.setId("id2");
        assertThat(analysisSetting1).isNotEqualTo(analysisSetting2);
        analysisSetting1.setId(null);
        assertThat(analysisSetting1).isNotEqualTo(analysisSetting2);
    }
}
