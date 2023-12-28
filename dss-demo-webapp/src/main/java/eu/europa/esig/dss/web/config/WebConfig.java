package eu.europa.esig.dss.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "eu.europa.esig.dss.web.controller" })
public class WebConfig implements WebMvcConfigurer {

	@Value("${multipart.maxFileSize:-1}")
	private long maxFileSize;

	@Value("${multipart.maxInMemorySize:-1}")
	private long maxInMemorySize;

	@Value("${multipart.resolveLazily:false}")
	private boolean resolveLazily;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
		registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/fonts/");
		registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
		registry.addResourceHandler("/scripts/**").addResourceLocations("classpath:/static/scripts/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/");
		registry.addResourceHandler("/jar/**").addResourceLocations("/jar/");
		registry.addResourceHandler("/downloads/**").addResourceLocations("/downloads/");
		registry.addResourceHandler("/doc/**").addResourceLocations("/doc/");
		registry.addResourceHandler("/apidocs/**").addResourceLocations("/apidocs/");
	}

	@Bean
	public MultipartResolver multipartResolver() {
		MultipartResolverProvider multipartResolverProvider = MultipartResolverProvider.getInstance();
		multipartResolverProvider.setMaxFileSize(maxFileSize);
		multipartResolverProvider.setMaxInMemorySize(maxInMemorySize);
		multipartResolverProvider.setResolveLazily(resolveLazily);
		return multipartResolverProvider.createMultipartResolver();
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("classpath:i18n/application");
		return messageSource;
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		// Remove the default SpringBoot behavior
	}

}
