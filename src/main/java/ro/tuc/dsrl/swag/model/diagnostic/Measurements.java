package ro.tuc.dsrl.swag.model.diagnostic;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ro.tuc.dsrl.swag.annotations.ontology.DataTypeProperty;
import ro.tuc.dsrl.swag.annotations.ontology.InstanceIdentifier;
import ro.tuc.dsrl.swag.annotations.ontology.ObjectProperty;
import ro.tuc.dsrl.swag.annotations.ontology.OntologyEntity;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
@Entity
@Table(name = "MEASUREMENTS")
@OntologyEntity
public class Measurements implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3073327268128216074L;

	@Id
	@Column(name = "MEASUREMENTS_ID")
	@InstanceIdentifier
	private Long id;

	@Column(name = "WEIGHT")
	@DataTypeProperty
	private Long weight;

	@Column(name = "HEIGHT")
	@DataTypeProperty
	private Long height;

	@Column(name = "DATE")
	@DataTypeProperty
	private Date date;

	@Transient
	@DataTypeProperty
	private Double BMI;

	@ManyToOne
	@JoinColumn(name = "USER_ID", nullable = true)
	@ObjectProperty(range = User.class)
	private User user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getWeight() {
		return weight;
	}

	public void setWeight(Long weight) {
		this.weight = weight;
	}

	public Long getHeight() {
		return height;
	}

	public void setHeight(Long height) {
		this.height = height;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Double getBMI() {
		return BMI;
	}

	public void setBMI(Double bMI) {
		BMI = bMI;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Measurements [id=" + id + ", weight=" + weight + ", height=" + height + ", date=" + date + ", BMI="
				+ BMI + ", user=" + user + "]";
	}

}
