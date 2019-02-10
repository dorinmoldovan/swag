package ro.tuc.dsrl.swag.jr;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;

import org.apache.log4j.Logger;

import ro.tuc.dsrl.swag.exceptions.FieldTypeException;
import ro.tuc.dsrl.swag.exceptions.FieldValueException;
import ro.tuc.dsrl.swag.exceptions.PropertyNameException;
import ro.tuc.dsrl.swag.parser.JRRulesParser;
import ro.tuc.dsrl.swag.utility.ConfigurationProperties;
import ro.tuc.dsrl.swag.utility.D2RQPrefixes;
import ro.tuc.dsrl.swag.utility.JavaType;
import ro.tuc.dsrl.swag.utility.PropertiesLoader;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class JRRulesGenerator {

    public static final String RULES_FILE = PropertiesLoader.getProperty(ConfigurationProperties.RULES_FILE);
    private static final Logger LOGGER = Logger.getLogger(JRRulesGenerator.class);
    private static volatile JRRulesGenerator instance;

    private JRRulesGenerator() {

    }

    public static JRRulesGenerator getInstance() {
        if (instance == null) {
            synchronized (JRRulesGenerator.class) {
                if (instance == null) {
                    instance = new JRRulesGenerator();
                }
            }
        }
        return instance;
    }

    private static String getOntologyPrefix() {
        String ontologyPrefix = "";
        ontologyPrefix += "@prefix ";
        ontologyPrefix += ": ";
        ontologyPrefix += "<";
        String ontologyURI = PropertiesLoader.getProperty(ConfigurationProperties.URI);
        ontologyPrefix += ontologyURI;
        ontologyPrefix += "#> .\r\n";
        return ontologyPrefix;
    }

    private static String getRDFPrefix() {
        String rdfPrefix = "";
        rdfPrefix += "@prefix ";
        rdfPrefix += D2RQPrefixes.RDF.name().toLowerCase();
        rdfPrefix += ": ";
        rdfPrefix += "<";
        rdfPrefix += D2RQPrefixes.RDF.value();
        rdfPrefix += "#> .\r\n";
        return rdfPrefix;
    }

    private static String generateRules(JRRulesCollection jrRulesCollection) {
        String rules = "";
        rules += getOntologyPrefix();
        rules += getRDFPrefix();
        rules += "\r\n";
        for (JRRule jrRule : jrRulesCollection.getJRRules()) {
            String rule = "";
            rule += "[";
            rule += jrRule.getName();
            rule += ":\r\n";

            // Generate the inputs and the outputs of the JR rule

            String input = "";
            String output = "";

            Collections.sort(jrRule.getPredicates());

            for (JRPredicate jrPredicate : jrRule.getPredicates()) {
                String predicate = "";

                // case 1 - if the predicate starts with an upper case letter

                if (jrPredicate.getPredicate().charAt(0) >= 'A' && jrPredicate.getPredicate().charAt(0) <= 'Z') {
                    predicate += "  ";
                    predicate += "(?";
                    predicate += jrPredicate.getFields().get(0).getValue();
                    predicate += " rdf:type :";
                    predicate += jrPredicate.getPredicate();
                    predicate += "),\r\n";
                }

                // case 2 - the predicate is from the set {lessThan,
                // ge, ...}

                for (JRMathPredicates mathPredicate : JRMathPredicates.values()) {
                    if (jrPredicate.getPredicate().equals(mathPredicate.value())
                            && (mathPredicate.equals(JRMathPredicates.PRODUCT)
                            || mathPredicate.equals(JRMathPredicates.QUOTIENT)
                            || mathPredicate.equals(JRMathPredicates.DIFFERENCE)
                            || mathPredicate.equals(JRMathPredicates.SUM))) {
                        predicate += "  ";
                        predicate += jrPredicate.getPredicate();
                        predicate += "(";

                        // add the 1st field

                        if (jrPredicate.getFields().get(0).getRange().equals(JavaType.OBJECT.value())) {
                            predicate += "?";
                            predicate += jrPredicate.getFields().get(0).getValue();
                            predicate += ", ";
                        } else {
                            predicate += jrPredicate.getFields().get(0).getValue();
                            predicate += ", ";
                        }

                        // add the 2nd field

                        if (jrPredicate.getFields().get(1).getRange().equals(JavaType.OBJECT.value())) {
                            predicate += "?";
                            predicate += jrPredicate.getFields().get(1).getValue();
                            predicate += ", ";
                        } else {
                            predicate += jrPredicate.getFields().get(1).getValue();
                            predicate += ", ";
                        }

                        // add the 3rd field

                        if (jrPredicate.getFields().get(2).getRange().equals(JavaType.OBJECT.value())) {
                            predicate += "?";
                            predicate += jrPredicate.getFields().get(2).getValue();
                        } else {
                            predicate += jrPredicate.getFields().get(2).getValue();
                        }

                        predicate += "),\r\n";
                    } else if (jrPredicate.getPredicate().equals(mathPredicate.value())) {
                        predicate += "  ";
                        predicate += jrPredicate.getPredicate();
                        predicate += "(";

                        // add the 1st field

                        if (jrPredicate.getFields().get(0).getRange().equals(JavaType.OBJECT.value())) {
                            predicate += "?";
                            predicate += jrPredicate.getFields().get(0).getValue();
                            predicate += ", ";
                        } else {
                            predicate += jrPredicate.getFields().get(0).getValue();
                            predicate += ", ";
                        }

                        // add the 2nd field

                        if (jrPredicate.getFields().get(1).getRange().equals(JavaType.OBJECT.value())) {
                            predicate += "?";
                            predicate += jrPredicate.getFields().get(1).getValue();
                        } else {
                            predicate += jrPredicate.getFields().get(1).getValue();
                        }

                        predicate += "),\r\n";
                    }
                }

                // case 3 - the name of the predicate starts with "has"

                if (jrPredicate.getPredicate().length() > 3 && jrPredicate.getPredicate().charAt(0) == 'h'
                        && jrPredicate.getPredicate().charAt(1) == 'a'
                        && jrPredicate.getPredicate().charAt(2) == 's') {
                    predicate += "  ";
                    predicate += "(";

                    // add the 1st field

                    if (jrPredicate.getFields().get(0).getRange().equals(JavaType.OBJECT.value())) {
                        predicate += "?";
                        predicate += jrPredicate.getFields().get(0).getValue();
                        predicate += " ";
                    }

                    predicate += ":";
                    predicate += jrPredicate.getPredicate();
                    predicate += " ";

                    // add the 2nd field

                    if (jrPredicate.getFields().get(1).getRange().equals(JavaType.OBJECT.value())) {
                        predicate += "?";
                        predicate += jrPredicate.getFields().get(1).getValue();
                    } else if (jrPredicate.getFields().get(1).getRange().equals(JavaType.INTEGER_WRAPPER.value())
                            || jrPredicate.getFields().get(1).getRange().equals(JavaType.INT.value())) {
                        predicate += "\"";
                        predicate += jrPredicate.getFields().get(1).getValue();
                        predicate += "\"";
                        predicate += "^^xsd:int";
                    } else if (jrPredicate.getFields().get(1).getRange().equals(JavaType.LONG_WRAPPER.value())
                            || jrPredicate.getFields().get(1).getRange().equals(JavaType.LONG.value())) {
                        predicate += "\"";
                        predicate += jrPredicate.getFields().get(1).getValue();
                        predicate += "\"";
                        predicate += "^^xsd:long";
                    } else if (jrPredicate.getFields().get(1).getRange().equals(JavaType.DOUBLE_WRAPPER.value())
                            || jrPredicate.getFields().get(1).getRange().equals(JavaType.DOUBLE.value())) {
                        predicate += "\"";
                        predicate += jrPredicate.getFields().get(1).getValue();
                        predicate += "\"";
                        predicate += "^^xsd:double";
                    } else if (jrPredicate.getFields().get(1).getRange().equals(JavaType.FLOAT_WRAPPER.value())
                            || jrPredicate.getFields().get(1).getRange().equals(JavaType.FLOAT.value())) {
                        predicate += "\"";
                        predicate += jrPredicate.getFields().get(1).getValue();
                        predicate += "\"";
                        predicate += "^^xsd:float";
                    } else if (jrPredicate.getFields().get(1).getRange().equals(JavaType.BOOLEAN_WRAPPER.value())
                            || jrPredicate.getFields().get(1).getRange().equals(JavaType.BOOLEAN.value())) {
                        predicate += "\"";
                        predicate += jrPredicate.getFields().get(1).getValue();
                        predicate += "\"";
                        predicate += "^^xsd:boolean";
                    } else if (jrPredicate.getFields().get(1).getRange().equals(JavaType.STRING.value())) {
                        predicate += "\"";
                        predicate += jrPredicate.getFields().get(1).getValue();
                        predicate += "\"";
                        predicate += "^^xsd:string";
                    } else if (jrPredicate.getFields().get(1).getRange().equals(JavaType.DATE.value())) {
                        predicate += "\"";
                        predicate += jrPredicate.getFields().get(1).getValue();
                        predicate += "\"";
                        predicate += "^^xsd:dateTime";
                    } else {
                        predicate += ":";
                        predicate += jrPredicate.getFields().get(1).getValue();
                    }

                    predicate += "),\r\n";
                }

                if (jrPredicate.getType().value().equals(JRPredicateType.INPUT.value())) {
                    input += predicate;
                } else if (jrPredicate.getType().value().equals(JRPredicateType.OUTPUT.value())) {
                    output += predicate;
                }
            }

            rule += input;
            rule += "->\r\n";
            rule += output;
            rule += "]\r\n\r\n";

            rules += rule;

        }
        return rules;
    }

    public static void generateRulesFile() {
        LOGGER.info("Generate the file which contains the rules");

        JRRulesParser jrRulesParser = new JRRulesParser();

        try {

            JRRulesCollection jrRulesCollection = jrRulesParser.generateJRRulesCollection();

            String jrRules = generateRules(jrRulesCollection);

            OutputStream os = null;
            try {
                os = new FileOutputStream(RULES_FILE);
            } catch (FileNotFoundException e) {
                LOGGER.error(e);
            }
            PrintStream printStream = new PrintStream(os);
            printStream.print(jrRules);
            printStream.close();

        } catch (PropertyNameException | FieldTypeException | FieldValueException e) {
            LOGGER.error(e.getMessage());
        }
    }

}
