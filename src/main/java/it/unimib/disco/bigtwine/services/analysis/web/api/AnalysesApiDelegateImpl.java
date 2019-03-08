package it.unimib.disco.bigtwine.services.analysis.web.api;

import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisStatusHistory;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisMapper;
import it.unimib.disco.bigtwine.services.analysis.security.SecurityUtils;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
import it.unimib.disco.bigtwine.services.analysis.validation.InvalidAnalysisStatusException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.BadRequestException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.NoSuchEntityException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UnauthorizedException;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.*;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Service
public class AnalysesApiDelegateImpl implements AnalysesApiDelegate {

    private final Logger log = LoggerFactory.getLogger(AnalysesApiDelegateImpl.class);
    private final NativeWebRequest request;
    private final AnalysisService analysisService;

    public AnalysesApiDelegateImpl(NativeWebRequest request, AnalysisService analysisService) {
        this.request = request;
        this.analysisService = analysisService;
    }

    private boolean checkAnalysisOwnership(@NotNull Analysis analysis, String ownerId) {
        return ownerId != null && ownerId.equals(analysis.getOwner());
    }

    private Optional<String> getCurrentUserIdentifier() {
        return SecurityUtils.getCurrentUserLogin();
    }

    private ResponseEntity<AnalysisDTO> updateAnalysis(
        String analysisId,
        AnalysisStatusEnum status,
        AnalysisVisibilityEnum visibility) {
        Optional<Analysis> analysisOpt = this.analysisService.findOne(analysisId);

        if (!analysisOpt.isPresent()) {
            throw new NoSuchEntityException(Analysis.class, analysisId);
        }

        Analysis analysis = analysisOpt.get();
        String ownerId = this.getCurrentUserIdentifier().orElse(null);

        if (!this.checkAnalysisOwnership(analysis, ownerId)) {
            throw new UnauthorizedException();
        }

        if (visibility != null) {
            AnalysisVisibility newVisibility = AnalysisMapper.INSTANCE.visibilityFromVisibilityEnum(visibility);

            analysis.setVisibility(newVisibility);
        }

        AnalysisStatus oldStatus = analysis.getStatus();
        AnalysisStatus newStatus = null;
        if (status != null) {
            newStatus = AnalysisMapper.INSTANCE.statusFromStatusEnum(status);

            analysis.setStatus(newStatus);
        }

        try {
            analysis = analysisService.save(analysis);
        } catch (InvalidAnalysisStatusException e) {
            throw new BadRequestException(e.getMessage());
        }

        if (newStatus != null) {
            AnalysisStatusHistory statusHistory = new AnalysisStatusHistory()
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .user(ownerId)
                .message("User status update request")
                .date(analysis.getUpdateDate())
                .analysis(analysis);

            this.analysisService.saveStatusHistory(statusHistory);
        }

        AnalysisDTO updatedAnalysisDTO = AnalysisMapper.INSTANCE.analysisDtoFromAnalysis(analysis);

        return new ResponseEntity<>(updatedAnalysisDTO, HttpStatus.OK);
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<AnalysisDTO> createAnalysisV1(AnalysisDTO analysis) {
        String ownerId = this.getCurrentUserIdentifier().orElse(null);

        if (ownerId == null) {
            throw new UnauthorizedException();
        }

        Analysis a = AnalysisMapper.INSTANCE.analysisFromAnalysisDTO(analysis);
        a.setOwner(ownerId);

        try {
            a = this.analysisService.save(a);
        }catch (ValidationException e) {
            throw new BadRequestException(e.getMessage());
        }

        AnalysisDTO savedAnalysis = AnalysisMapper.INSTANCE.analysisDtoFromAnalysis(a);
        URI entityLocation;

        try {
            entityLocation = new URI("/api/public/analyses/" + a.getId());
        }catch(URISyntaxException e) {
            // Must not happen
            log.error("Cannot create entity URI", e);
            throw new RuntimeException("Cannot create entity URI");
        }

        return ResponseEntity
            .created(entityLocation)
            .body(savedAnalysis);
    }

    @Override
    public ResponseEntity<List<AnalysisDTO>> listAnalysesV1(Integer pageNum, Integer pageSize) {
        String ownerId = this.getCurrentUserIdentifier().orElse(null);

        if (ownerId == null) {
            throw new UnauthorizedException();
        }

        if (pageNum == null) {
            pageNum = 0;
        }

        if (pageSize == null) {
            pageSize = 100;
        }

        if (pageNum < 0) {
            throw new BadRequestException("Page num must be greater of equal to 0");
        }

        if (pageSize < 1 || pageSize > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }

        Pageable page = PageRequest.of(pageNum, pageSize);
        List<Analysis> analyses = this.analysisService.findByOwner(ownerId, page).getContent();

        List<AnalysisDTO> analysisDTOs = AnalysisMapper.INSTANCE.analysisDtosFromAnalyses(analyses);


        return new ResponseEntity<>(analysisDTOs, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AnalysisDTO> getAnalysisV1(String analysisId) {
        Analysis analysis = this.analysisService.findOne(analysisId).orElse(null);

        if (analysis == null) {
            throw new NoSuchEntityException(Analysis.class, analysisId);
        }

        if (analysis.getVisibility() == AnalysisVisibility.PRIVATE) {
            String ownerId = this.getCurrentUserIdentifier().orElse(null);

            if (!this.checkAnalysisOwnership(analysis, ownerId)) {
                throw new UnauthorizedException();
            }
        }

        AnalysisDTO analysisDTO = AnalysisMapper.INSTANCE.analysisDtoFromAnalysis(analysis);

        return new ResponseEntity<>(analysisDTO, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteAnalysisV1(String analysisId) {
        ResponseEntity<AnalysisDTO> response = this.updateAnalysis(analysisId, AnalysisStatusEnum.CANCELLED, null);

        return new ResponseEntity<>(response.getStatusCode());
    }

    @Override
    public ResponseEntity<AnalysisDTO> patchAnalysisV1(String analysisId, AnalysisUpdatableDTO analysisDTO) {
       return this.updateAnalysis(analysisId, analysisDTO.getStatus(), analysisDTO.getVisibility());
    }

    @Override
    public ResponseEntity<List<AnalysisStatusHistoryDTO>> getAnalysisHistoryV1(String analysisId) {
        Optional<Analysis> analysis = this.analysisService.findOne(analysisId);

        if (!analysis.isPresent()) {
            throw new NoSuchEntityException(Analysis.class, analysisId);
        }

        if (analysis.get().getVisibility() == AnalysisVisibility.PRIVATE) {
            String ownerId = this.getCurrentUserIdentifier().orElse(null);

            if (!this.checkAnalysisOwnership(analysis.get(), ownerId)) {
                throw new UnauthorizedException();
            }
        }

        List<AnalysisStatusHistory> statusHistory = this.analysisService.getStatusHistory(analysisId);
        List<AnalysisStatusHistoryDTO> statusHistoryDTOs = AnalysisMapper.INSTANCE
            .statusHistoryDTOsFromStatusHistories(statusHistory);

        return new ResponseEntity<>(statusHistoryDTOs, HttpStatus.OK);
    }
}
