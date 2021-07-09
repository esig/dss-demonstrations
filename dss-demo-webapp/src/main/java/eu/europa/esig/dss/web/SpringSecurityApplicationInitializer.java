package eu.europa.esig.dss.web;

import eu.europa.esig.dss.web.config.MultipartResolverProvider;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;

import javax.servlet.ServletContext;

public class SpringSecurityApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
	
	@Override
    protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
		// initialize all files resolver with lazy loading
        insertFilters(servletContext, new AcceptAllFilesMultipartFilter());
    }

    /**
     * The filter used to accept all files, but load lazily
     */
    private static class AcceptAllFilesMultipartFilter extends MultipartFilter {

        @Override
        protected MultipartResolver lookupMultipartResolver() {
            return MultipartResolverProvider.getInstance().getAcceptAllFilesResolver();
        }

    }

}
