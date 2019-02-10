package ro.tuc.dsrl.swag.model.diagnostic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ro.tuc.dsrl.swag.model.diagnostic.Measurements;
import ro.tuc.dsrl.swag.ontology.access.OntologyRepository;
import ro.tuc.dsrl.swag.ontology.controller.OntologyController;
import ro.tuc.dsrl.swag.ontology.reasoner.OntologyReasoner;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
@RestController
@RequestMapping(value = "/measurements")
public class MeasurementsController extends OntologyController<Measurements> {

	@Autowired
	public MeasurementsController(OntologyReasoner<Measurements> ontologyReasoner,
								  OntologyRepository<Measurements> ontologyRepository) {
		super(ontologyReasoner, ontologyRepository);
	}

}
