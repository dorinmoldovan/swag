package ro.tuc.dsrl.swag.datamodel;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class ObjectPropertyData {
    private String objectProperty;
    private Class<?> range;

    public String getObjectProperty() {
        return objectProperty;
    }

    public void setObjectProperty(String objectProperty) {
        this.objectProperty = objectProperty;
    }

    public Class<?> getRange() {
        return range;
    }

    public void setRange(Class<?> range) {
        this.range = range;
    }

}