package ro.tuc.dsrl.swag.simulation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import ro.tuc.dsrl.swag.model.diagnostic.Measurements;
import ro.tuc.dsrl.swag.model.diagnostic.User;
import ro.tuc.dsrl.swag.model.diagnostic.reasoner.MeasurementsReasoner;
import ro.tuc.dsrl.swag.model.diagnostic.reasoner.UserReasoner;
import ro.tuc.dsrl.swag.model.diagnostic.repository.MeasurementsRepository;
import ro.tuc.dsrl.swag.model.diagnostic.repository.UserRepository;
import ro.tuc.dsrl.swag.ontology.utility.OntologyUtilityFactory;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public class DiagnosticOntologySimulation {

    private static final Logger LOGGER = Logger.getLogger(DiagnosticOntologySimulation.class);
    private static final String APPLICATION_CONTEXT = "src/main/webapp/WEB-INF/applicationContext.xml";

    private static final int NO_USERS = 5;

    @SuppressWarnings("resource")
    public static void main(String[] args) throws FileNotFoundException, IOException {

        new FileSystemXmlApplicationContext(APPLICATION_CONTEXT);

        OntologyUtilityFactory.getInstance();

        // Insert the data for simulation

        UserRepository userRepository = new UserRepository();
        MeasurementsRepository measurementsRepository = new MeasurementsRepository();

        Random rand = new Random();

        LOGGER.info("DATA FOR SIMULATION");

        long start = System.currentTimeMillis();

        for (int i = 1; i <= NO_USERS; i++) {
            User user = new User();
            user.setId((long) i);
            user.setFirstName("FirstName" + i);
            user.setLastName("LastName" + i);
            user.setAge(80L);
            user.setGender("male");
            userRepository.create(user);

            Measurements measurements = new Measurements();
            measurements.setId((long) i);
            int height = rand.nextInt(20) + 160;
            measurements.setHeight((long) height);
            int weight = rand.nextInt(50) + 50;
            measurements.setWeight((long) weight);
            measurements.setDate(new Date());
            measurements.setUser(user);
            measurementsRepository.create(measurements);
        }

        long end = System.currentTimeMillis();

        LOGGER.info("INSERTION TIME : " + (end - start));

        List<Measurements> measurementsList = measurementsRepository.findAll();
        for (Measurements m : measurementsList) {
            LOGGER.info(m);
        }

        List<User> usersList = userRepository.findAll();
        for (User u : usersList) {
            LOGGER.info(u);
        }

        LOGGER.info("DATA EXTRACTED FROM REPOSITORY");

        start = System.currentTimeMillis();

        measurementsList = measurementsRepository.findAll();
        usersList = userRepository.findAll();

        end = System.currentTimeMillis();

        LOGGER.info("RETRIEVAL TIME FROM REPOSITORY : " + (end - start));

        LOGGER.info("DATA EXTRACTED FROM REASONER");

        start = System.currentTimeMillis();

        // Simulate the reasoner

        MeasurementsReasoner measurementsReasoner = new MeasurementsReasoner();
        UserReasoner userReasoner = new UserReasoner();

        measurementsList = measurementsReasoner.findAll();
        usersList = userReasoner.findAll();

        end = System.currentTimeMillis();

        LOGGER.info("RETRIEVAL TIME FROM REASONER : " + (end - start));

        for (Measurements m : measurementsList) {
            LOGGER.info(m);
        }

        for (User u : usersList) {
            LOGGER.info(u);
        }

        OntologyUtilityFactory.getInstance().save();

    }

}
