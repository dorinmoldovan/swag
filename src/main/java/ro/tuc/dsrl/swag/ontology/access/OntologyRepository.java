package ro.tuc.dsrl.swag.ontology.access;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
@SuppressWarnings("unchecked")
public class OntologyRepository<T> {
	private Class<T> persistentClass;
	private OntologyAccessManager accessManager;

	public OntologyRepository() {

		this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
		accessManager = OntologyAccessManagerFactory.getInstance();
	}

	public T findByIdentifier(Long i) {
		return accessManager.getIndividual(persistentClass, i);
	}

	public List<T> findAll() {
		return accessManager.getIndividuals(persistentClass);
	}

	public void create(T t) {
		accessManager.addIndividual(t);
	}

	public void update(T t) {
		accessManager.updateIndividual(t);
	}

	public void delete(Long id) {
		accessManager.deleteIndividual(persistentClass, id);
	}
}
