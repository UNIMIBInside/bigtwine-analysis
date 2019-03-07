package it.unimib.disco.bigtwine.services.analysis.config;

import it.unimib.disco.bigtwine.services.analysis.validation.AnalysisStatusStaticValidator;
import it.unimib.disco.bigtwine.services.analysis.validation.AnalysisStatusValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisConfiguration {

    @Bean
    public AnalysisStatusValidator analysisStatusValidator() {
        return new AnalysisStatusStaticValidator();
    }
}
