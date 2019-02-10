package ro.tuc.dsrl.swag.datamodel;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class ObjectPropertyValue {
    private String objectProperty;
    private RangeData value;

    public ObjectPropertyValue(String objectProperty, RangeData value) {
        this.objectProperty = objectProperty;
        this.value = value;
    }

    public String getObjectProperty() {
        return objectProperty;
    }

    public void setObjectProperty(String objectProperty) {
        this.objectProperty = objectProperty;
    }

    public RangeData getValue() {
        return value;
    }

    public void setValue(RangeData value) {
        this.value = value;
    }

}
