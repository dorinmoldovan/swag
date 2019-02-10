package ro.tuc.dsrl.swag.model.diagnostic;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ro.tuc.dsrl.swag.annotations.ontology.DataTypeProperty;
import ro.tuc.dsrl.swag.annotations.ontology.InstanceIdentifier;
import ro.tuc.dsrl.swag.annotations.ontology.ObjectProperty;
import ro.tuc.dsrl.swag.annotations.ontology.OntologyEntity;
import ro.tuc.dsrl.swag.annotations.ontology.OntologyIgnore;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
@Entity
@Table(name = "USERS")
@OntologyEntity
public class User implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4977790369510625643L;

	@Id
	@Column(name = "USER_ID")
	@InstanceIdentifier
	private Long id;

	@Column(name = "FIRST_NAME")
	@DataTypeProperty
	private String firstName;

	@Column(name = "LAST_NAME")
	@DataTypeProperty
	private String lastName;

	@Column(name = "GENDER")
	@DataTypeProperty
	private String gender;

	@Column(name = "AGE")
	@DataTypeProperty
	private Long age;

	@OneToMany(mappedBy = "user")
	@OntologyIgnore
	@JsonIgnore
	private List<Measurements> measurements;

	@Transient
	@ObjectProperty(range = Diagnostic.class)
	private Diagnostic diagnostic;

	public User() {

	}

	public User(Long id, String firstName, String lastName, String gender, Long age) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.age = age;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Long getAge() {
		return age;
	}

	public void setAge(Long age) {
		this.age = age;
	}

	public List<Measurements> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<Measurements> measurements) {
		this.measurements = measurements;
	}

	public Diagnostic getDiagnostic() {
		return diagnostic;
	}

	public void setDiagnostic(Diagnostic diagnostic) {
		this.diagnostic = diagnostic;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", gender=" + gender
				+ ", age=" + age + ", diagnostic=" + diagnostic + "]";
	}

}
