package ro.tuc.dsrl.swag.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;

import ro.tuc.dsrl.swag.datamodel.DataTypePropertyData;
import ro.tuc.dsrl.swag.datamodel.ObjectPropertyData;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class AnnotationParser {

    private static final Logger LOGGER = Logger.getLogger(AnnotationParser.class);

    private static final String RANGE = "range";

    private AnnotationParser() {
    }

    public static DataTypePropertyData parseDataTypeProperty(Field field) {
        final String dynamicProp = "DataTypeProperty";
        DataTypePropertyData data = new DataTypePropertyData();
        for (Annotation annotation : field.getDeclaredAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (dynamicProp.equals(type.getSimpleName())) {
                data.setDataTypeProperty("has" + WordUtils.capitalize(field.getName()));
            }
        }
        return data;
    }

    public static ObjectPropertyData parseObjectProperty(Field field) {
        final String dynamicProp = "ObjectProperty";
        ObjectPropertyData data = new ObjectPropertyData();
        for (Annotation annotation : field.getDeclaredAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (dynamicProp.equals(type.getSimpleName())) {
                for (Method method : type.getDeclaredMethods()) {

                    try {

                        data.setObjectProperty("has" + WordUtils.capitalize(field.getName()));

                        if (RANGE.equals(method.getName())) {
                            data.setRange((Class<?>) method.invoke(annotation, (Object[]) null));
                        }
                    } catch (IllegalAccessException e) {
                        LOGGER.error("", e);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("", e);
                    } catch (InvocationTargetException e) {
                        LOGGER.error("", e);
                    }
                }
            }
        }
        return data;
    }

    public static boolean parseInstanceIdField(Field field) {
        final String instanceId = "InstanceIdentifier";
        Class<?> value = field.getType();

        for (Annotation annotation : field.getDeclaredAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (instanceId.equals(type.getSimpleName())) {

                if (!(value.isAssignableFrom(Long.class) || value.equals(Long.TYPE))) {
                    LOGGER.info("The instance id should be of type Long");
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public static boolean parseOwlIgnoreField(Field field) {
        final String owlIgnore = "OntologyIgnore";
        for (Annotation annotation : field.getDeclaredAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (owlIgnore.equals(type.getSimpleName())) {
                return true;
            }
        }
        return false;
    }
}