package it.unimib.disco.bigtwine.services.analysis.web.api.util;

import it.unimib.disco.bigtwine.services.analysis.AnalysisApp;
import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UnauthorizedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Test class for the AnalysisUtil class.
 *
 * @see AnalysisUtil
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AnalysisApp.class)
public class AnalysisUtilTest {

    @Test
    @WithMockUser(username = "testuser-1")
    public void checkAnalysisOwnershipOwnedPrivate() {
        Analysis analysis = new Analysis()
            .owner("testuser-1")
            .visibility(AnalysisVisibility.PRIVATE);

        assertTrue(AnalysisUtil.checkAnalysisOwnership(analysis));
    }

    @Test(expected = UnauthorizedException.class)
    @WithMockUser(username = "testuser-1")
    public void checkAnalysisOwnershipUnownedPrivate() {
        Analysis analysis = new Analysis()
            .owner("testuser-2")
            .visibility(AnalysisVisibility.PRIVATE);

        AnalysisUtil.checkAnalysisOwnership(analysis);
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void checkAnalysisOwnershipOwnedPublic() {
        Analysis analysis = new Analysis()
            .owner("testuser-1")
            .visibility(AnalysisVisibility.PUBLIC);

        assertTrue(AnalysisUtil.checkAnalysisOwnership(analysis));
    }

    @Test
    @WithMockUser(username = "testuser-1")
    public void checkAnalysisOwnershipUnownedPublic() {
        Analysis analysis = new Analysis()
            .owner("testuser-2")
            .visibility(AnalysisVisibility.PUBLIC);

        assertFalse(AnalysisUtil.checkAnalysisOwnership(analysis));
    }

    @Test
    public void checkAnalysisOwnershipUnloggedPublic() {
        Analysis analysis = new Analysis()
            .owner("testuser-1")
            .visibility(AnalysisVisibility.PUBLIC);

        assertFalse(AnalysisUtil.checkAnalysisOwnership(analysis));
    }

    @Test(expected = UnauthorizedException.class)
    public void checkAnalysisOwnershipUnloggedPrivate() {
        Analysis analysis = new Analysis()
            .owner("testuser-1")
            .visibility(AnalysisVisibility.PRIVATE);

        AnalysisUtil.checkAnalysisOwnership(analysis);
    }
}
