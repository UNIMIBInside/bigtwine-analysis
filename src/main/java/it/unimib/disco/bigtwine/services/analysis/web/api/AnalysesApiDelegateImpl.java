package it.unimib.disco.bigtwine.services.analysis.web.api;

import it.unimib.disco.bigtwine.services.analysis.domain.*;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisMapper;
import it.unimib.disco.bigtwine.services.analysis.security.SecurityUtils;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
import it.unimib.disco.bigtwine.services.analysis.service.DocumentService;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.BadRequestException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.NoSuchEntityException;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UnauthorizedException;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.*;
import it.unimib.disco.bigtwine.services.analysis.web.api.util.AnalysisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import javax.validation.ValidationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnalysesApiDelegateImpl implements AnalysesApiDelegate {

    private final Logger log = LoggerFactory.getLogger(AnalysesApiDelegateImpl.class);
    private final NativeWebRequest request;
    private final AnalysisService analysisService;
    private final DocumentService documentService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public AnalysesApiDelegateImpl(
        NativeWebRequest request,
        AnalysisService analysisService,
        DocumentService documentService) {
        this.request = request;
        this.analysisService = analysisService;
        this.documentService = documentService;
    }

    private Optional<String> getCurrentUserIdentifier() {
        return SecurityUtils.getCurrentUserId();
    }
    private Optional<User> getCurrentUser() {
        return AnalysisUtil.getCurrentUser();
    }

    private ResponseEntity<AnalysisDTO> updateAnalysis(
        String analysisId,
        AnalysisStatusEnum status,
        AnalysisVisibilityEnum visibility,
        Map<String, Object> userSettings) {
        Optional<Analysis> analysisOpt = this.analysisService.findOne(analysisId);

        if (!analysisOpt.isPresent()) {
            throw new NoSuchEntityException(Analysis.class, analysisId);
        }

        Analysis analysis = analysisOpt.get();

        AnalysisUtil.AccessType accessType = (status == AnalysisStatusEnum.CANCELLED ?
            AnalysisUtil.AccessType.DELETE : AnalysisUtil.AccessType.UPDATE);
        AnalysisUtil.checkAnalysisOwnership(analysis, accessType);

        if (visibility != null || userSettings != null) {
            if (visibility != null) {
                AnalysisVisibility newVisibility = AnalysisMapper.INSTANCE.visibilityFromVisibilityEnum(visibility);
                analysis.setVisibility(newVisibility);
            }

            analysis.setSettings((userSettings != null) ? userSettings : new HashMap<>());
            analysisService.cleanAnalysisSettings(analysis, SecurityUtils.getCurrentUserRoles());

            try {
                analysis = analysisService.save(analysis);
            } catch (ValidationException e) {
                throw new BadRequestException(e.getMessage());
            }
        }

        if (status != null) {
            AnalysisStatus newStatus = AnalysisMapper.INSTANCE.statusFromStatusEnum(status);

            try {
                this.analysisService.requestStatusChange(analysis, newStatus, true);
            } catch (ValidationException e) {
                throw new BadRequestException(e.getMessage());
            }
        }

        AnalysisDTO updatedAnalysisDTO = AnalysisMapper.INSTANCE.analysisDtoFromAnalysis(analysis);

        return new ResponseEntity<>(updatedAnalysisDTO, HttpStatus.OK);
    }

    private void autofillAnalysisProperties(Analysis analysis) {
        User owner = this.getCurrentUser().orElseThrow(UnauthorizedException::new);
        analysis.setOwner(owner);

        if (analysis.getInput() instanceof DatasetAnalysisInput) {
            DatasetAnalysisInput input = (DatasetAnalysisInput)analysis.getInput();
            if (input.getDocumentId() != null) {
                Optional<Document> docOpt = this.documentService.findOne(input.getDocumentId());
                if (docOpt.isPresent()) {
                    input.setName(docOpt.get().getFilename());
                    input.setSize(docOpt.get().getSize());
                }
            }
        }
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<AnalysisDTO> createAnalysisV1(AnalysisDTO analysis) {
        Analysis a = AnalysisMapper.INSTANCE.analysisFromAnalysisDTO(analysis);
        analysisService.cleanAnalysisSettings(a, SecurityUtils.getCurrentUserRoles());

        this.autofillAnalysisProperties(a);

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
    public ResponseEntity<PagedAnalyses> listAnalysesV1(Integer pageNum, Integer pageSize, AnalysisTypeEnum aType, Boolean owned) {
        String userId = this.getCurrentUserIdentifier().orElse(null);

        if (userId == null) {
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

        Pageable page = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "updateDate"));
        Page<Analysis> pageObj;

        AnalysisType analysisType = AnalysisMapper.INSTANCE.analysisTypeFromTypeEnum(aType);
        if (owned) {
            pageObj = this.analysisService.findByOwnerAndType(userId, analysisType, page);
        } else {
            pageObj = this.analysisService.findVisibleByType(userId, analysisType, page);
        }

        List<Analysis> analyses = pageObj.getContent();
        List<AnalysisDTO> analysisDTOs = AnalysisMapper.INSTANCE.analysisDtosFromAnalyses(analyses);
        PagedAnalyses responseBody = new PagedAnalyses();

        responseBody
            .objects(analysisDTOs)
            .page(pageObj.getPageable().getPageNumber())
            .pageSize(pageObj.getPageable().getPageSize())
            .count(pageObj.getNumberOfElements())
            .totalCount(pageObj.getTotalElements());

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AnalysisDTO> getAnalysisV1(String analysisId) {
        Analysis analysis = this.analysisService.findOne(analysisId).orElse(null);

        if (analysis == null) {
            throw new NoSuchEntityException(Analysis.class, analysisId);
        }

        AnalysisUtil.checkAnalysisOwnership(analysis, AnalysisUtil.AccessType.READ);

        AnalysisDTO analysisDTO = AnalysisMapper.INSTANCE.analysisDtoFromAnalysis(analysis);

        return new ResponseEntity<>(analysisDTO, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteAnalysisV1(String analysisId) {
        ResponseEntity<AnalysisDTO> response = this.updateAnalysis(analysisId, AnalysisStatusEnum.CANCELLED, null, null);

        return new ResponseEntity<>(response.getStatusCode());
    }

    @Override
    public ResponseEntity<AnalysisDTO> patchAnalysisV1(String analysisId, AnalysisUpdatableDTO analysisDTO) {
       return this.updateAnalysis(
           analysisId,
           analysisDTO.getStatus(),
           analysisDTO.getVisibility(),
           analysisDTO.getSettings());
    }

    @Override
    public ResponseEntity<List<AnalysisStatusHistoryDTO>> getAnalysisHistoryV1(String analysisId) {
        Optional<Analysis> analysis = this.analysisService.findOne(analysisId);

        if (!analysis.isPresent()) {
            throw new NoSuchEntityException(Analysis.class, analysisId);
        }

        AnalysisUtil.checkAnalysisOwnership(analysis.get(), AnalysisUtil.AccessType.READ);

        List<AnalysisStatusHistory> statusHistory = this.analysisService.getStatusHistory(analysisId);
        List<AnalysisStatusHistoryDTO> statusHistoryDTOs = AnalysisMapper.INSTANCE
            .statusHistoryDTOsFromStatusHistories(statusHistory);

        return new ResponseEntity<>(statusHistoryDTOs, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getAnalysisDefaultSettingsV1(String analysisId) {
        Optional<Analysis> analysis = this.analysisService.findOne(analysisId);

        if (!analysis.isPresent()) {
            throw new NoSuchEntityException(Analysis.class, analysisId);
        }

        AnalysisUtil.checkAnalysisOwnership(analysis.get(), AnalysisUtil.AccessType.READ);
        List<String> userRoles = SecurityUtils.getCurrentUserRoles();
        Map<String, Object> defaults = this.analysisService
            .getAnalysisDefaultSettings(analysisId, userRoles);


        return ResponseEntity.ok(defaults);
    }
}
