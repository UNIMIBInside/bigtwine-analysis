package it.unimib.disco.bigtwine.services.analysis.repository;

import it.unimib.disco.bigtwine.services.analysis.AnalysisApp;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisInputType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisType;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnalysisApp.class)
public class AnalysisRepositoryIntTest {

    @Autowired
    private AnalysisRepository analysisRepository;

    @Before
    public void setUp() throws Exception {
        for (int i = 1; i <= 3; ++i) {
            Analysis a = new Analysis()
                .ownerId("user-1")
                .status(AnalysisStatus.READY)
                .type(AnalysisType.TWITTER_NEEL)
                .inputType(AnalysisInputType.QUERY)
                .visibility(AnalysisVisibility.PUBLIC)
                .query("prova" + i)
                .createDate(Instant.now());

            this.analysisRepository.save(a);
        }
    }

    @Test
    public void findByOwner() {
        List<Analysis> analyses = this.analysisRepository.findByOwner("user-1");

        assertNotNull(analyses);
        assertEquals(1, analyses.size());

        analyses = this.analysisRepository.findByOwner("user-2");

        assertNotNull(analyses);
        assertEquals(0, analyses.size());
    }

    @Test
    public void findByOwnerPaged() {
        Pageable page = PageRequest.of(1, 2);
        Page<Analysis> analyses = this.analysisRepository.findByOwner("user-1", page);

        assertNotNull(analyses);
        assertEquals(2, analyses.getTotalPages());

        assertNotNull(analyses);
        assertEquals(2, analyses.getSize());
    }
}
