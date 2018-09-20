package eu.europa.esig.dss.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySources({ @PropertySource("classpath:dss.properties"), @PropertySource(value = "classpath:dss-custom.properties", ignoreResourceNotFound = true) })
public class PropertiesConfig {

	public PropertiesConfig() {
		super();
	}

	// static because return type is an instance of BeanFactoryPostProcessor (see
	// javadoc of @Bean)
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}