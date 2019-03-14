package it.unimib.disco.bigtwine.services.analysis.service;

import it.unimib.disco.bigtwine.commons.messaging.NeelTweetProcessedEvent;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.NeelProcessedTweet;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.NeelProcessedTweetMapper;
import it.unimib.disco.bigtwine.services.analysis.messaging.TwitterNeelOutputConsumerChannel;
import it.unimib.disco.bigtwine.services.analysis.messaging.TwitterNeelProcessedTweetProducerChannel;
import it.unimib.disco.bigtwine.services.analysis.repository.NeelProcessedTweetRepository;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.NeelProcessedTweetDTO;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

@Service
public class ProcessingOutputDispatcher {

    private AnalysisService analysisService;
    private NeelProcessedTweetRepository neelProcessedTweetRepository;
    private MessageChannel neelOutputForwardChannel;

    public ProcessingOutputDispatcher(
        AnalysisService analysisService,
        NeelProcessedTweetRepository neelProcessedTweetRepository,
        TwitterNeelProcessedTweetProducerChannel channel) {
        this.analysisService = analysisService;
        this.neelProcessedTweetRepository = neelProcessedTweetRepository;
        this.neelOutputForwardChannel = channel.twitterNeelProcessedTweetsChannel();
    }

    private NeelProcessedTweet saveProcessedTweet(NeelTweetProcessedEvent e) {
        Optional<Analysis> analysis = analysisService.findOne(e.getAnalysisId());

        if (analysis.isPresent()) {
            NeelProcessedTweet tweet = new NeelProcessedTweet()
                .analysis(analysis.get())
                .status(e.getStatus())
                .entities(Arrays.asList(e.getEntities()))
                .saveDate(Instant.now())
                .processDate(e.getProcessDate());

            return this.neelProcessedTweetRepository.save(tweet);
        }

        return null;
    }

    private void forwardProcessedTweet(NeelProcessedTweet tweet) {
        NeelProcessedTweetDTO tweetDTO = NeelProcessedTweetMapper.INSTANCE.neelProcessedTweetDTOFromModel(tweet);
        Message<NeelProcessedTweetDTO> message = MessageBuilder
            .withPayload(tweetDTO)
            .build();
        this.neelOutputForwardChannel
            .send(message);
    }

    @StreamListener(TwitterNeelOutputConsumerChannel.CHANNEL)
    public void consumeTwitterNeelOutput(NeelTweetProcessedEvent e) {
        NeelProcessedTweet savedTweet = this.saveProcessedTweet(e);
        if (savedTweet != null) {
            this.forwardProcessedTweet(savedTweet);
        }
    }
}
