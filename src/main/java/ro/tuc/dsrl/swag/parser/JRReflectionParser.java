package ro.tuc.dsrl.swag.parser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.reflections.Reflections;

import ro.tuc.dsrl.swag.annotations.ontology.OntologyEntity;
import ro.tuc.dsrl.swag.annotations.ontology.OntologyInstance;
import ro.tuc.dsrl.swag.jr.JRMathPredicates;
import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.JavaType;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class JRReflectionParser {
    private static final Logger LOGGER = Logger.getLogger(JRReflectionParser.class);

    private static final String PACKAGE = PropertiesLoader.getProperty(ConfigurationProperties.ENTITIES_PACKAGE);

    public List<String> getAllClasses() {

        LOGGER.info("Parse the ontology classes and the Java predefined classes");

        List<String> classes = new ArrayList<String>();

        Reflections reflections = new Reflections(PACKAGE);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(OntologyEntity.class);
        for (Class<?> c : annotatedClasses) {
            String className = c.getName();
            classes.add(className);
        }

        for (JavaType javaType : JavaType.values()) {
            classes.add(javaType.value());
        }

        return classes;
    }

    public List<String> getAllProperties() {
        LOGGER.info("Parse the ontology properties");

        List<String> properties = new ArrayList<String>();

        Reflections reflections = new Reflections(PACKAGE);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(OntologyEntity.class);
        for (Class<?> c : annotatedClasses) {
            for (Method method : c.getDeclaredMethods()) {
                String methodName = method.getName();
                if ("get".equals(methodName.substring(0, 3))) {
                    String suffix = methodName.substring(3, methodName.length());
                    suffix = Character.toString(suffix.charAt(0)).toUpperCase() + suffix.substring(1);
                    String property = "has" + suffix;

                    boolean flag = false;
                    for (String prop : properties) {
                        if (prop.equals(property)) {
                            flag = true;
                        }
                    }
                    if (!flag) {
                        properties.add(property);
                    }
                }
            }

        }

        for (JRMathPredicates jrStandardProperties : JRMathPredicates.values()) {
            properties.add(jrStandardProperties.value());
        }

        return properties;
    }

    public List<String> getAllIndividuals() {
        LOGGER.info("Parse the ontology individuals");

        List<String> individuals = new ArrayList<String>();

        Reflections reflections = new Reflections(PACKAGE);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(OntologyInstance.class);

        for (Class<?> c : annotatedClasses) {
            String className = c.getName();
            individuals.add(className);
        }

        return individuals;
    }

}
