package ro.tuc.dsrl.swag.parser;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.reflections.Reflections;

import ro.tuc.dsrl.swag.annotations.jr.JRDefinition;
import ro.tuc.dsrl.swag.annotations.jr.JRInput;
import ro.tuc.dsrl.swag.annotations.jr.JROutput;
import ro.tuc.dsrl.swag.annotations.jr.JRParameter;
import ro.tuc.dsrl.swag.exceptions.FieldTypeException;
import ro.tuc.dsrl.swag.exceptions.FieldValueException;
import ro.tuc.dsrl.swag.exceptions.PropertyNameException;
import ro.tuc.dsrl.swag.jr.JRField;
import ro.tuc.dsrl.swag.jr.JRPredicate;
import ro.tuc.dsrl.swag.jr.JRPredicateType;
import ro.tuc.dsrl.swag.jr.JRRule;
import ro.tuc.dsrl.swag.jr.JRRulesCollection;
import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.JavaType;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class JRRulesParser {

    private static final Logger LOGGER = Logger.getLogger(JRRulesParser.class);

    private static final String PACKAGE = PropertiesLoader.getProperty(ConfigurationProperties.ENTITIES_PACKAGE);

    private JRReflectionParser jrReflectionParser;
    private List<String> classes;
    private List<String> properties;
    private List<String> individuals;

    public JRRulesParser() {
        this.jrReflectionParser = new JRReflectionParser();
    }

    private void checkPredicate(String predicate) throws PropertyNameException {
        boolean flag = false;
        for (String property : properties) {
            if (property.equals(predicate))
                flag = true;
        }
        for (String clazz : classes) {
            String[] tokens = clazz.split("\\.");
            if (tokens[tokens.length - 1].equals(predicate))
                flag = true;
        }
        if (!flag) {
            throw new PropertyNameException("The name of the property \"" + predicate + "\" is not correct.");
        }
    }

    private void checkFieldType(JRField field) throws FieldTypeException {
        boolean flag = false;
        for (String clazz : classes) {
            if (clazz.equals(field.getRange()))
                flag = true;
        }
        for (JavaType javaType : JavaType.values())
            if (javaType.value().equals(field.getRange()))
                flag = true;
        if (!flag) {
            throw new FieldTypeException("The type \"" + field.getRange() + "\" is unknown");
        }
    }

    private void checkFieldValue(JRField field) throws FieldValueException {
        int flag = 0;
        String range = field.getRange();
        String value = field.getValue();
        if (range.equals(JavaType.DOUBLE_WRAPPER.value())) {
            try {
                Double.parseDouble(value);
            } catch (Exception e) {
                flag = 1;
            }
        } else if (range.equals(JavaType.FLOAT_WRAPPER.value())) {
            try {
                Float.parseFloat(value);
            } catch (Exception e) {
                flag = 1;
            }
        } else if (range.equals(JavaType.INTEGER_WRAPPER.value())) {
            try {
                Integer.parseInt(value);
            } catch (Exception e) {
                flag = 1;
            }
        } else if (range.equals(JavaType.LONG_WRAPPER.value())) {
            try {
                Long.parseLong(value);
            } catch (Exception e) {
                flag = 1;
            }
        } else if (range.equals(JavaType.BOOLEAN_WRAPPER.value())) {
            try {
                Boolean.parseBoolean(value);
            } catch (Exception e) {
                flag = 1;
            }
        } else if (classes.contains(range)
                && !(range.equals(JavaType.OBJECT.value()) || range.equals(JavaType.STRING.value()))) {
            try {
                String individualValue = new String();
                Class<?> rangeClass = Class.forName(range);

                for (String individual : individuals) {
                    String temp = individual;
                    String[] tokens = temp.split("\\.");
                    if (tokens[tokens.length - 1].equals(value)) {
                        individualValue = individual;
                    }
                }

                Class<?> valueClass = Class.forName(individualValue);

                if (!rangeClass.isAssignableFrom(valueClass)) {
                    flag = 1;
                }
            } catch (ClassNotFoundException e) {
                flag = 2;
            }
        } else if (!(range.equals(JavaType.OBJECT.value()) || range.equals(JavaType.STRING.value()))) {
            flag = 2;
        }

        if (flag == 1) {
            throw new FieldValueException("The value \"" + value + "\" does not have the type " + range);
        } else if (flag == 2) {
            throw new FieldValueException(
                    "The range \"" + range + "\" is incorrect or the value \"" + value + "\" is incorrect");
        }
    }

    public void getJRTerms(Class<?> c, JRRule jrRule)
            throws PropertyNameException, FieldTypeException, FieldValueException {

        for (Class<?> clazz : c.getInterfaces()) {
            getJRTerms(clazz, jrRule);
        }

        List<JRPredicate> predicates = jrRule.getPredicates();

        String predicate;
        JRPredicateType predicateType = JRPredicateType.DEFAULT;
        List<JRField> fields = new ArrayList<JRField>();

        int index = 0;

        for (Method method : c.getDeclaredMethods()) {
            predicate = method.getName();

            index = Integer.parseInt(predicate.substring(6));

            fields = new ArrayList<JRField>();
            if (method.getAnnotation(JRInput.class) != null || method.getAnnotation(JROutput.class) != null) {
                if (method.getAnnotation(JRInput.class) != null) {
                    predicateType = JRPredicateType.INPUT;
                    predicate = method.getAnnotation(JRInput.class).value();
                } else if (method.getAnnotation(JROutput.class) != null) {
                    predicateType = JRPredicateType.OUTPUT;
                    predicate = method.getAnnotation(JROutput.class).value();
                }

                checkPredicate(predicate);

                for (Parameter parameter : method.getParameters()) {
                    if (parameter.getAnnotation(JRParameter.class) != null) {
                        JRField field = new JRField(parameter.getType().getName(),
                                parameter.getAnnotation(JRParameter.class).value());

                        checkFieldType(field);
                        checkFieldValue(field);
                        fields.add(field);
                    }

                }
            }

            JRPredicate jrPredicate = new JRPredicate(predicateType, predicate, fields, index);
            predicates.add(jrPredicate);

        }

    }

    public JRRulesCollection generateJRRulesCollection()
            throws PropertyNameException, FieldTypeException, FieldValueException {

        LOGGER.info("Generate JR Rules Collection");

        classes = jrReflectionParser.getAllClasses();
        properties = jrReflectionParser.getAllProperties();
        individuals = jrReflectionParser.getAllIndividuals();

        Reflections reflections = new Reflections(PACKAGE);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(JRDefinition.class);

        JRRulesCollection jrRulesCollection = new JRRulesCollection();

        for (Class<?> c : annotated) {
            JRRule jrRule = new JRRule();
            jrRule.setName(c.getSimpleName());
            getJRTerms(c, jrRule);
            jrRulesCollection.getJRRules().add(jrRule);
        }

        return jrRulesCollection;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public List<String> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(List<String> individuals) {
        this.individuals = individuals;
    }

}
