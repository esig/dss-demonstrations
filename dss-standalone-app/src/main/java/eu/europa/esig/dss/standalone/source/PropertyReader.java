package eu.europa.esig.dss.standalone.source;

import eu.europa.esig.dss.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class PropertyReader {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyReader.class);

    private static Properties prop;

    private static Properties getProperties() {
        if (prop == null) {
            prop = new Properties();
            try (InputStream is = TLValidationJobExecutor.class.getResourceAsStream("/app.config")) {
                prop.load(is);
            } catch (IOException e) {
                LOG.error("Could not load properties file : {}", e.getMessage(), e);
            }
        }
        return prop;
    }

    public static String getProperty(String propertyKey) {
        String value = getProperties().getProperty(propertyKey);
        if (Utils.isStringNotBlank(value)) {
            return value;
        }
        return null;
    }

    public static char[] getCharArrayProperty(String propertyKey) {
        if (getProperties().contains(propertyKey)) {
            return getProperties().getProperty(propertyKey).toCharArray();
        }
        return null;
    }

    public static List<String> getPropertyAsList(String propertyKey) {
        String value = getProperties().getProperty(propertyKey);
        if (Utils.isStringNotBlank(value)) {
            String[] strings = value.split(",");
            return Arrays.stream(strings).map(String::trim).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static int getIntProperty(String propertyKey) {
        String value = getProperty(propertyKey);
        if (Utils.isStringNotEmpty(value)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOG.error("Cannot parse the integer from key name '{}'! {}", propertyKey, e.getMessage(), e);
            }
        }
        return -1;
    }

    public static Boolean getBooleanProperty(String propertyKey) {
        String value = getProperty(propertyKey);
        if (Utils.isStringNotEmpty(value)) {
            if (Boolean.TRUE.toString().equals(value)) {
                return true;
            } else if (Boolean.FALSE.toString().equals(value)) {
                return false;
            } else {
                LOG.error("Unsupported boolean property value '{}'!", value);
            }
        }
        return null;
    }

}
