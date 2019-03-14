package it.unimib.disco.bigtwine.services.analysis.service;

import it.unimib.disco.bigtwine.commons.messaging.NeelTweetProcessedEvent;
import it.unimib.disco.bigtwine.commons.models.LinkedEntity;
import it.unimib.disco.bigtwine.commons.models.TwitterStatus;
import it.unimib.disco.bigtwine.commons.models.TwitterUser;
import it.unimib.disco.bigtwine.commons.models.dto.TwitterStatusDTO;
import it.unimib.disco.bigtwine.commons.models.dto.TwitterUserDTO;
import it.unimib.disco.bigtwine.services.analysis.AnalysisApp;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.NeelProcessedTweet;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import it.unimib.disco.bigtwine.services.analysis.messaging.TwitterNeelOutputConsumerChannel;
import it.unimib.disco.bigtwine.services.analysis.messaging.TwitterNeelProcessedTweetProducerChannel;
import it.unimib.disco.bigtwine.services.analysis.repository.NeelProcessedTweetRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Test class for the LogsResource REST controller.
 *
 * @see ProcessingOutputDispatcher
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnalysisApp.class)
public class ProcessingOutputDispatcherIntTest {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private MessageCollector messageCollector;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private NeelProcessedTweetRepository tweetRepository;

    @Autowired
    private TwitterNeelProcessedTweetProducerChannel outputChannel;

    @Autowired
    private TwitterNeelOutputConsumerChannel inputChannel;

    @SpyBean
    private ProcessingOutputDispatcher dispatcher;

    private Analysis analysis;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void setUp() {
        this.analysis = this.analysisService.save(
            new Analysis()
                .type(AnalysisType.TWITTER_NEEL)
                .inputType(AnalysisInputType.DOCUMENT)
                .documentId("testdoc-1")
                .owner("testuser-1")
        );
    }

    @Test
    public void testDispatch() {
        TwitterUser user = new TwitterUserDTO();
        user.setScreenName("testuser-1");
        user.setId("testuser-1");

        TwitterStatus status = new TwitterStatusDTO();
        status.setId("teststatus-1");
        status.setText("text");
        status.setUser(user);

        NeelTweetProcessedEvent e = new NeelTweetProcessedEvent();
        e.setAnalysisId(this.analysis.getId());
        e.setProcessDate(Instant.now());
        e.setStatus(status);
        e.setEntities(new LinkedEntity[0]);

        int tweetsSizeBeforeCreate = tweetRepository.findAll().size();

        inputChannel.twitterNeelOutputChannel().send(MessageBuilder.withPayload(e).build());

        List<NeelProcessedTweet> tweets = tweetRepository.findAll();
        assertThat(tweets).hasSize(tweetsSizeBeforeCreate + 1);

        Message<?> received = messageCollector
            .forChannel(outputChannel.twitterNeelProcessedTweetsChannel())
            .poll();
        assertNotNull(received);
        /*assertThat(received.getPayload()).isInstanceOf(NeelProcessedTweetDTO.class);
        NeelProcessedTweetDTO payload = (NeelProcessedTweetDTO)received.getPayload();
        assertThat(payload.getAnalysisId()).isEqualTo(analysis.getId());*/
        verify(this.dispatcher, times(1)).consumeTwitterNeelOutput(any());
    }
}
