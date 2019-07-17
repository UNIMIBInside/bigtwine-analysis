package it.unimib.disco.bigtwine.services.analysis.service;

import it.unimib.disco.bigtwine.commons.messaging.AnalysisResultProducedEvent;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisResult;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisResultPayload;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisResultMapper;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisResultMapperLocator;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisResultPayloadMapper;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisResultPayloadMapperLocator;
import it.unimib.disco.bigtwine.services.analysis.messaging.AnalysisResultsConsumerChannel;
import it.unimib.disco.bigtwine.services.analysis.messaging.AnalysisResultsProducerChannel;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisResultsRepository;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class ProcessingOutputDispatcher {

    private final Logger log = LoggerFactory.getLogger(ProcessingOutputDispatcher.class);

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
            if (payloadDto == null) {
                log.debug("Payload missing");
                return null;
            }

            AnalysisResultPayloadMapper mapper = this.payloadMapperLocator
                .getMapper(payloadDto.getClass());

            if (mapper == null) {
                log.debug("Missing mapper for payload type: {}", payloadDto.getClass());
                return null;
            }

            AnalysisResultPayload payload = mapper.map(payloadDto);

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
        if (result.getPayload() == null) {
            log.debug("Payload missing");
            return;
        }

        AnalysisResultMapper mapper = this.resultMapperLocator
            .getMapper(result.getPayload().getClass());

        if (mapper == null) {
            log.debug("Missing mapper for analysis result payload type: {}", result.getPayload().getClass());
            return;
        }

        AnalysisResultDTO resultDto = mapper.map(result);
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
