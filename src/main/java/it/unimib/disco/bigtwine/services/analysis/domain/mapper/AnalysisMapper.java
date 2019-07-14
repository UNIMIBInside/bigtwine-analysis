package it.unimib.disco.bigtwine.services.analysis.domain.mapper;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.BeanUtil;
import it.unimib.disco.bigtwine.services.analysis.domain.*;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisErrorCode;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.BeanUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Mapper
public interface AnalysisMapper {

    AnalysisMapper INSTANCE = Mappers.getMapper( AnalysisMapper.class );
    ObjectMapper jsonMapper = new ObjectMapper();

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

    default AnalysisInput analysisInputFromAnalysisInputDTO(Object input) {
        return null;
    }

    default AnalysisInputDTO analysisInputDtoFromAnalysisInput(AnalysisInput input) {
        if (input instanceof QueryAnalysisInput) {
            return this.queryAnalysisInputDtoFromQueryAnalysisInput((QueryAnalysisInput)input);
        } else if (input instanceof DatasetAnalysisInput) {
            return this.datasetAnalysisInputDtoFromDatasetAnalysisInput((DatasetAnalysisInput)input);
        } else {
            throw new UnsupportedOperationException("Unsupported input type " + input.getClass());
        }
    }

    @AfterMapping
    default void afterAnalysisDTOMapping(@MappingTarget Analysis analysis, AnalysisDTO analysisDto) {
        if (analysis.getInputType() == null) {
            analysis.setInput(null);
        } else {
            this.jsonMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            AnalysisInput input = this.jsonMapper.convertValue(
                analysisDto.getInput(),
                analysis.getInputType().inputClass);

            analysis.setInput(input);
        }
    }

    QueryAnalysisInputDTO queryAnalysisInputDtoFromQueryAnalysisInput(QueryAnalysisInput input);

    DatasetAnalysisInputDTO datasetAnalysisInputDtoFromDatasetAnalysisInput(DatasetAnalysisInput input);

    QueryAnalysisInput queryAnalysisInputFromQueryAnalysisInputDTO(QueryAnalysisInputDTO input);

    DatasetAnalysisInput datasetAnalysisInputFromDatasetAnalysisInputDTO(DatasetAnalysisInputDTO input);

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
