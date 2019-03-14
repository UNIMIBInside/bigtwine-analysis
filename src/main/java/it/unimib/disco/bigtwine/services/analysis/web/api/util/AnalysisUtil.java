package it.unimib.disco.bigtwine.services.analysis.web.api.util;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import it.unimib.disco.bigtwine.services.analysis.security.SecurityUtils;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UnauthorizedException;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public final class AnalysisUtil {

    public static Optional<String> getCurrentUserIdentifier() {
        return SecurityUtils.getCurrentUserLogin();
    }

    /**
     * Controlla che l'utente corrente sia il proprietario dell'analisi corrente o che l'analisi sia pubblica
     * in caso contrario lancia un eccesione
     *
     * @param analysis L'analisis da controllare
     * @throws UnauthorizedException Nel caso l'analisi non sia accessibile dall'utente corrente
     */
    public static boolean checkAnalysisOwnership(@NotNull Analysis analysis) {
        String ownerId = getCurrentUserIdentifier().orElse(null);
        boolean isOwner = ownerId != null && ownerId.equals(analysis.getOwner());

        if (analysis.getVisibility() == AnalysisVisibility.PRIVATE) {
            if (!isOwner) {
                throw new UnauthorizedException();
            }
        }

        return isOwner;
    }
}
