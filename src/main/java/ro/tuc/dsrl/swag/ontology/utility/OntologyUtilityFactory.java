package ro.tuc.dsrl.swag.ontology.utility;

import org.apache.log4j.Logger;

import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

import ro.tuc.dsrl.swag.types.ApiType;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class OntologyUtilityFactory {

    private static final Logger LOGGER = Logger
            .getLogger(OntologyUtilityFactory.class);

    private static final String TYPE = PropertiesLoader
            .getProperty(ConfigurationProperties.API_TYPE);

    private static volatile OntologyUtility instance;

    private OntologyUtilityFactory() {
    }

    public static OntologyUtility getInstance() {
        if (instance == null) {
            synchronized (OntologyUtility.class) {
                if (instance == null) {
                    switch (ApiType.valueOf(TYPE)) {
                        case JENA: {
                            instance = JenaUtility.getInstance();
                            break;
                        }
                        case D2RQ: {
                            instance = D2RQUtility.getInstance();
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
