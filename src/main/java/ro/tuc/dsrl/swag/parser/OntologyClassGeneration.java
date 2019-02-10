package ro.tuc.dsrl.swag.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import ro.tuc.dsrl.swag.annotations.ontology.OntologyEntity;
import ro.tuc.dsrl.swag.datamodel.OntologyClass;
import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class OntologyClassGeneration {

    private static final String PACKAGE = PropertiesLoader.getProperty(ConfigurationProperties.ENTITIES_PACKAGE);
    private static final String AUTO = PropertiesLoader.getProperty(ConfigurationProperties.AUTO_GEN);

    private OntologyClassGeneration() {
    }

    public static List<OntologyClass> generateOwlClasses() {
        if (AUTO == null || AUTO.equals("false")) {
            return new ArrayList<OntologyClass>();
        }

        Reflections reflections = new Reflections(PACKAGE);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(OntologyEntity.class);

        List<OntologyClass> ontologyClasses = new ArrayList<OntologyClass>();
        for (Class<?> c : annotated) {
            ontologyClasses.add(EntityReflectionParser.getOntologyClass(c));
        }
        return ontologyClasses;
    }

}
