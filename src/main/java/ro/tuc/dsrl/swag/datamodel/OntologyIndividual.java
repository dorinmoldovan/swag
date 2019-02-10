package ro.tuc.dsrl.swag.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class OntologyIndividual implements OntologyModel {
    private String className;
    private String individualName;
    private Long id;
    private Map<String, Object> params;
    private Map<String, List<RangeData>> foreignKeys;

    public OntologyIndividual(String className, Long id) {
        this.className = className;
        this.id = id;
        this.individualName = className + "_" + id;
        this.params = new HashMap<String, Object>();
        this.foreignKeys = new HashMap<String, List<RangeData>>();
    }

    @Override
    public void addField(FieldValueType data) {
        if (data == null || data.getValue() == null) {
            return;
        }
        if (!data.isId()) {
            params.put(data.getField(), data.getValue());
        } else {
            this.setId((Long) data.getValue());
        }

    }

    @Override
    public void addObjectProperty(List<ObjectPropertyValue> op) {
        if (op == null) {
            return;
        }
        for (ObjectPropertyValue value : op) {
            if (value.getValue() != null && value.getValue().getId() != null) {
                putObjectPropertyValue(value);
            }
        }
    }

    private void putObjectPropertyValue(ObjectPropertyValue op) {
        List<RangeData> domains;
        if (foreignKeys.containsKey(op.getObjectProperty())) {
            domains = foreignKeys.get(op.getObjectProperty());
        } else {
            domains = new ArrayList<RangeData>();
            foreignKeys.put(op.getObjectProperty(), domains);
        }
        domains.add(op.getValue());

    }

    public String getClassName() {
        return className;
    }

    public String getIndividualName() {
        return individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Map<String, List<RangeData>> getForeignKeys() {
        return foreignKeys;
    }

    @Override
    public String toString() {

        StringBuilder paramString = new StringBuilder();
        for (Entry<String, Object> param : params.entrySet()) {
            paramString.append("\n		");
            paramString.append(param.getKey() + " = " + param.getValue());

        }

        StringBuilder fkString = new StringBuilder();
        for (Entry<String, List<RangeData>> fk : foreignKeys.entrySet()) {
            for (RangeData d : fk.getValue()) {
                fkString.append("\n		");
                fkString.append(fk.getKey() + " = " + d.getClassName() + "  " + d.getId());
            }

        }

        return "\n	ClassName: " + className + "\n" + "	Id: " + id + "\n" + "	IndividualName: " + individualName
                + "\n" + "	Params: " + paramString.toString() + "\n" + "	ForeignKeys: " + fkString.toString();
    }

}
