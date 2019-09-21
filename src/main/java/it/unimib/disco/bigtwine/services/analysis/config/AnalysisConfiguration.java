package it.unimib.disco.bigtwine.services.analysis.config;

import it.unimib.disco.bigtwine.commons.messaging.dto.NeelProcessedTweetDTO;
import it.unimib.disco.bigtwine.services.analysis.domain.NeelProcessedTweet;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisResultMapperLocator;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisResultPayloadMapperLocator;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.NeelProcessedTweetMapper;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.TwitterNeelAnalysisResultMapper;
import it.unimib.disco.bigtwine.services.analysis.validation.AnalysisStatusStaticValidator;
import it.unimib.disco.bigtwine.services.analysis.validation.AnalysisStatusValidator;
import it.unimib.disco.bigtwine.services.analysis.validation.analysis.input.AnalysisInputValidatorLocator;
import it.unimib.disco.bigtwine.services.analysis.validation.analysis.input.AnalysisInputValidatorRegistry;
import it.unimib.disco.bigtwine.services.analysis.validation.analysis.input.DatasetAnalysisInputValidator;
import it.unimib.disco.bigtwine.services.analysis.validation.analysis.input.QueryAnalysisInputValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisConfiguration {

    @Bean
    public AnalysisStatusValidator analysisStatusValidator() {
        return new AnalysisStatusStaticValidator();
    }

    @Bean
    public AnalysisInputValidatorLocator analysisInputValidatorLocator() {
        AnalysisInputValidatorRegistry registry = new AnalysisInputValidatorRegistry();
        registry.registerInputValidator(new QueryAnalysisInputValidator(), AnalysisInputType.QUERY);
        registry.registerInputValidator(new DatasetAnalysisInputValidator(), AnalysisInputType.DATASET);

        return registry;
    }

    @Bean
    public AnalysisResultPayloadMapperLocator analysisResultPayloadMapperLocator() {
        AnalysisResultPayloadMapperLocator locator = new AnalysisResultPayloadMapperLocator();
        locator.registerMapper(NeelProcessedTweetDTO.class, NeelProcessedTweetMapper.INSTANCE);

        return locator;
    }

    @Bean
    public AnalysisResultMapperLocator analysisResultMapperLocator() {
        AnalysisResultMapperLocator locator = new AnalysisResultMapperLocator();
        locator.registerMapper(NeelProcessedTweet.class, TwitterNeelAnalysisResultMapper.INSTANCE);

        return locator;
    }
}
