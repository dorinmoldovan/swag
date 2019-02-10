package ro.tuc.dsrl.swag.ontology.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import ro.tuc.dsrl.swag.datamodel.OntologyClass;
import ro.tuc.dsrl.swag.datamodel.OntologyIndividual;
import ro.tuc.dsrl.swag.jr.JRRulesGenerator;
import ro.tuc.dsrl.swag.parser.OntologyClassGeneration;
import ro.tuc.dsrl.swag.parser.OntologyIndividualGeneration;
import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.D2RQPrefixes;
import ro.tuc.dsrl.swag.utility.JavaType;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class JenaUtility implements OntologyUtility {

    public static final String OWL_FILE = PropertiesLoader.getProperty(ConfigurationProperties.OWL_FILE);
    public static final String AUTOGENERATE = PropertiesLoader.getProperty(ConfigurationProperties.AUTO_GEN);
    public static final String OWL_URI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
    public static final String OWL_PREFIX = PropertiesLoader.getProperty(ConfigurationProperties.URI) + "#";
    public static final String RULES_FILE = PropertiesLoader.getProperty(ConfigurationProperties.RULES_FILE);
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd hh_mm_ss";
    private static final Logger LOGGER = Logger.getLogger(JenaUtility.class);
    private static final String THING = "Thing";
    private static final String TRUE = "true";
    private static volatile JenaUtility instance;
    private static String prefixes;
    private OntModel ontModel;
    private InfModel infModel;
    private Reasoner reasoner;

    private JenaUtility() {
        if (TRUE.equals(AUTOGENERATE)) {
            generateOntology();
            JRRulesGenerator.generateRulesFile();
        }

        loadOntology();

        reasoner = new GenericRuleReasoner(Rule.rulesFromURL(RULES_FILE));
        infModel = ModelFactory.createInfModel(reasoner, ontModel);

        prefixes = generatePrefixes();
    }

    public static JenaUtility getInstance() {
        if (instance == null) {
            synchronized (JenaUtility.class) {
                if (instance == null) {
                    instance = new JenaUtility();
                }
            }
        }
        return instance;
    }

    public static String getPrefixes() {
        return prefixes;
    }

    public static void setPrefixes(String prefixes) {
        JenaUtility.prefixes = prefixes;
    }

    public void refresh() {
        infModel = ModelFactory.createInfModel(reasoner, ontModel);
    }

    private void insertId() {
        OntClass ontClass = ontModel.getOntClass("http://www.w3.org/2002/07/owl#Thing");
        DatatypeProperty datatypeProperty = ontModel.createDatatypeProperty(JenaUtility.OWL_PREFIX + "hasId");
        datatypeProperty.setDomain(ontClass);
        datatypeProperty.setRange(XSD.xlong);
    }

    private void insertClasses(List<OntologyClass> owlClasses) {
        for (OntologyClass oc : owlClasses) {
            String className = oc.getClassName();
            String superClassName = oc.getSuperClass();
            if (!(THING.equals(superClassName))) {
                OntClass ontClass = ontModel.createClass(JenaUtility.OWL_PREFIX + className);
                OntClass ontSuperClass = ontModel.createClass(JenaUtility.OWL_PREFIX + superClassName);
                ontModel.add(ontClass, RDFS.subClassOf, ontSuperClass);
            } else {
                ontModel.createClass(JenaUtility.OWL_PREFIX + className);
            }
        }
    }

    private void insertDataTypeProperties(List<OntologyClass> owlClasses) {
        for (OntologyClass oc : owlClasses) {
            String className = oc.getClassName();

            OntClass ontClass = ontModel.getOntClass(JenaUtility.OWL_PREFIX + className);

            Iterator<Entry<String, String>> it = oc.getFields().entrySet().iterator();

            while (it.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry pair = (Map.Entry) it.next();

                DatatypeProperty datatypeProperty = ontModel
                        .createDatatypeProperty(JenaUtility.OWL_PREFIX + pair.getKey());
                datatypeProperty.setDomain(ontClass);

                String value = (String) pair.getValue();

                if (JavaType.DATE.value().equals(value)) {
                    datatypeProperty.setRange(XSD.dateTime);
                } else if (JavaType.INTEGER_WRAPPER.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xint);
                } else if (JavaType.STRING.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xstring);
                } else if (JavaType.LONG_WRAPPER.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xlong);
                } else if (JavaType.DOUBLE_WRAPPER.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xdouble);
                } else if (JavaType.FLOAT_WRAPPER.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xfloat);
                } else if (JavaType.BOOLEAN_WRAPPER.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xboolean);
                } else if (JavaType.BOOLEAN.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xboolean);
                } else if (JavaType.LONG.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xlong);
                } else if (JavaType.INT.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xint);
                } else if (JavaType.FLOAT.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xfloat);
                } else if (JavaType.DOUBLE.value().equals(value)) {
                    datatypeProperty.setRange(XSD.xdouble);
                } else {
                }

                it.remove();
            }

        }
    }

    private void insertObjectProperties(List<OntologyClass> owlClasses) {
        for (OntologyClass oc : owlClasses) {
            String className = oc.getClassName();

            OntClass ontClass = ontModel.getOntClass(JenaUtility.OWL_PREFIX + className);

            Iterator<Entry<String, String>> it = oc.getObjectProperties().entrySet().iterator();

            while (it.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry pair = (Map.Entry) it.next();

                ObjectProperty objectProperty = ontModel.createObjectProperty(JenaUtility.OWL_PREFIX + pair.getKey());
                objectProperty.setDomain(ontClass);

                String range = (String) pair.getValue();
                OntClass rangeClass = ontModel.getOntClass(JenaUtility.OWL_PREFIX + range);

                objectProperty.setRange(rangeClass);

                it.remove();
            }

        }
    }

    private void insertIndividuals(List<OntologyIndividual> owlIndividuals) {
        for (OntologyIndividual i : owlIndividuals) {
            String individualName = i.getIndividualName();
            String className = i.getClassName();
            OntClass ontClass = ontModel.getOntClass(JenaUtility.OWL_PREFIX + className);
            ontModel.createIndividual(JenaUtility.OWL_PREFIX + individualName, ontClass);
        }
    }

    public void generateOntology() {

        LOGGER.info("Generate Ontology");

        OntModelSpec spec = new OntModelSpec(PelletReasonerFactory.THE_SPEC);
        ontModel = ModelFactory.createOntologyModel(spec, null);

        ontModel.createOntology(OWL_URI);
        ontModel.setNsPrefix("", OWL_PREFIX);

        List<OntologyClass> owlClasses = OntologyClassGeneration.generateOwlClasses();

        insertId();
        insertClasses(owlClasses);
        insertDataTypeProperties(owlClasses);
        insertObjectProperties(owlClasses);

        List<OntologyIndividual> owlIndividuals = OntologyIndividualGeneration.generateOwlIndividuals();

        insertIndividuals(owlIndividuals);

        save();

    }

    public void save() {
        try {

            File file = new File(OWL_FILE);
            ontModel.write(new FileOutputStream(file));

        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public void sparql(String queryString) {
        queryString = getPrefixes() + " " + queryString;
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, infModel);
        ResultSet results = qe.execSelect();
        ResultSetFormatter.out(System.out, results, query);
        qe.close();
    }

    private void loadOntology() {
        ontModel = getDataModel();
    }

    public OntModel getDataModel() {
        OntDocumentManager dm = new OntDocumentManager();

        OntModelSpec s = new OntModelSpec(OntModelSpec.OWL_DL_MEM);
        s.setDocumentManager(dm);
        OntModel m = ModelFactory.createOntologyModel(s, null);

        InputStream in = null;

        in = FileManager.get().open(OWL_FILE);
        if (in == null) {
            LOGGER.error("", new IllegalArgumentException("File: " + OWL_FILE + " not found"));
        }

        try {
            m.read(in, "");
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        return m;
    }

    private String generatePrefixes() {
        String prefixes = "";

        for (D2RQPrefixes d2rqPrefixes : D2RQPrefixes.values()) {
            String prefix = "";
            prefix += "PREFIX ";
            prefix += d2rqPrefixes.name().toLowerCase();
            prefix += ": ";
            prefix += "<";
            prefix += d2rqPrefixes.value();
            prefix += "#> \n";
            prefixes += prefix;
        }

        String ontologyPrefix = "";
        ontologyPrefix += "PREFIX ";
        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        String[] tokens = ontologyURI.split("/");
        String uri = tokens[tokens.length - 1];
        ontologyPrefix += uri;
        ontologyPrefix += ": ";
        ontologyPrefix += "<";
        ontologyPrefix += ontologyURI;
        ontologyPrefix += "#> \n";

        prefixes += ontologyPrefix;

        return prefixes;
    }

    public OntModel getOntModel() {
        return ontModel;
    }

    public void setOntModel(OntModel ontModel) {
        this.ontModel = ontModel;
    }

    public InfModel getInfModel() {
        return infModel;
    }

    public void setInfModel(InfModel infModel) {
        this.infModel = infModel;
    }

    public Reasoner getReasoner() {
        return reasoner;
    }

    public void setReasoner(Reasoner reasoner) {
        this.reasoner = reasoner;
    }

}
