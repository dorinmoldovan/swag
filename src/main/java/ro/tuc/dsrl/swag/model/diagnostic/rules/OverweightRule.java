package ro.tuc.dsrl.swag.model.diagnostic.rules;

import ro.tuc.dsrl.swag.annotations.jr.JRDefinition;
import ro.tuc.dsrl.swag.annotations.jr.JRInput;
import ro.tuc.dsrl.swag.annotations.jr.JROutput;
import ro.tuc.dsrl.swag.annotations.jr.JRParameter;
import ro.tuc.dsrl.swag.model.diagnostic.Diagnostic;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
@JRDefinition
public interface OverweightRule extends WeightRulePrefix {

	@JRInput(value = "ge")
	void method5(@JRParameter(value = "b") Object arg0, @JRParameter(value = "25.0") Double arg1);

	@JRInput(value = "lessThan")
	void method6(@JRParameter(value = "b") Object arg0, @JRParameter(value = "30.0") Double arg1);

	@JROutput(value = "hasDiagnostic")
	void method7(@JRParameter(value = "x") Object arg0, @JRParameter(value = "Overweight") Diagnostic arg1);

}
