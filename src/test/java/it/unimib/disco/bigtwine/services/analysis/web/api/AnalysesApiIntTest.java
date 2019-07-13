package it.unimib.disco.bigtwine.services.analysis.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unimib.disco.bigtwine.commons.messaging.AnalysisStatusChangeRequestedEvent;
import it.unimib.disco.bigtwine.services.analysis.AnalysisApp;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisStatusHistory;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import it.unimib.disco.bigtwine.services.analysis.messaging.AnalysisStatusChangeRequestProducerChannel;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisRepository;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisStatusHistoryRepository;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisStatusEnum;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisUpdatableDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisVisibilityEnum;
import it.unimib.disco.bigtwine.services.analysis.web.rest.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnalysisApp.class)
public class AnalysesApiIntTest {

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private AnalysisStatusHistoryRepository analysisStatusHistoryRepository;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private MessageCollector messageCollector;

    @Autowired
    private AnalysisStatusChangeRequestProducerChannel statusChangeRequestsChannel;

    private MockMvc restApiMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AnalysesApiDelegate delegate = new AnalysesApiDelegateImpl(null, analysisService);
        AnalysesApiController controller = new AnalysesApiController(delegate);
        this.restApiMvc = MockMvcBuilders.standaloneSetup(controller)
            .build();
    }

    @Before
    public void cleanMessageQueue() {
        messageCollector
            .forChannel(statusChangeRequestsChannel.analysisStatusChangeRequestsChannel())
            .clear();
    }

    private Analysis createAnalysis() {
        return new Analysis()
            .type(AnalysisType.TWITTER_NEEL)
            .inputType(AnalysisInputType.QUERY)
            .createDate(Instant.now())
            .updateDate(Instant.now())
            .visibility(AnalysisVisibility.PUBLIC)
            .status(AnalysisStatus.READY)
            .owner("testuser-1")
            .query("prova");
    }

    private AnalysisDTO createAnalysisDTO() {
        return new AnalysisDTO()
            .type(AnalysisDTO.TypeEnum.TWITTER_NEEL)
            .inputType(AnalysisDTO.InputTypeEnum.QUERY)
            .query("prova");
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testCreateAnalysis() throws Exception {
        AnalysisDTO analysis = this.createAnalysisDTO();

        int countBeforeCreate = this.analysisRepository.findAll().size();

        this.restApiMvc.perform(post("/api/public/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));

        List<Analysis> analysisList = analysisRepository.findAll();
        assertThat(analysisList).hasSize(countBeforeCreate + 1);

        Analysis testAnalysis = analysisList.get(analysisList.size() - 1);
        assertThat(testAnalysis.getOwner()).isEqualTo("testuser-1");
        assertThat(testAnalysis.getQuery()).isEqualTo("prova");
        assertThat(testAnalysis.getType()).isEqualTo(AnalysisType.TWITTER_NEEL);
        assertThat(testAnalysis.getInputType()).isEqualTo(AnalysisInputType.QUERY);
        assertThat(testAnalysis.getStatus()).isEqualTo(Analysis.DEFAULT_STATUS);
        assertThat(testAnalysis.getVisibility()).isEqualTo(Analysis.DEFAULT_VISIBILITY);
        assertThat(testAnalysis.getCreateDate()).isNotNull();
        assertThat(testAnalysis.getUpdateDate()).isNotNull();
    }

    @Test
    public void testCreateAnalysisUnauthenticated() throws Exception {
        AnalysisDTO analysis = this.createAnalysisDTO();

        this.restApiMvc.perform(post("/api/public/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testCreateAnalysisInvalidInputDoc() throws Exception {
        AnalysisDTO analysis = this.createAnalysisDTO()
            .inputType(AnalysisDTO.InputTypeEnum.DOCUMENT)
            .query("prova")
            .documentId(null);

        this.restApiMvc.perform(post("/api/public/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testCreateAnalysisInvalidInputQuery() throws Exception {
        AnalysisDTO analysis = this.createAnalysisDTO()
            .inputType(AnalysisDTO.InputTypeEnum.QUERY)
            .query(null)
            .documentId("1");

        this.restApiMvc.perform(post("/api/public/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testCreateAnalysisInvalidInputBoth() throws Exception {
        AnalysisDTO analysis = this.createAnalysisDTO()
            .inputType(AnalysisDTO.InputTypeEnum.QUERY)
            .query(null)
            .documentId(null);

        this.restApiMvc.perform(post("/api/public/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testGetPublicAnalysis() throws Exception {
        Analysis analysis = this.createAnalysis()
            .visibility(AnalysisVisibility.PUBLIC)
            .owner("testuser-2");
        analysis = this.analysisRepository.save(analysis);

        this.restApiMvc.perform(get("/api/public/analyses/{id}", analysis.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(analysis.getId()));
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testGetOwnedPrivateAnalysis() throws Exception {
        Analysis analysis = this.createAnalysis()
            .visibility(AnalysisVisibility.PRIVATE)
            .owner("testuser-1");
        analysis = this.analysisRepository.save(analysis);

        this.restApiMvc.perform(get("/api/public/analyses/{id}", analysis.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(analysis.getId()));
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testGetUnonwnedPrivateAnalysis() throws Exception {
        Analysis analysis = this.createAnalysis()
            .visibility(AnalysisVisibility.PRIVATE)
            .owner("testuser-2");
        analysis = this.analysisRepository.save(analysis);

        this.restApiMvc.perform(get("/api/public/analyses/{id}", analysis.getId()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testGetAnalyses() throws Exception {
        this.analysisRepository.deleteAll();
        Analysis a1 = this.createAnalysis()
            .visibility(AnalysisVisibility.PRIVATE)
            .owner("testuser-1");
        Analysis a2 = this.createAnalysis()
            .visibility(AnalysisVisibility.PRIVATE)
            .owner("testuser-2");
        Analysis a3 = this.createAnalysis()
            .visibility(AnalysisVisibility.PRIVATE)
            .owner("testuser-1");
        a1 = this.analysisRepository.save(a1);
        this.analysisRepository.save(a2);
        a3 = this.analysisRepository.save(a3);

        this.restApiMvc.perform(get("/api/public/analyses"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(a1.getId()))
            .andExpect(jsonPath("$[1].id").value(a3.getId()));
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testDeleteOwnedAnalysis() throws Exception {
        Analysis analysis = this.createAnalysis()
            .owner("testuser-1");
        analysis = this.analysisRepository.save(analysis);

        this.restApiMvc.perform(delete("/api/public/analyses/{id}", analysis.getId()))
            .andExpect(status().isOk());

        AnalysisStatus statusBeforeDelete = analysis.getStatus();
        int countBeforeDelete = this.analysisRepository.findAll().size();

        List<Analysis> analysisList = this.analysisRepository.findAll();

        assertThat(analysisList).hasSize(countBeforeDelete);

        Analysis testAnalysis = analysisList.get(countBeforeDelete - 1);

        assertThat(testAnalysis.getStatus()).isEqualTo(statusBeforeDelete);
        Message<?> received = messageCollector
            .forChannel(statusChangeRequestsChannel.analysisStatusChangeRequestsChannel())
            .poll();
        assertNotNull(received);
        AnalysisStatusChangeRequestedEvent event = new ObjectMapper().readValue((String)received.getPayload(),
            AnalysisStatusChangeRequestedEvent.class);

        assertThat(event.getAnalysisId()).isEqualTo(analysis.getId());
        assertThat(event.isUserRequested()).isEqualTo(true);
        assertThat(event.getDesiredStatus())
            .isEqualTo(it.unimib.disco.bigtwine.commons.models.AnalysisStatusEnum.CANCELLED);
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testDeleteUnownedAnalysis() throws Exception {
        Analysis analysis = this.createAnalysis()
            .owner("testuser-2");
        analysis = this.analysisRepository.save(analysis);

        this.restApiMvc.perform(delete("/api/public/analyses/{id}", analysis.getId()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testUpdateStatusOwnedAnalysis() throws Exception {
        Analysis analysis = this.createAnalysis()
            .owner("testuser-1")
            .status(AnalysisStatus.READY);
        analysis = this.analysisRepository.save(analysis);

        AnalysisUpdatableDTO analysisUpdate = new AnalysisUpdatableDTO()
            .status(AnalysisStatusEnum.STARTED);
        AnalysisStatus statusBeforeUpdate = analysis.getStatus();

        this.restApiMvc.perform(patch("/api/public/analyses/{id}", analysis.getId())
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisUpdate)))
            .andExpect(status().isOk());

        Analysis testAnalysis = this.analysisRepository.findById(analysis.getId()).orElse(null);

        assertThat(testAnalysis).isNotNull();
        assertThat(testAnalysis.getStatus()).isEqualTo(statusBeforeUpdate);
        Message<?> received = messageCollector
            .forChannel(statusChangeRequestsChannel.analysisStatusChangeRequestsChannel())
            .poll();
        assertNotNull(received);
        AnalysisStatusChangeRequestedEvent event = new ObjectMapper().readValue((String)received.getPayload(),
            AnalysisStatusChangeRequestedEvent.class);

        assertThat(event.getAnalysisId()).isEqualTo(analysis.getId());
        assertThat(event.isUserRequested()).isEqualTo(true);
        assertThat(event.getDesiredStatus())
            .isEqualTo(it.unimib.disco.bigtwine.commons.models.AnalysisStatusEnum.STARTED);
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testUpdateVisibilityOwnedAnalysis() throws Exception {
        Analysis analysis = this.createAnalysis()
            .owner("testuser-1")
            .visibility(AnalysisVisibility.PRIVATE);
        analysis = this.analysisRepository.save(analysis);

        AnalysisUpdatableDTO analysisUpdate = new AnalysisUpdatableDTO()
            .visibility(AnalysisVisibilityEnum.PUBLIC);

        this.restApiMvc.perform(patch("/api/public/analyses/{id}", analysis.getId())
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisUpdate)))
            .andExpect(status().isOk());

        Analysis testAnalysis = this.analysisRepository.findById(analysis.getId()).orElse(null);

        assertThat(testAnalysis).isNotNull();

        assertThat(testAnalysis.getVisibility()).isEqualTo(AnalysisVisibility.PUBLIC);
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testUpdateUnownedAnalysis() throws Exception {
        Analysis analysis = this.createAnalysis()
            .owner("testuser-2")
            .visibility(AnalysisVisibility.PRIVATE);
        analysis = this.analysisRepository.save(analysis);

        AnalysisUpdatableDTO analysisUpdate = new AnalysisUpdatableDTO()
            .visibility(AnalysisVisibilityEnum.PUBLIC);

        this.restApiMvc.perform(patch("/api/public/analyses/{id}", analysis.getId())
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisUpdate)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testCreateStatusHistory() throws Exception {
        Analysis analysis = this.createAnalysis()
            .owner("testuser-1")
            .status(AnalysisStatus.READY);
        analysis = this.analysisRepository.save(analysis);

        AnalysisUpdatableDTO analysisUpdate = new AnalysisUpdatableDTO()
            .status(AnalysisStatusEnum.STARTED);

        this.restApiMvc.perform(patch("/api/public/analyses/{id}", analysis.getId())
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysisUpdate)))
            .andExpect(status().isOk());

        Analysis testAnalysis = this.analysisRepository.findById(analysis.getId()).orElse(null);
        assertThat(testAnalysis).isNotNull();

        List<AnalysisStatusHistory> statusHistory = this.analysisStatusHistoryRepository
            .findByAnalysisId(testAnalysis.getId());

        assertThat(statusHistory).hasSize(0);
    }
}
