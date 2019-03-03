package it.unimib.disco.bigtwine.services.analysis.web.api;

import it.unimib.disco.bigtwine.services.analysis.domain.mapper.AnalysisMapper;
import it.unimib.disco.bigtwine.services.analysis.repository.AnalysisRepository;
import it.unimib.disco.bigtwine.services.analysis.security.SecurityUtils;
import it.unimib.disco.bigtwine.services.analysis.service.AnalysisService;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.AnalysisDTO;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@Service
public class AnalysesApiDelegateImpl implements AnalysesApiDelegate {

    private final NativeWebRequest request;
    private final AnalysisService analysisService;

    public AnalysesApiDelegateImpl(NativeWebRequest request, AnalysisService analysisService) {
        this.request = request;
        this.analysisService = analysisService;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<Void> createAnalysisV1(AnalysisDTO analysis) {
//        System.out.println("Current user id: " + SecurityUtils.getUserId());
//        Analysis a = AnalysisMapper.INSTANCE.analysisFromAnalysisDTO(analysis);
//        this.analysisRepository.save(a);
        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<AnalysisDTO>> listAnalysesV1(Integer offset, Integer limit) {
        String userId = SecurityUtils.getUserId().orElse(null);

        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }


        return null;
    }
}
