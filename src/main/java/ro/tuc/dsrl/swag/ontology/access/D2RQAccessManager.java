package ro.tuc.dsrl.swag.ontology.access;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import de.fuberlin.wiwiss.d2rq.D2RQException;
import ro.tuc.dsrl.swag.annotations.ontology.DataTypeProperty;
import ro.tuc.dsrl.swag.annotations.ontology.ObjectProperty;
import ro.tuc.dsrl.swag.datamodel.OntologyIndividual;
import ro.tuc.dsrl.swag.datamodel.RangeData;
import ro.tuc.dsrl.swag.ontology.utility.D2RQUtility;
import ro.tuc.dsrl.swag.ontology.utility.OntologyUtilityFactory;
import ro.tuc.dsrl.swag.parser.EntityReflectionParser;
import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.JavaType;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class D2RQAccessManager implements OntologyAccessManager {

    private static final Logger LOGGER = Logger.getLogger(D2RQAccessManager.class);

    private static final String LIST_INTERFACE = "interface java.util.List";
    private static volatile D2RQAccessManager instance;
    private D2RQUtility d2rqUtility;

    private D2RQAccessManager() {
        d2rqUtility = (D2RQUtility) OntologyUtilityFactory.getInstance();
    }

    static D2RQAccessManager getInstance() {
        if (instance == null) {
            synchronized (D2RQAccessManager.class) {
                if (instance == null) {
                    instance = new D2RQAccessManager();
                }
            }
        }
        return instance;
    }

    @Override
    public <T> void addIndividual(T t) {
        OntologyIndividual data = EntityReflectionParser.getOntologyData(t);

        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];

        String sparqlInsert = "INSERT DATA { " + " " + uri + ":" + data.getClassName() + "_" + data.getId() + " ";

        for (Map.Entry<String, Object> entry : data.getParams().entrySet()) {
            String dataProperty = entry.getKey();
            Object value = entry.getValue();

            if (value.getClass().getName().equals(JavaType.DOUBLE_WRAPPER.value())) {
                value = "'" + value + "'^^xsd:double";
            }
            if (value.getClass().getName().equals(JavaType.STRING.value())) {
                value = "'" + value + "'^^xsd:string";
            }
            if (value.getClass().getName().equals(JavaType.INTEGER_WRAPPER.value())) {
                value = "'" + value + "'^^xsd:int";
            }
            if (value.getClass().getName().equals(JavaType.LONG_WRAPPER.value())) {
                value = "'" + value + "'^^xsd:long";
            }
            if (value.getClass().getName().equals(JavaType.BOOLEAN_WRAPPER.value())) {
                value = "'" + value + "'^^xsd:boolean";
            }
            if (value.getClass().getName().equals(JavaType.FLOAT_WRAPPER.value())) {
                value = "'" + value + "'^^xsd:float";
            }
            if (value.getClass().getName().equals(JavaType.DATE.value())) {
                SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
                String dateStringValue1 = dt1.format((Date) value);

                SimpleDateFormat dt2 = new SimpleDateFormat("hh:mm:ss");
                String dateStringValue2 = dt2.format((Date) value);

                value = "'" + dateStringValue1 + "T" + dateStringValue2 + "'^^xsd:dateTime";
            }

            String insertValue = uri + ":" + dataProperty + " " + value + " ; ";
            sparqlInsert += insertValue;

        }

        for (Map.Entry<String, List<RangeData>> entry : data.getForeignKeys().entrySet()) {

            for (RangeData rangeData : entry.getValue()) {
                String individualClassName = rangeData.getClassName();
                long individualId = rangeData.getId();

                String insertValue = uri + ":" + "has" + individualClassName + "Id '" + individualId + "'^^xsd:long ; ";
                sparqlInsert += insertValue;

            }

        }

        sparqlInsert += "}";

        UpdateRequest request = UpdateFactory.create(D2RQUtility.getPrefixes() + sparqlInsert);
        UpdateAction.execute(request, D2RQUtility.getInstance().getD2rData());

    }

    @Override
    public <T> void updateIndividual(T t) {
        // Get the ontology's URI

        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];

        OntologyIndividual data = EntityReflectionParser.getOntologyData(t);
        Long id = data.getId();

        @SuppressWarnings("unchecked")
        T oldT = (T) getIndividual(t.getClass(), id);

        String individualName = uri + ":" + t.getClass().getSimpleName() + "_" + id;

        String sparqlNewData = "";
        String sparqlOldData = "";

        if (t != null) {

            for (Field field : t.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                try {
                    if (field.get(t) != null && !field.getName().equals("id")) {
                        String sparqlOldInformation = "";
                        String sparqlNewInformation = "";
                        String sparqlPredicate = uri + ":" + "has" + WordUtils.capitalize(field.getName());

                        if (field.getType().getName().equals(JavaType.LONG_WRAPPER.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:long . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:long . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.LONG.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:long . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:long . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.INTEGER_WRAPPER.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:int . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:int . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.INT.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:int . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:int . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.DOUBLE_WRAPPER.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:double . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:double . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.DOUBLE.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:double . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:double . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.FLOAT_WRAPPER.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:float . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:float . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.FLOAT.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:float . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:float . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.BOOLEAN_WRAPPER.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:boolean . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:boolean . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.BOOLEAN.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:boolean . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:boolean . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.STRING.value())) {
                            String sparqlNewValue = "'" + field.get(t) + "'^^xsd:string . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;
                            String sparqlOldValue = "'" + field.get(oldT) + "'^^xsd:string . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else if (field.getType().getName().equals(JavaType.DATE.value())) {

                            SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
                            String dateStringValue1 = dt1.format((Date) field.get(t));

                            SimpleDateFormat dt2 = new SimpleDateFormat("hh:mm:ss");
                            String dateStringValue2 = dt2.format((Date) field.get(t));
                            String sparqlNewValue = "'" + dateStringValue1 + "T" + dateStringValue2 + "'^^xsd:dateTime . ";

                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + sparqlNewValue;

                            dateStringValue1 = dt1.format((Date) field.get(oldT));
                            dateStringValue2 = dt2.format((Date) field.get(oldT));

                            String sparqlOldValue = "'" + dateStringValue1 + "T" + dateStringValue2 + "'^^xsd:dateTime . ";

                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + sparqlOldValue;
                        } else {
                            String secondIndividual = field.getType().getSimpleName();
                            long secondIndividualId = 0;

                            for (Field f : field.getType().getDeclaredFields()) {
                                f.setAccessible(true);
                                if (f.get(field.get(t)) != null) {
                                    if (f.getName().equals("id")) {
                                        secondIndividualId = (long) f.get(field.get(t));
                                    }
                                }
                            }

                            String secondIndividualName = uri + ":" + secondIndividual + "_" + secondIndividualId
                                    + " . ";
                            sparqlNewInformation = individualName + " " + sparqlPredicate + " " + secondIndividualName;

                            for (Field f : field.getType().getDeclaredFields()) {
                                f.setAccessible(true);
                                if (f.get(field.get(oldT)) != null) {
                                    if (f.getName().equals("id")) {
                                        secondIndividualId = (long) f.get(field.get(oldT));
                                    }
                                }
                            }

                            secondIndividualName = uri + ":" + secondIndividual + "_" + secondIndividualId
                                    + " . ";
                            sparqlOldInformation = individualName + " " + sparqlPredicate + " " + secondIndividualName;
                        }

                        sparqlNewData += sparqlNewInformation;
                        sparqlOldData += sparqlOldInformation;

                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.error(e);
                } catch (IllegalAccessException e) {
                    LOGGER.error(e);
                }
            }
        }

        String sparqlUpdate = " MODIFY DELETE " + "{ " + sparqlOldData + "} " + " INSERT " + "{ " + sparqlNewData + " }";

        try {
            UpdateRequest request = UpdateFactory.create(D2RQUtility.getPrefixes() + sparqlUpdate);
            UpdateAction.execute(request, D2RQUtility.getInstance().getD2rData());
        } catch (D2RQException e) {
            LOGGER.error(e);
        }

    }

    @Override
    public <T> List<T> getIndividuals(Class<T> cls) {
        String className = cls.getSimpleName();
        List<String> individuals = new ArrayList<String>();

        // Get the ontology's URI

        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];

        // Get the individuals using SPARQL

        String sparqlSelect = D2RQUtility.getPrefixes() + " SELECT ?individual WHERE {?individual rdf:type " + uri + ":"
                + className + "}";

        Query query = QueryFactory.create(sparqlSelect);
        QueryExecution qe = QueryExecutionFactory.create(query, D2RQUtility.getInstance().getD2rData());
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
                if (annot instanceof OneToOne) {
                    count++;
                }
                if (annot instanceof OneToMany) {
                    count++;
                }
                if (annot instanceof ManyToOne) {
                    count++;
                }
                if (annot instanceof ManyToMany) {
                    count++;
                }
                if (annot instanceof ObjectProperty) {
                    count++;
                }
            }
            if (count == 2) {
                String fieldLiteral = "has" + WordUtils.capitalize(field.getName());
                fieldsMap.put(fieldLiteral, field.getType());
            }
        }

        for (Map.Entry<String, Class<?>> entry : fieldsMap.entrySet()) {
            String propName = WordUtils.uncapitalize(entry.getKey().substring(3));

            List<T> nObjects = new ArrayList<T>();
            List<String> newIndiv = new ArrayList<String>();

            String sparqlSelect = D2RQUtility.getPrefixes() + "SELECT ?individual WHERE {" + uri + ":" + individ + " "
                    + uri + ":" + entry.getKey() + " ?individual }";

            Query query = QueryFactory.create(sparqlSelect);
            QueryExecution qe = QueryExecutionFactory.create(query, D2RQUtility.getInstance().getD2rData());
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
                    field.set(clsInstance, nObjects.get(0));
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
                if (annot instanceof Column) {
                    count++;
                }
                if (annot instanceof DataTypeProperty) {
                    count++;
                }
            }
            if (count == 2) {
                String dataTypeProperty = uri + ":has" + WordUtils.capitalize(field.getName());
                String fieldLiteral = "?" + field.getName();
                String selectValue = uri + ":" + individual + " " + dataTypeProperty + " " + fieldLiteral + " . ";
                fieldsMap.put(fieldLiteral, field.getType());
                sparqlValues += selectValue;
                fields += fieldLiteral + " ";
            }

        }

        // Get the individuals using SPARQL

        String sparqlSelect = D2RQUtility.getPrefixes() + "SELECT ?id " + fields + " WHERE {" + uri + ":" + individual
                + " " + uri + ":hasId ?id . " + sparqlValues + " }";

        Query query = QueryFactory.create(sparqlSelect);
        QueryExecution qe = QueryExecutionFactory.create(query, D2RQUtility.getInstance().getD2rData());
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

    @Override
    public <T> void deleteIndividual(Class<T> cls, Long id) {

        // Get the ontology's URI

        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];

        T t = getIndividual(cls, id);

        String individualName = uri + ":" + cls.getSimpleName() + "_" + id;

        String sparqlBody = "";

        if (t != null) {

            for (Field field : t.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                try {
                    if (field.get(t) != null) {
                        String sparqlRow = "";
                        String sparqlPredicate = uri + ":" + "has" + WordUtils.capitalize(field.getName());

                        if (field.getType().getName().equals(JavaType.LONG_WRAPPER.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:long . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.LONG.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:long . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.INTEGER_WRAPPER.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:int . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.INT.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:int . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.DOUBLE_WRAPPER.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:double . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.DOUBLE.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:double . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.FLOAT_WRAPPER.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:float . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.FLOAT.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:float . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.BOOLEAN_WRAPPER.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:boolean . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.BOOLEAN.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:boolean . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.STRING.value())) {
                            String sparqlValue = "'" + field.get(t) + "'^^xsd:string . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else if (field.getType().getName().equals(JavaType.DATE.value())) {

                            SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
                            String dateStringValue1 = dt1.format((Date) field.get(t));

                            SimpleDateFormat dt2 = new SimpleDateFormat("hh:mm:ss");
                            String dateStringValue2 = dt2.format((Date) field.get(t));
                            String sparqlValue = "'" + dateStringValue1 + "T" + dateStringValue2 + "'^^xsd:dateTime . ";

                            sparqlRow = individualName + " " + sparqlPredicate + " " + sparqlValue;
                        } else {
                            String secondIndividual = field.getType().getSimpleName();
                            long secondIndividualId = 0;

                            for (Field f : field.getType().getDeclaredFields()) {
                                f.setAccessible(true);
                                if (f.get(field.get(t)) != null) {
                                    if (f.getName().equals("id")) {
                                        secondIndividualId = (long) f.get(field.get(t));
                                    }
                                }
                            }

                            String secondIndividualName = uri + ":" + secondIndividual + "_" + secondIndividualId
                                    + " . ";
                            sparqlRow = individualName + " " + sparqlPredicate + " " + secondIndividualName;
                        }

                        sparqlBody += sparqlRow;

                    }
                } catch (IllegalArgumentException e) {
                    LOGGER.error(e);
                } catch (IllegalAccessException e) {
                    LOGGER.error(e);
                }
            }
        }

        String sparqlUpdate = " DELETE " + "{ " + sparqlBody + "} ";

        try {
            UpdateRequest request = UpdateFactory.create(D2RQUtility.getPrefixes() + sparqlUpdate);
            UpdateAction.execute(request, D2RQUtility.getInstance().getD2rData());
        } catch (D2RQException e) {
            LOGGER.error(e);
        }

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

    public D2RQUtility getD2rqUtility() {
        return d2rqUtility;
    }

    public void setD2rqUtility(D2RQUtility d2rqUtility) {
        this.d2rqUtility = d2rqUtility;
    }

}
