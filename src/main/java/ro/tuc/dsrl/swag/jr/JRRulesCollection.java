package ro.tuc.dsrl.swag.jr;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class JRRulesCollection {

	private List<JRRule> jrRules;

	public JRRulesCollection() {
		jrRules = new ArrayList<JRRule>();
	}

	public List<JRRule> getJRRules() {
		return jrRules;
	}

	public void setJRRules(List<JRRule> srwlRules) {
		this.jrRules = srwlRules;
	}

}
