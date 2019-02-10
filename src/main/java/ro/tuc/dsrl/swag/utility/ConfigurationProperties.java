package ro.tuc.dsrl.swag.utility;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public enum ConfigurationProperties {

    OWL_FILE("OWL_FILE"),
    RULES_FILE("RULES_FILE"),
    URI("ONT_URI"),
    ENTITIES_PACKAGE("ENTITIES_PACKAGE"),
    AUTO_GEN("AUTO_GEN"),
    JDBC_DRIVER_CLASS_NAME("JDBC_DRIVER_CLASS_NAME"),
    JDBC_URL("JDBC_URL"),
    JDBC_USERNAME("JDBC_USERNAME"),
    JDBC_PASSWORD("JDBC_PASSWORD"),
    TTL_FILE("TTL_FILE"),
    API_TYPE("API_TYPE");

    private String value;

    private ConfigurationProperties(String url) {
        this.value = url;
    }

    public String value() {
        return value;
    }
}
