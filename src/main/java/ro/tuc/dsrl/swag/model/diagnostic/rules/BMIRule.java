package ro.tuc.dsrl.swag.model.diagnostic.rules;

import ro.tuc.dsrl.swag.annotations.jr.JRDefinition;
import ro.tuc.dsrl.swag.annotations.jr.JRInput;
import ro.tuc.dsrl.swag.annotations.jr.JROutput;
import ro.tuc.dsrl.swag.annotations.jr.JRParameter;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
@JRDefinition
public interface BMIRule {

    @JRInput(value = "Measurements")
    void method1(@JRParameter(value = "x") Object arg0);

    @JRInput(value = "hasHeight")
    void method2(@JRParameter(value = "x") Object arg0, @JRParameter(value = "h") Object arg1);

    @JRInput(value = "hasWeight")
    void method3(@JRParameter(value = "x") Object arg0, @JRParameter(value = "w") Object arg1);

    @JRInput(value = "product")
    void method4(@JRParameter(value = "h") Object arg0, @JRParameter(value = "h") Object arg1, @JRParameter(value = "n") Object arg2);

    @JRInput(value = "product")
    void method5(@JRParameter(value = "n") Object arg0, @JRParameter(value = "1.0") Double arg1, @JRParameter(value = "m") Object arg2);

    @JRInput(value = "quotient")
    void method6(@JRParameter(value = "w") Object arg0, @JRParameter(value = "m") Object arg1, @JRParameter(value = "r") Object arg2);

    @JRInput(value = "product")
    void method7(@JRParameter(value = "r") Object arg0, @JRParameter(value = "10000.0") Double arg1, @JRParameter(value = "rez") Object arg2);

    @JROutput(value = "hasBMI")
    void method8(@JRParameter(value = "x") Object arg0, @JRParameter(value = "rez") Object arg1);

}
