package ro.tuc.dsrl.swag.datamodel;

import java.util.List;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public interface OntologyModel {

    void addField(FieldValueType field);

    void addObjectProperty(List<ObjectPropertyValue> op);

}
