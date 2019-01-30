package it.unimib.disco.bigtwine.services.analysis.web.api;

import it.unimib.disco.bigtwine.services.analysis.web.api.TwitterNeelApiDelegate;
import it.unimib.disco.bigtwine.services.analysis.web.api.model.Analysis;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

@Service
public class TwitterNeelApiDelegateImpl implements TwitterNeelApiDelegate {

    private final NativeWebRequest request;

    public TwitterNeelApiDelegateImpl(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<Analysis> getTwitterNeelAnalysisV1(String analysisId) {
        Analysis analysis = new Analysis();
        analysis.setId("1");
        analysis.setName("aaa");

        return ResponseEntity.ok(analysis);
    }
}
