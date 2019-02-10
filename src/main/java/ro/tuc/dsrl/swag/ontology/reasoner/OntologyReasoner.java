package ro.tuc.dsrl.swag.ontology.reasoner;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
@SuppressWarnings("unchecked")
public class OntologyReasoner<T> {
    private Class<T> persistentClass;
    private ReasonerAccessManager accessManager;

    public OntologyReasoner() {

        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        accessManager = ReasonerAccessManagerFactory.getInstance();
    }

    public T findByIdentifier(Long i) {
        return accessManager.getIndividual(persistentClass, i);
    }

    public List<T> findAll() {
        return accessManager.getIndividuals(persistentClass);
    }

}
