package it.unimib.disco.bigtwine.services.analysis.web.api.util;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisVisibility;
import it.unimib.disco.bigtwine.services.analysis.security.AuthoritiesConstants;
import it.unimib.disco.bigtwine.services.analysis.security.SecurityUtils;
import it.unimib.disco.bigtwine.services.analysis.web.api.errors.UnauthorizedException;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public final class AnalysisUtil {

    public enum AccessType {
        READ, UPDATE, DELETE
    }

    public static Optional<String> getCurrentUserIdentifier() {
        return SecurityUtils.getCurrentUserLogin();
    }

    /**
     * Controlla che l'utente corrente sia il proprietario dell'analisi corrente o che l'analisi sia pubblica
     * in caso contrario lancia un eccesione
     *
     * @param analysis L'analisis da controllare
     * @return true se l'utente corrente è proprietario dell'analisi, false altrimenti
     * @throws UnauthorizedException Nel caso l'analisi non sia accessibile dall'utente corrente
     */
    public static boolean checkAnalysisOwnership(@NotNull Analysis analysis, AccessType accessType) {
        String ownerId = getCurrentUserIdentifier().orElse(null);
        boolean isOwner = ownerId != null && ownerId.equals(analysis.getOwner());
        boolean isSuperUser = SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN);

        if (isOwner || isSuperUser) {
            return isOwner;
        }else {
            // Non siamo né i proprietari dell'analisi né un admin
            if (accessType == AccessType.DELETE) {
                throw new UnauthorizedException();
            }

            if (analysis.getVisibility() == AnalysisVisibility.PRIVATE) {
                throw new UnauthorizedException();
            }
        }

        return false;
    }
}
