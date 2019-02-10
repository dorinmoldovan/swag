package ro.tuc.dsrl.swag.model.diagnostic.rules;

import ro.tuc.dsrl.swag.annotations.jr.JRInput;
import ro.tuc.dsrl.swag.annotations.jr.JRParameter;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public interface WeightRulePrefix {

	@JRInput(value = "User")
	void method1(@JRParameter(value = "x") Object arg0);

	@JRInput(value = "Measurements")
	void method2(@JRParameter(value = "y") Object arg0);

	@JRInput(value = "hasUser")
	void method3(@JRParameter(value = "y") Object arg0, @JRParameter(value = "x") Object arg1);

	@JRInput(value = "hasBMI")
	void method4(@JRParameter(value = "y") Object arg0, @JRParameter(value = "b") Object arg1);

}
