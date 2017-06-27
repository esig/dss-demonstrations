package eu.europa.esig.dss.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:dss-custom.properties", ignoreResourceNotFound = true)
public class OverrideConfig {

	@Configuration
	@Import({PersistenceConfig.class, SpringConfig.class, WebSecurityConfig.class, DSSBeanFactory.class })
	static class InnerConfiguration {

	}
}
