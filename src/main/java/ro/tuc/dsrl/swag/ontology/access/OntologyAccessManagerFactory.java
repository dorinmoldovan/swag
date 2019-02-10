package ro.tuc.dsrl.swag.ontology.access;

import org.apache.log4j.Logger;

import ro.tuc.dsrl.swag.types.ApiType;
import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class OntologyAccessManagerFactory {
    private static final Logger LOGGER = Logger.getLogger(OntologyAccessManagerFactory.class);

    private static final String TYPE = PropertiesLoader
            .getProperty(ConfigurationProperties.API_TYPE);
    private static volatile OntologyAccessManager instance;

    private OntologyAccessManagerFactory() {
    }

    public static OntologyAccessManager getInstance() {
        if (instance == null) {
            synchronized (OntologyAccessManagerFactory.class) {
                if (instance == null) {
                    switch (ApiType.valueOf(TYPE)) {
                        case JENA: {
                            instance = JenaAccessManager.getInstance();
                            break;
                        }
                        case D2RQ: {
                            instance = D2RQAccessManager.getInstance();
                            break;
                        }
                        default: {
                            LOGGER.error("The specified API type is not available.");
                            break;
                        }
                    }
                }
            }
        }
        return instance;
    }
}
