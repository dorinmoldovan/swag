package ro.tuc.dsrl.swag.ontology.reasoner;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class ReasonerAccessManagerFactory {

	private static volatile ReasonerAccessManager instance;

	private ReasonerAccessManagerFactory() {
	}

	public static ReasonerAccessManager getInstance() {
		if (instance == null) {
			synchronized (ReasonerAccessManagerFactory.class) {
				if (instance == null) {
					instance = OntologyReasonerAccessManager.getInstance();
				}
			}
		}
		return instance;
	}

}
