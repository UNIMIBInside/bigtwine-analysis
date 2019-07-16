package it.unimib.disco.bigtwine.services.analysis.config;

/**
 * Application constants.
 */
public final class Constants {

    // Db
    public static final String ANALYSIS_DB_COLLECTION = "analyses";
    public static final String ANALYSIS_RESULTS_DB_COLLECTION = "analyses.results";

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String DEFAULT_LANGUAGE = "en";
    
    private Constants() {
    }
}
