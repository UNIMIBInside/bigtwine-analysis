package it.unimib.disco.bigtwine.services.analysis.web.api;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.NeelProcessedTweet;
import it.unimib.disco.bigtwine.services.analysis.domain.mapper.NeelProcessedTweetMapper;
import it.unimib.disco.bigtwine.services.analysis.repository.NeelProcessedTweetRepository;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.NoSuchEntityException;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.NeelProcessedTweetDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.TwitterNeelAnalysisResultDTO;
import it.unimib.disco.bigtwine.services.analysis.web.api.util.AnalysisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@Service
public class AnalysisResultsApiDelegateImpl implements AnalysisResultsApiDelegate {


    private final Logger log = LoggerFactory.getLogger(AnalysesApiDelegateImpl.class);
    private final NativeWebRequest request;
    private final AnalysisService analysisService;
    private final NeelProcessedTweetRepository tweetRepository;


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public AnalysisResultsApiDelegateImpl(NativeWebRequest request, AnalysisService analysisService, NeelProcessedTweetRepository tweetRepository) {
        this.request = request;
        this.analysisService = analysisService;
        this.tweetRepository = tweetRepository;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }


    @Override
    public ResponseEntity<TwitterNeelAnalysisResultDTO> getTwitterNeelAnalysisResultsV1(String analysisId, Integer pageNum, Integer pageSize) {
        Optional<Analysis> analysis = this.analysisService.findOne(analysisId);

        if (!analysis.isPresent()) {
            throw new NoSuchEntityException(String.format("Analysis with id '%s' not found", analysis));
        }

        AnalysisUtil.checkAnalysisOwnership(analysis.get(), AnalysisUtil.AccessType.READ);

        Pageable page = PageRequest.of(pageNum, pageSize);
        Page<NeelProcessedTweet> tweets = this.tweetRepository.findByAnalysisId(analysis.get().getId(), page);
        List<NeelProcessedTweetDTO> tweetDTOs = NeelProcessedTweetMapper.INSTANCE
            .neelProcessedTweetDTOsFromModels(tweets.getContent());

        TwitterNeelAnalysisResultDTO result = new TwitterNeelAnalysisResultDTO();
        result
            .objects(tweetDTOs)
            .page(tweets.getPageable().getPageNumber())
            .pageSize(tweets.getPageable().getPageSize())
            .count(tweets.getNumberOfElements())
            .totalCount(tweets.getTotalElements());

        return ResponseEntity.ok(result);
    }
}
