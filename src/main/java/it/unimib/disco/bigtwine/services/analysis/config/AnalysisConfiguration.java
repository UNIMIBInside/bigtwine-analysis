package it.unimib.disco.bigtwine.services.analysis.config;

import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisInput;
import it.unimib.disco.bigtwine.services.analysis.domain.DatasetAnalysisInput;
import it.unimib.disco.bigtwine.services.analysis.domain.QueryAnalysisInput;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
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
}
