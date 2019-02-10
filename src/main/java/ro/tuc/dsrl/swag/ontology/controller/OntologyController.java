package ro.tuc.dsrl.swag.ontology.controller;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import ro.tuc.dsrl.swag.ontology.access.OntologyRepository;
import ro.tuc.dsrl.swag.ontology.reasoner.OntologyReasoner;
import ro.tuc.dsrl.swag.ontology.utility.OntologyUtilityFactory;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
@SuppressWarnings("unchecked")
public class OntologyController<T> {

    private static final Logger LOGGER = Logger.getLogger(OntologyController.class);

    private Class<T> persistentClass;
    private OntologyReasoner<T> ontologyReasoner;
    private OntologyRepository<T> ontologyRepository;

    public OntologyController(OntologyReasoner<T> ontologyReasoner, OntologyRepository<T> ontologyRepository) {
        this.ontologyReasoner = ontologyReasoner;
        this.ontologyRepository = ontologyRepository;
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @RequestMapping(value = "/getFromRepository/", method = RequestMethod.GET)
    public ResponseEntity<List<T>> listAllFromRepository() {
        LOGGER.info("Get all individuals of type " + persistentClass.getSimpleName() + " from repository");
        List<T> all = ontologyRepository.findAll();
        if (all.isEmpty()) {
            return new ResponseEntity<List<T>>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<T>>(all, HttpStatus.OK);
    }

    @RequestMapping(value = "/getFromOntologyAfterReasoning/", method = RequestMethod.GET)
    public ResponseEntity<List<T>> listAllFromOntologyAfterReasoning() {
        LOGGER.info(
                "Get all individuals of type " + persistentClass.getSimpleName() + " from ontology after reasoning");
        OntologyUtilityFactory.getInstance().refresh();
        List<T> all = ontologyReasoner.findAll();
        if (all.isEmpty()) {
            return new ResponseEntity<List<T>>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<T>>(all, HttpStatus.OK);
    }

    @RequestMapping(value = "/getFromRepositoryByID/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<T> findFromRepositoryByID(@PathVariable("id") Long id) {
        LOGGER.info("Get individual with id " + id + " and type " + persistentClass.getSimpleName() + " from repository");
        T t = ontologyRepository.findByIdentifier(id);
        if (t == null) {
            return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<T>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/getFromOntologyAfterReasoningByID/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<T> findFromOntologyAfterReasoningByID(@PathVariable("id") Long id) {
        LOGGER.info("Get individual with id " + id + " and type " + persistentClass.getSimpleName() + " from ontology after reasoning");
        OntologyUtilityFactory.getInstance().refresh();
        T t = ontologyReasoner.findByIdentifier(id);
        if (t == null) {
            return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<T>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/create/", method = RequestMethod.POST)
    public ResponseEntity<Void> createIndividual(@RequestBody T t, UriComponentsBuilder ucBuilder) {

        Long id = 0L;

        try {
            Field field = t.getClass().getDeclaredField("id");
            field.setAccessible(true);
            Object value = field.get(t);
            id = (Long) value;
        } catch (NoSuchFieldException e) {
            LOGGER.error(e);
        } catch (SecurityException e) {
            LOGGER.error(e);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e);
        }

        LOGGER.info("Create a new individual with id " + id);

        if (ontologyRepository.findByIdentifier(id) != null) {
            LOGGER.info("A an individual with id " + id + " already exists");
            return new ResponseEntity<Void>(HttpStatus.CONFLICT);
        }

        ontologyRepository.create(t);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/create/{id}").buildAndExpand(id).toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
    public ResponseEntity<T> update(@PathVariable("id") Long id, @RequestBody T t) {
        LOGGER.info("Update individual with id " + id + " and type " + persistentClass.getSimpleName());
        T currentT = ontologyRepository.findByIdentifier(id);
        if (currentT == null) {
            LOGGER.info("Individual with id " + id + " not found");
            return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
        }

        ontologyRepository.update(t);

        return new ResponseEntity<T>(t, HttpStatus.OK);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<T> deleteUser(@PathVariable("id") Long id) {
        LOGGER.info("Fetching and deleting individual with id " + id);
        T t = ontologyRepository.findByIdentifier(id);
        if (t == null) {
            LOGGER.info("Unable to delete. Individual with id " + id + " not found");
            return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
        }

        ontologyRepository.delete(id);

        return new ResponseEntity<T>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/save/", method = RequestMethod.GET)
    public void save() {
        OntologyUtilityFactory.getInstance().save();
    }

}
