package it.unimib.disco.bigtwine.services.analysis.messaging;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface TwitterNeelProcessedTweetProducerChannel {
    String CHANNEL = "twitterNeelProcessedTweetsChannel";

    @Output
    MessageChannel twitterNeelProcessedTweetsChannel();
}
