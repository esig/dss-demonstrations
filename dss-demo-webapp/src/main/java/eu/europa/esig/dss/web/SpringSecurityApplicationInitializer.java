package eu.europa.esig.dss.web;

import javax.servlet.ServletContext;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import eu.europa.esig.dss.web.config.MultipartFormDataFilter;

public class SpringSecurityApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
	
	@Override
    protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
		// does not work with a default MultipartFilter
        insertFilters(servletContext, new MultipartFormDataFilter());
    }

}
