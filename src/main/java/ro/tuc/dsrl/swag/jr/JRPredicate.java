package ro.tuc.dsrl.swag.jr;

import java.util.List;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class JRPredicate implements Comparable<JRPredicate> {

    private JRPredicateType type;
    private String predicate;
    private List<JRField> fields;
    private int index;

    public JRPredicate(JRPredicateType type, String predicate, List<JRField> fields, int index) {
        this.type = type;
        this.predicate = predicate;
        this.fields = fields;
        this.index = index;
    }

    public JRPredicateType getType() {
        return type;
    }

    public void setType(JRPredicateType type) {
        this.type = type;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public List<JRField> getFields() {
        return fields;
    }

    public void setFields(List<JRField> fields) {
        this.fields = fields;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int compareTo(JRPredicate o) {
        int compareIndex = ((JRPredicate) o).getIndex();
        return this.index - compareIndex;
    }

}
