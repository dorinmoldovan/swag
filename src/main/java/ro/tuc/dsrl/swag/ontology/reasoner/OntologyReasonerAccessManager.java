package ro.tuc.dsrl.swag.ontology.reasoner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import ro.tuc.dsrl.swag.annotations.ontology.DataTypeProperty;
import ro.tuc.dsrl.swag.annotations.ontology.ObjectProperty;
import ro.tuc.dsrl.swag.ontology.utility.D2RQUtility;
import ro.tuc.dsrl.swag.ontology.utility.OntologyUtility;
import ro.tuc.dsrl.swag.ontology.utility.JenaUtility;
import ro.tuc.dsrl.swag.ontology.utility.OntologyUtilityFactory;
import ro.tuc.dsrl.swag.types.ApiType;
import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.JavaType;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class OntologyReasonerAccessManager implements ReasonerAccessManager {

    private static final Logger LOGGER = Logger.getLogger(OntologyReasonerAccessManager.class);
    private static final String LIST_INTERFACE = "interface java.util.List";
    private static final String TYPE = PropertiesLoader.getProperty(ConfigurationProperties.API_TYPE);
    private static volatile OntologyReasonerAccessManager instance;
    private static String prefixes;
    private static Model ontModel;
    private OntologyUtility ontologyUtility;

    private OntologyReasonerAccessManager() {
        ontologyUtility = OntologyUtilityFactory.getInstance();
        switch (ApiType.valueOf(TYPE)) {
            case JENA: {
                prefixes = JenaUtility.getPrefixes();
                ontModel = JenaUtility.getInstance().getInfModel();
                break;
            }
            case D2RQ: {
                prefixes = D2RQUtility.getPrefixes();
                ontModel = D2RQUtility.getInstance().getJenaModel();
                break;
            }
            default: {
                LOGGER.error("The specified API type is not available.");
                break;
            }
        }
    }

    static OntologyReasonerAccessManager getInstance() {
        if (instance == null) {
            synchronized (OntologyReasonerAccessManager.class) {
                if (instance == null) {
                    instance = new OntologyReasonerAccessManager();
                }
            }
        }
        return instance;
    }

    public static String getPrefixes() {
        return prefixes;
    }

    public static void setPrefixes(String prefixes) {
        OntologyReasonerAccessManager.prefixes = prefixes;
    }

    @Override
    public <T> List<T> getIndividuals(Class<T> cls) {
        ontologyUtility.refresh();

        switch (ApiType.valueOf(TYPE)) {
            case JENA: {
                prefixes = JenaUtility.getPrefixes();
                ontModel = JenaUtility.getInstance().getInfModel();
                break;
            }
            case D2RQ: {
                prefixes = D2RQUtility.getPrefixes();
                ontModel = D2RQUtility.getInstance().getJenaModel();
                break;
            }
            default: {
                LOGGER.error("The specified API type is not available.");
                break;
            }
        }

        String className = cls.getSimpleName();
        List<String> individuals = new ArrayList<String>();

        // Get the ontology's URI

        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];

        // Get the individuals using SPARQL

        String sparqlSelect = OntologyReasonerAccessManager.prefixes
                + " SELECT ?individual WHERE {?individual rdf:type " + uri + ":" + className + "}";

        Query query = QueryFactory.create(sparqlSelect);
        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
        ResultSet results = qe.execSelect();

        while (results.hasNext()) {
            QuerySolution row = results.nextSolution();
            String individualName = row.getResource("?individual").getLocalName();
            individuals.add(individualName);
        }

        qe.close();

        List<T> objects = new ArrayList<T>();

        try {
            getIndividualsRecursive(cls, individuals, objects);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e);
        } catch (InstantiationException e) {
            LOGGER.error(e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e);
        }

        return objects;
    }

    private <T> void getIndividualsRecursive(Class<?> cls, List<String> individuals, List<T> objects)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (!individuals.isEmpty()) {
            for (String individual : individuals) {
                Class<?> c = cls;
                String className = individual;
                className = className.replaceAll("\\_{1,2}[0-9]*", "");

                if (!cls.getSimpleName().equals(className)) {
                    c = Class.forName(cls.getPackage().getName() + "." + className);
                }

                @SuppressWarnings("unchecked")
                T clsInstance = (T) c.newInstance();
                populateDataPropertyFields(c, individual, clsInstance);
                populateObjectPropertyFields(c, individual, clsInstance);
                objects.add(clsInstance);
            }
        }
    }

    public <T> void populateObjectPropertyFields(Class<?> cls, String individ, T clsInstance)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        // Get the ontology's URI

        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];

        // Get the fields

        Map<String, Class<?>> fieldsMap = new HashMap<String, Class<?>>();

        // fields annotated with @OneToOne/@OneToMany/@ManyToMany/@ManyToOne and
        // @ObjectProperty

        for (Field field : cls.getDeclaredFields()) {
            int count = 0;
            field.setAccessible(true);

            for (Annotation annot : field.getAnnotations()) {
                if (annot instanceof ObjectProperty) {
                    count++;
                }
            }
            if (count == 1) {
                String fieldLiteral = "has" + WordUtils.capitalize(field.getName());
                fieldsMap.put(fieldLiteral, field.getType());
            }
        }

        for (Map.Entry<String, Class<?>> entry : fieldsMap.entrySet()) {
            String propName = WordUtils.uncapitalize(entry.getKey().substring(3));

            List<T> nObjects = new ArrayList<T>();
            List<String> newIndiv = new ArrayList<String>();

            String sparqlSelect = OntologyReasonerAccessManager.prefixes + "SELECT ?individual WHERE {" + uri + ":"
                    + individ + " " + uri + ":" + entry.getKey() + " ?individual }";

            Query query = QueryFactory.create(sparqlSelect);
            QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
            ResultSet results = qe.execSelect();

            while (results.hasNext()) {
                QuerySolution row = results.nextSolution();
                String individualName = row.getResource("?individual").getLocalName();
                newIndiv.add(individualName);
            }

            qe.close();

            getIndividualsRecursive(cls, newIndiv, nObjects);

            Field field = getField(cls, propName);

            if (field != null) {
                if (field.getType().isInterface() && LIST_INTERFACE.equals(field.getType().toString())) {
                    field.setAccessible(true);
                    field.set(clsInstance, nObjects);
                } else {
                    field.setAccessible(true);
                    if (nObjects != null && nObjects.size() != 0) {
                        field.set(clsInstance, nObjects.get(0));
                    }
                }
            }

        }

    }

    private <T> void populateDataPropertyFields(Class<?> cls, String individual, T clsInstance)
            throws IllegalArgumentException, IllegalAccessException {

        // Get the ontology's URI

        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];

        // Get the fields

        Map<String, Class<?>> fieldsMap = new HashMap<String, Class<?>>();

        // fields annotated with @Column and @DataTypeProperty

        String fields = "";
        String sparqlValues = "";

        for (Field field : cls.getDeclaredFields()) {
            int count = 0;
            field.setAccessible(true);

            for (Annotation annot : field.getAnnotations()) {
                if (annot instanceof DataTypeProperty) {
                    count++;
                }
            }
            if (count == 1) {
                String dataTypeProperty = uri + ":has" + WordUtils.capitalize(field.getName());
                String fieldLiteral = "?" + field.getName();
                String selectValue = uri + ":" + individual + " " + dataTypeProperty + " " + fieldLiteral + " . ";
                fieldsMap.put(fieldLiteral, field.getType());
                sparqlValues += selectValue;
                fields += fieldLiteral + " ";
            }

        }

        // Get the individuals using SPARQL

        String sparqlSelect = OntologyReasonerAccessManager.prefixes + "SELECT ?id " + fields + " WHERE {" + uri + ":"
                + individual + " " + uri + ":hasId ?id . " + sparqlValues + " }";

        Query query = QueryFactory.create(sparqlSelect);
        QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
        ResultSet results = qe.execSelect();

        while (results.hasNext()) {

            QuerySolution row = results.nextSolution();
            Long id = row.getLiteral("?id").getLong();
            Field field = getField(cls, "id");
            if (field != null) {
                field.setAccessible(true);
                field.set(clsInstance, id);
            }
            for (Map.Entry<String, Class<?>> entry : fieldsMap.entrySet()) {
                String literal = entry.getKey();
                String fieldName = literal.substring(1);
                Object fieldValue = null;

                if (entry.getValue().getName().equals(JavaType.LONG_WRAPPER.value())) {
                    fieldValue = row.getLiteral(literal).getLong();
                } else if (entry.getValue().getName().equals(JavaType.INTEGER_WRAPPER.value())) {
                    fieldValue = row.getLiteral(literal).getInt();
                } else if (entry.getValue().getName().equals(JavaType.DOUBLE_WRAPPER.value())) {
                    fieldValue = row.getLiteral(literal).getDouble();
                } else if (entry.getValue().getName().equals(JavaType.FLOAT_WRAPPER.value())) {
                    fieldValue = row.getLiteral(literal).getFloat();
                } else if (entry.getValue().getName().equals(JavaType.BOOLEAN_WRAPPER.value())) {
                    fieldValue = row.getLiteral(literal).getBoolean();
                } else if (entry.getValue().getName().equals(JavaType.LONG.value())) {
                    fieldValue = row.getLiteral(literal).getLong();
                } else if (entry.getValue().getName().equals(JavaType.INT.value())) {
                    fieldValue = row.getLiteral(literal).getInt();
                } else if (entry.getValue().getName().equals(JavaType.DOUBLE.value())) {
                    fieldValue = row.getLiteral(literal).getDouble();
                } else if (entry.getValue().getName().equals(JavaType.FLOAT.value())) {
                    fieldValue = row.getLiteral(literal).getFloat();
                } else if (entry.getValue().getName().equals(JavaType.BOOLEAN.value())) {
                    fieldValue = row.getLiteral(literal).getBoolean();
                } else if (entry.getValue().getName().equals(JavaType.STRING.value())) {
                    fieldValue = row.getLiteral(literal).getString();
                } else if (entry.getValue().getName().equals(JavaType.DATE.value())) {
                    SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String dateValue = row.getLiteral(literal).getString();
                    String parsedDate = dateValue.replace('T', ' ');
                    try {
                        fieldValue = dt.parse(parsedDate);
                    } catch (ParseException e) {
                        LOGGER.error(e);
                    }
                }
                Field f = getField(cls, fieldName);
                if (f != null) {
                    f.setAccessible(true);
                    f.set(clsInstance, fieldValue);
                }
            }
        }

        qe.close();

    }

    @Override
    public <T> T getIndividual(Class<T> cls, Long id) {
        List<T> individuals = getIndividuals(cls);
        Field field = getField(cls, "id");
        field.setAccessible(true);
        for (T ind : individuals) {
            try {
                if (field.get(ind) == id) {
                    return ind;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("", e);
            } catch (IllegalAccessException e) {
                LOGGER.error("", e);
            }
        }
        return null;
    }

    private Field getField(Class<?> cls, String name) {
        Class<?> clazz = cls;
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

}
