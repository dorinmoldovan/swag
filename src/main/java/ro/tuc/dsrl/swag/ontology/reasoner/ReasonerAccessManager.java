package ro.tuc.dsrl.swag.ontology.reasoner;

import java.util.List;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public interface ReasonerAccessManager {

    <T> T getIndividual(Class<T> cls, Long id);

    <T> List<T> getIndividuals(Class<T> cls);

}
