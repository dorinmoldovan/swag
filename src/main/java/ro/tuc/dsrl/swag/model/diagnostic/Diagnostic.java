package ro.tuc.dsrl.swag.model.diagnostic;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ro.tuc.dsrl.swag.annotations.ontology.InstanceIdentifier;
import ro.tuc.dsrl.swag.annotations.ontology.OntologyEntity;
import ro.tuc.dsrl.swag.annotations.ontology.OntologyIgnore;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
@OntologyEntity
public class Diagnostic implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5055197765129857775L;

    @InstanceIdentifier
    @JsonIgnore
    private Long id;
    @OntologyIgnore
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        this.name = this.getClass().getSimpleName();
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
