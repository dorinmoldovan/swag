package ro.tuc.dsrl.swag.jr;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class JRRule {

	private String name;
	private List<JRPredicate> predicates;

	public JRRule() {
		predicates = new ArrayList<JRPredicate>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<JRPredicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(List<JRPredicate> predicates) {
		this.predicates = predicates;
	}

}
