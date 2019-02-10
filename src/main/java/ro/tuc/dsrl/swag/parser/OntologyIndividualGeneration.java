package ro.tuc.dsrl.swag.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import ro.tuc.dsrl.swag.annotations.ontology.OntologyInstance;
import ro.tuc.dsrl.swag.datamodel.OntologyIndividual;
import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class OntologyIndividualGeneration {

    private static final String PACKAGE = PropertiesLoader.getProperty(ConfigurationProperties.ENTITIES_PACKAGE);
    private static final String AUTO = PropertiesLoader.getProperty(ConfigurationProperties.AUTO_GEN);

    private OntologyIndividualGeneration() {
    }

    public static List<OntologyIndividual> generateOwlIndividuals() {
        if (AUTO == null || AUTO.equals("false")) {
            return new ArrayList<OntologyIndividual>();
        }

        Reflections reflections = new Reflections(PACKAGE);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(OntologyInstance.class);

        List<OntologyIndividual> ontologyIndividuals = new ArrayList<OntologyIndividual>();
        for (Class<?> c : annotated) {
            ontologyIndividuals.add(EntityReflectionParser.getOntologyIndividual(c));
        }
        return ontologyIndividuals;
    }

}
