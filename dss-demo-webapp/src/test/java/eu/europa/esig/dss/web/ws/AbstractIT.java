package eu.europa.esig.dss.web.ws;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractIT {

	protected String getBaseCxf() {
		Properties props = new Properties();
		try (InputStream is = new FileInputStream("src/test/resources/dss-test.properties")) {
			props.load(is);
		} catch (IOException e) {
			throw new RuntimeException("Unable to load properties");
		}
		return props.getProperty("base.cxf");
	}

}
