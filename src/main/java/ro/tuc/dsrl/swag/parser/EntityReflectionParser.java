package ro.tuc.dsrl.swag.parser;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.log4j.Logger;

import ro.tuc.dsrl.swag.datamodel.DataTypePropertyData;
import ro.tuc.dsrl.swag.datamodel.FieldValueType;
import ro.tuc.dsrl.swag.datamodel.ObjectPropertyData;
import ro.tuc.dsrl.swag.datamodel.ObjectPropertyValue;
import ro.tuc.dsrl.swag.datamodel.OntologyClass;
import ro.tuc.dsrl.swag.datamodel.OntologyIndividual;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class EntityReflectionParser {

    private static final Logger LOGGER = Logger
            .getLogger(EntityReflectionParser.class);

    private EntityReflectionParser() {
    }

    public static OntologyIndividual getOntologyData(Object o) {
        if (o == null) {
            LOGGER.info("Null entity in EntityReflectionParser");
            return new OntologyIndividual("", (long) 0);
        }
        Class<?> clazz = o.getClass();
        String name = clazz.getSimpleName();
        OntologyIndividual data = new OntologyIndividual(name, (long) 0);
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (AnnotationParser.parseOwlIgnoreField(field)) {
                    continue;
                }

                if (AnnotationParser.parseInstanceIdField(field)) {
                    FieldValueType found = FieldParser.parse(field,
                            getFieldValue(field, o));
                    data.addField(found);
                }

                DataTypePropertyData dataTypeProperty = AnnotationParser.parseDataTypeProperty(field);

                if (dataTypeProperty.getDataTypeProperty() != null) {
                    FieldValueType found = FieldParser.parse(field,
                            getFieldValue(field, o));
                    data.addField(found);
                }

                ObjectPropertyData objectProperty = AnnotationParser
                        .parseObjectProperty(field);

                if (objectProperty.getObjectProperty() != null) {
                    List<ObjectPropertyValue> op = ObjectPropertyParser
                            .parseCollectionForeignKey(getFieldValue(field, o),
                                    objectProperty);

                    data.addObjectProperty(op);
                }

            }
            clazz = clazz.getSuperclass();
        }
        return data;
    }

    public static OntologyIndividual getOntologyIndividual(Class<?> clazz) {
        if (clazz == null) {
            LOGGER.info("Null class in EntityReflectionParser");
            return null;
        }

        String className = clazz.getSimpleName();
        String superClassName = clazz.getSuperclass().getSimpleName();

        OntologyIndividual ontologyIndividual = new OntologyIndividual(superClassName, 0L);
        ontologyIndividual.setIndividualName(className);

        return ontologyIndividual;
    }

    public static OntologyClass getOntologyClass(Class<?> clazz) {
        if (clazz == null) {
            LOGGER.info("Null class in EntityReflectionParser");
            return null;
        }

        String name = clazz.getSimpleName();
        OntologyClass ontologyClass = new OntologyClass(name);

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (AnnotationParser.parseOwlIgnoreField(field)) {
                continue;
            }

            DataTypePropertyData dataTypeProperty = AnnotationParser.parseDataTypeProperty(field);

            if (dataTypeProperty.getDataTypeProperty() != null) {
                FieldValueType found = FieldParser.parse(field, null);
                ontologyClass.addField(found);
            }

            ObjectPropertyData objectProperty = AnnotationParser
                    .parseObjectProperty(field);
            if (objectProperty.getObjectProperty() != null) {
                List<ObjectPropertyValue> op = ObjectPropertyParser
                        .parseCollectionForeignKey(null, objectProperty);

                ontologyClass.addObjectProperty(op);
            }

        }
        String superClazz = clazz.getSuperclass().getSimpleName();
        ontologyClass.setSuperClass(superClazz);
        return ontologyClass;
    }

    private static Object getFieldValue(Field field, Object o) {
        try {
            if (o != null) {
                return field.get(o);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("", e);
        }
        return null;
    }

}
