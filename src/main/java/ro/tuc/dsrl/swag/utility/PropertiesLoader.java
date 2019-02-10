package ro.tuc.dsrl.swag.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @Author: Technical University of Cluj-Napoca, Romania Distributed Systems
 * Research Laboratory, http://dsrl.coned.utcluj.ro/
 */
public final class PropertiesLoader {
    private static final Logger LOGGER = Logger.getLogger(PropertiesLoader.class);
    private static final String CONFIG_FILE = "/src/main/resources/swag.config";
    private static Properties prop;

    private PropertiesLoader() {
    }

    private static void loadProperties() {
        prop = new Properties();
        try {
            final Path path = Paths.get(CONFIG_FILE);
            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                prop.load(new FileInputStream(CONFIG_FILE));
            } else {
                prop.load(PropertiesLoader.class.getResourceAsStream(CONFIG_FILE));
            }
            setHibernateProperties();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    public static String getProperty(ConfigurationProperties configProp) {
        if (prop == null) {
            loadProperties();
        }
        return prop.getProperty(configProp.value());
    }

    public static Properties getProp() {
        return prop;
    }

    public static void setProp(Properties prop) {
        PropertiesLoader.prop = prop;
    }

    private static void setHibernateProperties() {
        if (PropertiesLoader.getProperty(ConfigurationProperties.AUTO_GEN).equals("true")) {
            prop.setProperty("hibernate.hbm2ddl.auto", "create");
        } else {
            prop.setProperty("hibernate.hbm2ddl.auto", "update");
        }
        prop.setProperty("hibernate.connection.driver_class", PropertiesLoader.getProperty(ConfigurationProperties.JDBC_DRIVER_CLASS_NAME));
        prop.setProperty("hibernate.connection.url", PropertiesLoader.getProperty(ConfigurationProperties.JDBC_URL));
        prop.setProperty("hibernate.connection.username", PropertiesLoader.getProperty(ConfigurationProperties.JDBC_USERNAME));
        prop.setProperty("hibernate.connection.password", PropertiesLoader.getProperty(ConfigurationProperties.JDBC_PASSWORD));
    }
}
