package it.unimib.disco.bigtwine.services.analysis.web.api.util;

import it.unimib.disco.bigtwine.services.analysis.domain.Analysis;
import it.unimib.disco.bigtwine.services.analysis.domain.User;
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
        return SecurityUtils.getCurrentUserId();
    }

    public static Optional<User> getCurrentUser() {
        String userId = SecurityUtils.getCurrentUserId().orElse(null);
        if (userId == null) {
            return Optional.empty();
        }

        User user = new User()
            .uid(userId)
            .username(SecurityUtils.getCurrentUserLogin().orElse(null));

        return Optional.of(user);
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
        boolean isOwner = ownerId != null && analysis.getOwner() != null && ownerId.equals(analysis.getOwner().getUid());
        boolean isSuperUser = SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN);

        if (isOwner || isSuperUser) {
            return isOwner;
        } else {
            // Non siamo né i proprietari dell'analisi né un admin
            if (accessType == AccessType.DELETE || accessType == AccessType.UPDATE) {
                throw new UnauthorizedException();
            }

            if (analysis.getVisibility() == AnalysisVisibility.PRIVATE) {
                throw new UnauthorizedException();
            }
        }

        return false;
    }

    /**
     * Controlla che l'utente corrente possa creare nuove analisi
     * @param analysis L'analisi che si vorrebbe create
     * @return Restituisce true se l'utente corrente può creare nuove analisi
     */
    public static boolean canCreateAnalysis(@NotNull Analysis analysis) {
        boolean isDemoUser = SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.DEMO);
        return !isDemoUser;
    }

    /**
     * Controlla che l'utente corrente possa terminare l'analisi indicata.
     * Con terminare si intende cambiare lo stato dell'analisi in uno degli stati terminali (COMPLETED, CANCELLED)
     * @param analysis L'analisi che si vorrebbe terminare
     * @return Restituisce true se l'utente corrente può terminare l'analisi
     */
    public static boolean canTerminateAnalysis(@NotNull Analysis analysis) {
        boolean isDemoUser = SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.DEMO);
        return !isDemoUser;
    }
}
