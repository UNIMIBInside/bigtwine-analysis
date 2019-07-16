package it.unimib.disco.bigtwine.services.analysis.domain.mapper;

import it.unimib.disco.bigtwine.commons.models.dto.NeelProcessedTweetDTO;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisResultPayload;
import it.unimib.disco.bigtwine.services.analysis.domain.NeelProcessedTweet;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.TimeZone;

@Mapper
public interface NeelProcessedTweetMapper extends AnalysisResultPayloadMapper {
    NeelProcessedTweetMapper INSTANCE = Mappers.getMapper( NeelProcessedTweetMapper.class );

    NeelProcessedTweet neelProcessedTweetFromDto(NeelProcessedTweetDTO tweet);

    @Override
    default AnalysisResultPayload map(Object object) {
        if (object instanceof NeelProcessedTweetDTO) {
            return neelProcessedTweetFromDto((NeelProcessedTweetDTO) object);
        } else {
            throw new IllegalArgumentException("object must be an instance of NeelProcessedTweetDTO");
        }
    }
}
