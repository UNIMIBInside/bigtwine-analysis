package it.unimib.disco.bigtwine.services.analysis.web.api;

import it.unimib.disco.bigtwine.commons.models.TwitterStatus;
import it.unimib.disco.bigtwine.commons.models.TwitterUser;
import it.unimib.disco.bigtwine.commons.models.dto.TwitterStatusDTO;
import it.unimib.disco.bigtwine.commons.models.dto.TwitterUserDTO;
import it.unimib.disco.bigtwine.services.analysis.AnalysisApp;
import it.unimib.disco.bigtwine.services.analysis.SpringSecurityWebAuxTestConfig;
import it.unimib.disco.bigtwine.services.analysis.WithMockCustomUser;
import it.unimib.disco.bigtwine.services.analysis.WithMockCustomUserSecurityContextFactory;
import it.unimib.disco.bigtwine.services.analysis.domain.*;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisResultMapperLocator;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisRepository;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisResultsRepository;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    AnalysisApp.class,
    SpringSecurityWebAuxTestConfig.class,
    WithMockCustomUserSecurityContextFactory.class
})
public class AnalysisResultsApiIntTest {

    @Autowired
    private AnalysisResultMapperLocator resultMapperLocator;

    @Autowired
    private AnalysisResultsRepository resultsRepository;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private AnalysisRepository analysisRepository;

    private MockMvc restApiMvc;


    private Analysis createAnalysis() {
        AnalysisInput input = new QueryAnalysisInput()
            .tokens(Arrays.asList("query", "di", "prova"))
            .joinOperator(QueryAnalysisInput.JoinOperator.AND);

        return new Analysis()
            .type(AnalysisType.TWITTER_NEEL)
            .createDate(Instant.now())
            .updateDate(Instant.now())
            .visibility(AnalysisVisibility.PUBLIC)
            .status(AnalysisStatus.READY)
            .owner("testuser-1")
            .input(input);
    }

    private NeelProcessedTweet createProcessedTweet() {
        TwitterUser user = new TwitterUserDTO();
        user.setId("1");
        user.setScreenName("user1");
        TwitterStatus status = new TwitterStatusDTO();
        status.setId("1");
        status.setText("text");
        status.setUser(user);

        NeelProcessedTweet tweet = new NeelProcessedTweet();
        tweet.setStatus(status);
        tweet.setEntities(Collections.emptyList());
        
        return tweet;


    }

    private AnalysisResult<?> createAnalysisResult() {
        NeelProcessedTweet tweet = this.createProcessedTweet();
        return new AnalysisResult<>()
            .payload(tweet)
            .processDate(Instant.now());
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AnalysisResultsApiDelegateImpl delegate = new AnalysisResultsApiDelegateImpl(null, analysisService, resultsRepository, resultMapperLocator);
        AnalysisResultsApiController controller = new AnalysisResultsApiController(delegate);
        this.restApiMvc = MockMvcBuilders.standaloneSetup(controller)
            .build();
    }

    @Test
    @WithMockCustomUser(userId = "testuser-1")
    public void testListAnalysisResultEmpty() throws Exception {
        Analysis analysis = this.createAnalysis()
            .visibility(AnalysisVisibility.PUBLIC)
            .owner("testuser-1");
        analysis = this.analysisRepository.save(analysis);

        this.restApiMvc.perform(get("/api/public/analysis-results/{id}", analysis.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.objects.length()").value(0));
    }

    @Test
    @WithMockCustomUser(userId = "testuser-1")
    public void testListAnalysisResultNotFound() throws Exception {
        this.restApiMvc.perform(get("/api/public/analysis-results/{id}", 1))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(userId = "testuser-1")
    public void testListAnalysisResultUnauthorized() throws Exception {
        Analysis analysis = this.createAnalysis()
            .visibility(AnalysisVisibility.PRIVATE)
            .owner("testuser-2");
        analysis = this.analysisRepository.save(analysis);

        this.restApiMvc.perform(get("/api/public/analysis-results/{id}", analysis.getId()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(userId = "testuser-1")
    public void testListAnalysisResult() throws Exception {
        Analysis analysis = this.createAnalysis()
            .visibility(AnalysisVisibility.PUBLIC)
            .owner("testuser-1");
        analysis = this.analysisRepository.save(analysis);

        int tweetCount = 3;
        for (int i = 0; i < tweetCount; ++i) {
            AnalysisResult<?> result = this.createAnalysisResult()
                .analysis(analysis)
                .saveDate(Instant.now());

            this.resultsRepository.save(result);
        }

        this.restApiMvc.perform(get("/api/public/analysis-results/{id}", analysis.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.objects.length()").value(3));
    }
}
