package eu.europa.esig.dss.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

public class AppInitializer implements WebApplicationInitializer {

	@Value("${cookie.secure}")
	private boolean cookieSecure;

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		WebApplicationContext context = getContext();
		servletContext.addListener(new ContextLoaderListener(context));
		servletContext.addFilter("characterEncodingFilter", new CharacterEncodingFilter("UTF-8"));

		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("Dispatcher", new DispatcherServlet(context));
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping("/*");

		CXFServlet cxf = new CXFServlet();
		BusFactory.setDefaultBus(cxf.getBus());
		ServletRegistration.Dynamic cxfServlet = servletContext.addServlet("CXFServlet", cxf);
		cxfServlet.setLoadOnStartup(1);
		cxfServlet.addMapping("/services/*");

		servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy("springSecurityFilterChain")).addMappingForUrlPatterns(null, false,
				"/*");

		servletContext.getSessionCookieConfig().setSecure(cookieSecure);
	}

	private AnnotationConfigWebApplicationContext getContext() {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.setConfigLocations("eu.europa.esig.dss.web.config");
		return context;
	}
}
