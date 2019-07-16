package it.unimib.disco.bigtwine.services.analysis.service;

import it.unimib.disco.bigtwine.commons.messaging.AnalysisResultProducedEvent;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisResult;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisResultPayload;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisResultMapperLocator;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisResultPayloadMapperLocator;
import it.unimib.disco.bigtwine.services.analysis.messaging.AnalysisResultsConsumerChannel;
import it.unimib.disco.bigtwine.services.analysis.messaging.AnalysisResultsProducerChannel;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisResultsRepository;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisResultDTO;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class ProcessingOutputDispatcher {

    private AnalysisService analysisService;
    private AnalysisResultsRepository resultsRepository;
    private MessageChannel analysisResultsForwardChannel;
    private AnalysisResultPayloadMapperLocator payloadMapperLocator;
    private AnalysisResultMapperLocator resultMapperLocator;

    public ProcessingOutputDispatcher(
        AnalysisService analysisService,
        AnalysisResultsRepository resultsRepository,
        AnalysisResultPayloadMapperLocator payloadMapperLocator,
        AnalysisResultMapperLocator resultMapperLocator,
        AnalysisResultsProducerChannel channel) {
        this.analysisService = analysisService;
        this.resultsRepository = resultsRepository;
        this.payloadMapperLocator = payloadMapperLocator;
        this.resultMapperLocator = resultMapperLocator;
        this.analysisResultsForwardChannel = channel.analysisResultsForwardedChannel();
    }

    private AnalysisResult<?> saveAnalysisResult(AnalysisResultProducedEvent e) {
        Optional<Analysis> analysis = analysisService.findOne(e.getAnalysisId());

        if (analysis.isPresent()) {
            // TODO: Controllare payload != null e che mapper esiste
            Object payloadDto = e.getPayload();
            AnalysisResultPayload payload = this.payloadMapperLocator
                .getMapper(payloadDto.getClass())
                .map(payloadDto);

            AnalysisResult<?> result = new AnalysisResult<>()
                .analysis(analysis.get())
                .saveDate(Instant.now())
                .processDate(e.getProcessDate())
                .payload(payload);

            return this.resultsRepository.save(result);
        }

        return null;
    }

    private void forwardAnalysisResult(AnalysisResult result) {
        // TODO: Controllare payload != null e che mapper esiste
        AnalysisResultDTO resultDto = this.resultMapperLocator
            .getMapper(result.getPayload().getClass())
            .map(result);

        Message<AnalysisResultDTO> message = MessageBuilder
            .withPayload(resultDto)
            .build();

        this.analysisResultsForwardChannel
            .send(message);
    }

    @StreamListener(AnalysisResultsConsumerChannel.CHANNEL)
    public void consumeAnalysisResult(AnalysisResultProducedEvent e) {
        AnalysisResult<?> result = this.saveAnalysisResult(e);
        if (result != null) {
            this.forwardAnalysisResult(result);
        }
    }
}
