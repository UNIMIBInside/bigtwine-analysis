package it.unimib.disco.bigtwine.services.analysis.web.api;

import it.unimib.disco.bigtwine.services.analysis.AnalysisApp;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisRepository;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisStatusEnum;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisVisibilityEnum;
import it.unimib.disco.bigtwine.services.analysis.web.rest.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnalysisApp.class)
public class AnalysesApiIntTest {

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private AnalysisRepository analysisRepository;

    private MockMvc restApiMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AnalysesApiDelegate delegate = new AnalysesApiDelegateImpl(null, analysisService);
        AnalysesApiController controller = new AnalysesApiController(delegate);
        this.restApiMvc = MockMvcBuilders.standaloneSetup(controller)
            .build();
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void testCreateAnalysis() throws Exception {
        AnalysisDTO analysis = new AnalysisDTO()
            .type(AnalysisDTO.TypeEnum.TWITTER_NEEL)
            .inputType(AnalysisDTO.InputTypeEnum.QUERY)
            .query("prova");

        int countBeforeCreate = this.analysisRepository.findAll().size();

        this.restApiMvc.perform(post("/api/public/analyses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(analysis)))
            .andExpect(status().isCreated());

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
}
