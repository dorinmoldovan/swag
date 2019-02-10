package ro.tuc.dsrl.swag.jr;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public enum JRPredicateType {

	DEFAULT("DEFAULT"), INPUT("INPUT"), OUTPUT("OUTPUT");

	private String value;

	private JRPredicateType(String type) {
		this.value = type;
	}

	public String value() {
		return value;
	}

}
