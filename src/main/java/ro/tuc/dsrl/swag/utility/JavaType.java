package ro.tuc.dsrl.swag.utility;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public enum JavaType {

    INTEGER_WRAPPER("java.lang.Integer"),
    LONG_WRAPPER("java.lang.Long"),
    DOUBLE_WRAPPER("java.lang.Double"),
    FLOAT_WRAPPER("java.lang.Float"),
    BOOLEAN_WRAPPER("java.lang.Boolean"),
    BOOLEAN("boolean"),
    LONG("long"),
    INT("int"),
    FLOAT("float"),
    DOUBLE("double"),
    STRING("java.lang.String"),
    OBJECT("java.lang.Object"),
    DATE("java.util.Date");

    private String value;

    JavaType(String type) {
        this.value = type;
    }

    public String value() {
        return value;
    }

}
