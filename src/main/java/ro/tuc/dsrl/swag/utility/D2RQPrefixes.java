package ro.tuc.dsrl.swag.utility;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public enum D2RQPrefixes {

    D2RQ("http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1"),
    RDFS("http://www.w3.org/2000/01/rdf-schema"),
    XSD("http://www.w3.org/2001/XMLSchema"),
    JDBC("http://d2rq.org/terms/jdbc/"),
    RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns"),
    VOCAB("vocab/"),
    OWL("http://www.w3.org/2002/07/owl"),
    DC("http://purl.org/dc/elements/1.1/"),
    DCTERMS("http://purl.org/dc/terms/"),
    FOAF("http://xmlns.com/foaf/0.1/"),
    SKOS("http://www.w3.org/2004/02/skos/core"),
    ISWC("http://annotation.semanticweb.org/iswc/iswc.daml"),
    VCARD("http://www.w3.org/2001/vcard-rdf/3.0");

    private String value;

    D2RQPrefixes(String prefix) {
        this.value = prefix;
    }

    public String value() {
        return value;
    }
}
