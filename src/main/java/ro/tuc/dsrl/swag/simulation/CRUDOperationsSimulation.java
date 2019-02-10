package ro.tuc.dsrl.swag.simulation;

import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import ro.tuc.dsrl.swag.model.diagnostic.Measurements;
import ro.tuc.dsrl.swag.model.diagnostic.User;
import ro.tuc.dsrl.swag.model.diagnostic.reasoner.MeasurementsReasoner;
import ro.tuc.dsrl.swag.model.diagnostic.repository.MeasurementsRepository;
import ro.tuc.dsrl.swag.model.diagnostic.repository.UserRepository;
import ro.tuc.dsrl.swag.ontology.utility.OntologyUtilityFactory;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class CRUDOperationsSimulation {

    private static final Logger LOGGER = Logger.getLogger(CRUDOperationsSimulation.class);
    private static final String APPLICATION_CONTEXT = "src/main/webapp/WEB-INF/applicationContext.xml";

    @SuppressWarnings("resource")
    public static void main(String[] args) {

        LOGGER.info("Start");

        new FileSystemXmlApplicationContext(APPLICATION_CONTEXT);

        Random rand = new Random();

        OntologyUtilityFactory.getInstance();

        UserRepository userRepository = new UserRepository();
        // create
        User user = new User(21L, "John", "Smith", "male", 80L);
        userRepository.create(user);
        // update
        user.setAge(81L);
        userRepository.update(user);
        // find by ID
        userRepository.findByIdentifier(21L);
        // find all
        userRepository.findAll();

        MeasurementsRepository measurementsRepository = new MeasurementsRepository();

        Measurements measurements = new Measurements();
        measurements.setId(21L);
        int height = rand.nextInt(20) + 160;
        measurements.setHeight((long) height);
        int weight = rand.nextInt(50) + 50;
        measurements.setWeight((long) weight);
        measurements.setDate(new Date());
        measurements.setUser(user);
        measurementsRepository.create(measurements);

        MeasurementsReasoner measurementsReasoner = new MeasurementsReasoner();
        // find by ID
        measurementsReasoner.findByIdentifier(21L);
        // find all
        measurementsReasoner.findAll();

        // delete by ID

        measurementsRepository.delete(21L);
        userRepository.delete(21L);

        LOGGER.info("End");

    }

}
