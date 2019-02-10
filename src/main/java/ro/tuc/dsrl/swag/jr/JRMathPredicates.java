package ro.tuc.dsrl.swag.jr;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public enum JRMathPredicates {

	QUOTIENT("quotient"),
	PRODUCT("product"),
	GE("ge"),
	LE("le"),
	LESS_THAN("lessThan"),
	GREATER_THAN("greaterThan"),
	DIFFERENCE("difference"),
	SUM("sum");

	private String value;

	private JRMathPredicates(String method) {
		this.value = method;
	}

	public String value() {
		return value;
	}

}
