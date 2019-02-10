package ro.tuc.dsrl.swag.ontology.utility;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public interface OntologyUtility {

	public void save();

	public void refresh();

	public void sparql(String queryString);

}
