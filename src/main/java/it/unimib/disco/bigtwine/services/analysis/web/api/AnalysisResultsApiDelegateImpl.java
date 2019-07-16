package it.unimib.disco.bigtwine.services.analysis.web.api;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisResult;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisResultMapperLocator;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisResultsRepository;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.NoSuchEntityException;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisResultDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisResultsCount;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.PagedAnalysisResults;
import it.unimib.disco.bigtwine.services.analysis.web.api.util.AnalysisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AnalysisResultsApiDelegateImpl implements AnalysisResultsApiDelegate {


    private final Logger log = LoggerFactory.getLogger(AnalysesApiDelegateImpl.class);
    private final NativeWebRequest request;
    private final AnalysisService analysisService;
    private final AnalysisResultsRepository resultsRepository;
    private final AnalysisResultMapperLocator resultMapperLocator;


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public AnalysisResultsApiDelegateImpl(
        NativeWebRequest request,
        AnalysisService analysisService,
        AnalysisResultsRepository resultsRepository,
        AnalysisResultMapperLocator resultMapperLocator) {
        this.request = request;
        this.analysisService = analysisService;
        this.resultsRepository = resultsRepository;
        this.resultMapperLocator = resultMapperLocator;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    private Analysis getAnalysisById(String analysisId) {
        Analysis analysis = this.analysisService.findOne(analysisId).orElseThrow(() ->
            new NoSuchEntityException(String.format("Analysis with id '%s' not found", analysisId))
        );

        AnalysisUtil.checkAnalysisOwnership(analysis, AnalysisUtil.AccessType.READ);

        return analysis;
    }

    private ResponseEntity<PagedAnalysisResults> buildPagedResultsResponse(Analysis analysis, Page<AnalysisResult<?>> pageObj) {
        List<AnalysisResult<?>> results = pageObj.getContent();
        List<Object> resultsDtos = new ArrayList<>(results.size());
        results.forEach((AnalysisResult<?> result) -> {
            // TODO: Controllare payload != null e che mapper esiste
            AnalysisResultDTO resultDto = this.resultMapperLocator
                .getMapper(result.getPayload().getClass())
                .map(result);

            resultsDtos.add(resultDto);
        });

        PagedAnalysisResults pagedResults = new PagedAnalysisResults();
        pagedResults
            .objects(resultsDtos)
            .analysisId(analysis.getId())
            .page(pageObj.getPageable().getPageNumber())
            .pageSize(pageObj.getPageable().getPageSize())
            .count(pageObj.getNumberOfElements())
            .totalCount(pageObj.getTotalElements());

        return ResponseEntity.ok(pagedResults);
    }

    @Override
    public ResponseEntity<PagedAnalysisResults> getAnalysisResultsV1(String analysisId, Integer pageNum, Integer pageSize) {
        Analysis analysis = this.getAnalysisById(analysisId);
        Pageable page = PageRequest.of(pageNum, pageSize);

        @SuppressWarnings("unchecked")
        Page<AnalysisResult<?>> pageObj = resultsRepository.findByAnalysisId(analysis.getId(), page);

        return this.buildPagedResultsResponse(analysis, pageObj);
    }

    @Override
    public ResponseEntity<PagedAnalysisResults> searchAnalysisResultsV1(String analysisId, String filter, Integer pageNum, Integer pageSize) {
        Analysis analysis = this.getAnalysisById(analysisId);
        Pageable page = PageRequest.of(pageNum, pageSize);
        Example example = new Example() {
            @Override
            public Object getProbe() {
                return null;
            }

            @Override
            public ExampleMatcher getMatcher() {
                return null;
            }
        };

        @SuppressWarnings("unchecked")
        Page<Object> pageObj = resultsRepository.findAll(example, page);

        return null; // this.buildPagedResultsResponse(analysis, pageObj);
    }

    @Override
    public ResponseEntity<AnalysisResultsCount> countAnalysisResultsV1(String analysisId) {
        Analysis analysis = this.getAnalysisById(analysisId);
        @SuppressWarnings("unchecked")
        long count = resultsRepository.countByAnalysisId(analysisId);

        AnalysisResultsCount body = new AnalysisResultsCount()
            .analysisId(analysis.getId())
            .count((double) count);

        return ResponseEntity.ok(body);
    }
}
