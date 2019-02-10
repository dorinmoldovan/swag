package ro.tuc.dsrl.swag.ontology.access;

import java.util.List;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public interface OntologyAccessManager {

    public <T> void addIndividual(T t);

    public <T> void updateIndividual(T t);

    <T> List<T> getIndividuals(Class<T> cls);

    <T> T getIndividual(Class<T> cls, Long id);

    public <T> void deleteIndividual(Class<T> cls, Long id);
}
