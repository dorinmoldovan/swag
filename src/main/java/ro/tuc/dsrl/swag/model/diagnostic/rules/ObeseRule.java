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
public interface ObeseRule extends WeightRulePrefix {

	@JRInput(value = "ge")
	void method5(@JRParameter(value = "b") Object arg0, @JRParameter(value = "30.0") Double arg1);

	@JROutput(value = "hasDiagnostic")
	void method6(@JRParameter(value = "x") Object arg0, @JRParameter(value = "Obese") Diagnostic arg1);

}
