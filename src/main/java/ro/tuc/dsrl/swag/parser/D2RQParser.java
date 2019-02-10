package ro.tuc.dsrl.swag.parser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;
import org.reflections.Reflections;

import ro.tuc.dsrl.swag.annotations.ontology.DataTypeProperty;
import ro.tuc.dsrl.swag.annotations.ontology.ObjectProperty;
import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.D2RQPrefixes;
import ro.tuc.dsrl.swag.utility.JavaType;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class D2RQParser {

    private static final Logger LOGGER = Logger.getLogger(D2RQParser.class);
    private static final String SEPARATOR = "#######################################################################################################################\n\n";
    private static final String SPACE = "    ";
    private static final String PACKAGE = PropertiesLoader.getProperty(ConfigurationProperties.ENTITIES_PACKAGE);
    private static final String AUTO = PropertiesLoader.getProperty(ConfigurationProperties.AUTO_GEN);

    private D2RQParser() {
    }

    private static String getOntologyPrefix() {
        String ontologyPrefix = "";
        ontologyPrefix += "@prefix ";
        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];
        ontologyPrefix += uri;
        ontologyPrefix += ": ";
        ontologyPrefix += "<";
        ontologyPrefix += ontologyURI;
        ontologyPrefix += "#> .\n";
        return ontologyPrefix;
    }

    private static String generateD2RQPrefixes() {
        String prefixes = "";

        prefixes += "@prefix map: <#> .\n";

        for (D2RQPrefixes d2rqPrefixes : D2RQPrefixes.values()) {
            String prefix = "";
            prefix += "@prefix ";
            prefix += d2rqPrefixes.name().toLowerCase();
            prefix += ": ";
            prefix += "<";
            prefix += d2rqPrefixes.value();
            prefix += "#> .\n";
            prefixes += prefix;
        }

        prefixes += getOntologyPrefix();
        prefixes += "\n";

        return prefixes;
    }

    private static String generateDBConnnection() {
        String connection = "";

        connection += "map:database a d2rq:Database;\n";
        connection += SPACE + "d2rq:jdbcDSN \"" + PropertiesLoader.getProperty(ConfigurationProperties.JDBC_URL)
                + "\";\n";
        connection += SPACE + "d2rq:jdbcDriver \""
                + PropertiesLoader.getProperty(ConfigurationProperties.JDBC_DRIVER_CLASS_NAME) + "\";\n";
        connection += SPACE + "d2rq:username \"" + PropertiesLoader.getProperty(ConfigurationProperties.JDBC_USERNAME)
                + "\";\n";
        connection += SPACE + "d2rq:password \"" + PropertiesLoader.getProperty(ConfigurationProperties.JDBC_PASSWORD)
                + "\";\n";
        connection += SPACE + "jdbc:autoReconnect \"true\";\n";
        connection += SPACE + "jdbc:zeroDateTimeBehavior \"convertToNull\";\n";
        connection += SPACE + ".\n\n";

        return connection;
    }

    private static List<String> generateClasses() {
        List<String> classes = new ArrayList<String>();

        Reflections reflections = new Reflections(PACKAGE);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Entity.class);

        for (Class<?> c : annotated) {
            classes.add(generateClass(c));
        }

        return classes;
    }

    private static String generateClass(Class<?> clazz) {
        String d2rqClass = "";
        String tableName = "";
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof javax.persistence.Table) {
                tableName = ((javax.persistence.Table) annotation).name();
            }
        }

        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];

        // the mapping of the class

        d2rqClass += "#Table " + tableName + "\n";
        d2rqClass += "map:" + clazz.getSimpleName().toLowerCase() + " a d2rq:ClassMap;\n";
        d2rqClass += SPACE + "d2rq:dataStorage map:database;\n";

        // the mapping of the id

        Field[] fields = clazz.getDeclaredFields();

        String columnId = "";

        for (Field field : fields) {
            field.setAccessible(true);
            Annotation[] fieldAnnotations = field.getAnnotations();
            for (Annotation annotation : fieldAnnotations) {
                if (annotation instanceof Id) {
                    // extract the name of the id
                    for (Annotation annot : fieldAnnotations)
                        if (annot instanceof Column) {
                            columnId = ((Column) annot).name();
                        }
                }
            }
            field.setAccessible(false);
        }

        d2rqClass += SPACE + "d2rq:uriPattern \"" + ontologyURI + "#" + clazz.getSimpleName() + "_@@" + tableName + "."
                + columnId + "@@\";\n";
        d2rqClass += SPACE + "d2rq:class " + uri + ":" + clazz.getSimpleName() + ";\n";
        d2rqClass += SPACE + ".\n\n";

        d2rqClass += "#DataTypeProperty hasId\n";
        d2rqClass += "map:" + "has" + clazz.getSimpleName() + "Id a d2rq:PropertyBridge;\n";
        d2rqClass += SPACE + "d2rq:belongsToClassMap map:" + clazz.getSimpleName().toLowerCase() + ";\n";
        d2rqClass += SPACE + "d2rq:property " + uri + ":hasId;\n";
        d2rqClass += SPACE + "d2rq:column \"" + tableName + "." + columnId + "\";\n";
        d2rqClass += SPACE + "d2rq:datatype xsd:long;\n";
        d2rqClass += SPACE + ".\n\n";

        // generate the DataTypeProperties

        d2rqClass += generateDataTypeProperties(clazz);

        // generate the ObjectProperties

        d2rqClass += generateObjectProperties(clazz);

        return d2rqClass;
    }

    private static String generateDataTypeProperties(Class<?> clazz) {
        String dataTypeProperties = "";

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Annotation[] annotations = field.getAnnotations();
            int flag = 0;
            for (Annotation annotation : annotations) {
                if (annotation instanceof DataTypeProperty) {
                    flag++;
                }
                if (annotation instanceof Column) {
                    flag++;
                }
            }
            if (flag == 2)
                dataTypeProperties += generateDataTypeProperty(clazz, field);
            field.setAccessible(false);
        }

        return dataTypeProperties;
    }

    private static String generateDataTypeProperty(Class<?> clazz, Field field) {
        String dataTypeProperty = "";
        String dataTypePropertyValue = "has" + WordUtils.capitalize(field.getName());
        dataTypeProperty += "#DataTypeProperty " + dataTypePropertyValue + "\n";
        dataTypeProperty += "map:" + dataTypePropertyValue + " a d2rq:PropertyBridge;\n";
        dataTypeProperty += SPACE + "d2rq:belongsToClassMap map:" + clazz.getSimpleName().toLowerCase() + ";\n";

        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];

        dataTypeProperty += SPACE + "d2rq:property " + uri + ":" + dataTypePropertyValue + ";\n";

        // get the name of the table

        String tableName = "";
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof javax.persistence.Table) {
                tableName = ((javax.persistence.Table) annotation).name();
            }
        }

        // get the name of the column

        String columnName = "";

        Annotation[] fieldAnnotations = field.getAnnotations();
        for (Annotation annotation : fieldAnnotations) {
            if (annotation instanceof Column) {
                columnName = ((Column) annotation).name();
            }
        }

        dataTypeProperty += SPACE + "d2rq:column \"" + tableName + "." + columnName + "\";\n";

        // get the type of the field

        String range = field.getType().getName();

        dataTypeProperty += SPACE + "d2rq:datatype xsd:";

        String dataType = "";

        if (range.equals(JavaType.DOUBLE_WRAPPER.value())) {
            dataType = "double";
        }
        if (range.equals(JavaType.STRING.value())) {
            dataType = "string";
        }
        if (range.equals(JavaType.INTEGER_WRAPPER.value())) {
            dataType = "int";
        }
        if (range.equals(JavaType.LONG_WRAPPER.value())) {
            dataType = "long";
        }
        if (range.equals(JavaType.BOOLEAN_WRAPPER.value())) {
            dataType = "boolean";
        }
        if (range.equals(JavaType.FLOAT_WRAPPER.value())) {
            dataType = "float";
        }
        if (range.equals(JavaType.DATE.value())) {
            dataType = "dateTime";
        }

        dataTypeProperty += dataType;
        dataTypeProperty += ";\n";
        dataTypeProperty += SPACE + ".\n";
        dataTypeProperty += "\n";

        return dataTypeProperty;
    }

    private static String generateObjectProperties(Class<?> clazz) {
        String objectProperties = "";

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Annotation[] annotations = field.getAnnotations();
            int flag = 0;
            for (Annotation annotation : annotations) {
                if (annotation instanceof ObjectProperty) {
                    flag++;
                }
                if (annotation instanceof JoinColumn) {
                    flag++;
                }
            }
            if (flag == 2)
                objectProperties += generateObjectProperty(clazz, field);
            field.setAccessible(false);
        }

        return objectProperties;
    }

    private static String generateObjectProperty(Class<?> clazz, Field field) {
        String objectProperty = "";
        String objectPropertyValue = "has" + WordUtils.capitalize(field.getName());
        objectProperty += "#ObjectProperty " + objectPropertyValue + "\n";
        objectProperty += "map:" + objectPropertyValue + " a d2rq:PropertyBridge;\n";
        objectProperty += SPACE + "d2rq:belongsToClassMap map:" + clazz.getSimpleName().toLowerCase() + ";\n";

        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];

        objectProperty += SPACE + "d2rq:property " + uri + ":" + objectPropertyValue + ";\n";
        objectProperty += SPACE + "d2rq:refersToClassMap map:" + field.getType().getSimpleName().toLowerCase() + ";\n";

        // get the name of the first table

        String tableName1 = "";
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof javax.persistence.Table) {
                tableName1 = ((javax.persistence.Table) annotation).name();
            }
        }

        // get the id of the first table

        String id1 = "";

        Field[] fields = clazz.getDeclaredFields();

        for (Field f : fields) {
            f.setAccessible(true);
            Annotation[] fieldAnnotations = f.getAnnotations();
            for (Annotation annotation : fieldAnnotations) {
                if (annotation instanceof Id) {
                    for (Annotation annot : fieldAnnotations) {
                        if (annot instanceof Column) {
                            id1 = ((Column) annot).name();
                        }
                    }
                }
            }
            f.setAccessible(false);
        }

        objectProperty += SPACE + "d2rq:join \"" + tableName1 + "." + id1 + " = " + tableName1 + "_2." + id1 + "\";\n";

        // get the name of the second table

        String tableName2 = "";
        annotations = field.getType().getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof javax.persistence.Table) {
                tableName2 = ((javax.persistence.Table) annotation).name();
            }
        }

        // get the id of the second table

        String id2 = "";

        fields = field.getType().getDeclaredFields();

        for (Field f : fields) {
            f.setAccessible(true);
            Annotation[] fieldAnnotations = f.getAnnotations();
            for (Annotation annotation : fieldAnnotations) {
                if (annotation instanceof Id) {
                    for (Annotation annot : fieldAnnotations) {
                        if (annot instanceof Column) {
                            id2 = ((Column) annot).name();
                        }
                    }
                }
            }
            f.setAccessible(false);
        }

        objectProperty += SPACE + "d2rq:join \"" + tableName1 + "." + id2 + " = " + tableName2 + "." + id2 + "\";\n";

        objectProperty += SPACE + "d2rq:alias \"" + tableName1 + " as " + tableName1 + "_2\";\n";

        objectProperty += SPACE + ".\n";
        objectProperty += "\n";

        objectProperty += "#DataTypeProperty has" + WordUtils.capitalize(field.getName()) + "Id\n";
        objectProperty += "map:" + "has" + WordUtils.capitalize(field.getName()) + "IdFK a d2rq:PropertyBridge;\n";
        objectProperty += SPACE + "d2rq:belongsToClassMap map:" + clazz.getSimpleName().toLowerCase() + ";\n";
        objectProperty += SPACE + "d2rq:property " + uri + ":has" + WordUtils.capitalize(field.getName()) + "Id;\n";
        objectProperty += SPACE + "d2rq:column \"" + tableName1 + "." + id2 + "\";\n";
        objectProperty += SPACE + "d2rq:datatype xsd:long;\n";
        objectProperty += SPACE + ".\n\n";

        return objectProperty;
    }

    private static String generateTTLFileContent() {
        String d2rq = "";
        d2rq += generateD2RQPrefixes();
        d2rq += SEPARATOR;
        d2rq += generateDBConnnection();
        d2rq += SEPARATOR;
        List<String> classes = generateClasses();
        for (String clazz : classes) {
            d2rq += clazz;
            d2rq += SEPARATOR;
        }
        return d2rq;
    }

    public static void generateMappingFile() {
        if (AUTO == null || AUTO.equals("false")) {
            return;
        }

        String d2rq = generateTTLFileContent();

        OutputStream os = null;
        try {
            os = new FileOutputStream(PropertiesLoader.getProperty(ConfigurationProperties.TTL_FILE));
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        }
        PrintStream printStream = new PrintStream(os);
        printStream.print(d2rq);
        printStream.close();

    }
}
