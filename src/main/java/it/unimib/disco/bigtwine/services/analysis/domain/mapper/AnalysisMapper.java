package it.unimib.disco.bigtwine.services.analysis.domain.mapper;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisStatusHistory;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisErrorCode;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisStatusEnum;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisStatusHistoryDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisVisibilityEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.TimeZone;

@Mapper
public interface AnalysisMapper {

    AnalysisMapper INSTANCE = Mappers.getMapper( AnalysisMapper.class );

    default OffsetDateTime fromInstant(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, TimeZone.getTimeZone("UTC").toZoneId());
    }

    default Instant fromOffsetDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? null : Instant.from(dateTime);
    }

    Analysis analysisFromAnalysisDTO(AnalysisDTO analysisDTO);

    List<Analysis> analysesFromAnalysisDTOs(List<AnalysisDTO> analysisDTOs);

    AnalysisDTO analysisDtoFromAnalysis(Analysis analysis);

    List<AnalysisDTO> analysisDtosFromAnalyses(List<Analysis> analyses);

    AnalysisVisibility visibilityFromVisibilityEnum(AnalysisVisibilityEnum visibility);

    AnalysisStatus statusFromStatusEnum(AnalysisStatusEnum status);

    AnalysisStatusHistoryDTO statusHistoryDTOFromStatusHistory(AnalysisStatusHistory statusHistory);

    List<AnalysisStatusHistoryDTO> statusHistoryDTOsFromStatusHistories(List<AnalysisStatusHistory> statusHistories);

    default AnalysisErrorCode analysisErrorCodeFromInt(Integer value) {
        return AnalysisErrorCode.valueOf(value);
    }

    default Integer intFromAnalysisErrorCode(AnalysisErrorCode value) {
        return value.getValue();
    }
}
