package it.unimib.disco.bigtwine.services.analysis.domain.mapper;

import it.unimib.disco.bigtwine.commons.messaging.NeelTweetProcessedEvent;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.NeelProcessedTweet;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.NeelProcessedTweetDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.TimeZone;

@Mapper
public interface NeelProcessedTweetMapper {
    NeelProcessedTweetMapper INSTANCE = Mappers.getMapper( NeelProcessedTweetMapper.class );

    default OffsetDateTime fromInstant(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, TimeZone.getTimeZone("UTC").toZoneId());
    }

    default Instant fromOffsetDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? null : Instant.from(dateTime);
    }

    @Mapping(source = "analysis.id", target = "analysisId")
    NeelProcessedTweetDTO neelProcessedTweetDTOFromModel(NeelProcessedTweet tweet);

    List<NeelProcessedTweetDTO> neelProcessedTweetDTOsFromModels(List<NeelProcessedTweet> tweets);
}
