package it.unimib.disco.bigtwine.services.analysis.domain.mapper;

import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisResult;
import it.unimib.disco.bigtwine.services.analysis.domain.NeelProcessedTweet;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisResultDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.TwitterNeelAnalysisResultDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TwitterNeelAnalysisResultMapper extends AnalysisResultMapper {
    TwitterNeelAnalysisResultMapper INSTANCE = Mappers.getMapper( TwitterNeelAnalysisResultMapper.class );

    @Mapping(source = "analysis.id", target = "analysisId")
    TwitterNeelAnalysisResultDTO analysisResultDtoFromModel(AnalysisResult<NeelProcessedTweet> result);

    @Override
    default AnalysisResultDTO map(AnalysisResult result) {
        if (result.getPayload() instanceof NeelProcessedTweet) {
            @SuppressWarnings("unchecked")
            AnalysisResult<NeelProcessedTweet> _result = (AnalysisResult<NeelProcessedTweet>) result;
            return analysisResultDtoFromModel(_result);
        } else {
            throw new IllegalArgumentException("object payload must be an instance of NeelProcessedTweet");
        }
    }
}
