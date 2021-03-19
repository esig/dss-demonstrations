package eu.europa.esig.dss.web.config;

import eu.europa.esig.dss.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.MappedInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${web.security.cookie.samesite}")
	private String samesite;

	@Value("${web.security.csp}")
	private String csp;
	
	/** API urls (REST/SOAP webServices) */
	private static final String[] API_URLS = new String[] {
			"/services/rest/**", "/services/soap/**"
	};

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// javadoc uses frames
		http.headers().addHeaderWriter(javadocHeaderWriter());
		http.headers().addHeaderWriter(svgHeaderWriter());
		http.headers().addHeaderWriter(serverEsigDSS());
		
		http.csrf().ignoringAntMatchers(API_URLS); // disable CSRF for API calls (REST/SOAP webServices)

		if (Utils.isStringNotEmpty(csp)) {
			http.headers().contentSecurityPolicy(csp);
		}
	}

	@Bean
	public HeaderWriter javadocHeaderWriter() {
		final AntPathRequestMatcher javadocAntPathRequestMatcher = new AntPathRequestMatcher("/apidocs/**");
		final HeaderWriter hw = new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN);
		return new DelegatingRequestMatcherHeaderWriter(javadocAntPathRequestMatcher, hw);
	}

	@Bean
	public  HeaderWriter svgHeaderWriter() {
		final AntPathRequestMatcher javadocAntPathRequestMatcher = new AntPathRequestMatcher("/validation/diag-data.svg");
		final HeaderWriter hw = new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN);
		return new DelegatingRequestMatcherHeaderWriter(javadocAntPathRequestMatcher, hw);
	}
	
	@Bean
	public HeaderWriter serverEsigDSS() {
		return new StaticHeadersWriter("Server", "ESIG-DSS");
	}

	@Bean
	public MappedInterceptor cookiesInterceptor() {
		return new MappedInterceptor(null, new CookiesHandlerInterceptor());
	}

	/**
	 * The class is used to enrich "Set-Cookie" header with "SameSite=strict" value
	 *
	 * NOTE: Spring does not provide support of cookies handling out of the box
	 *       and requires a Spring Session dependency for that.
	 *       Here is a manual way of response headers configuration
	 */
	private final class CookiesHandlerInterceptor implements HandlerInterceptor {

		/** The "SameSite" cookie parameter name */
		private static final String SAMESITE_NAME = "SameSite";

		@Override
		public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
							   ModelAndView modelAndView) {
			if (Utils.isStringNotEmpty(samesite)) {
				Collection<String> setCookieHeaders = response.getHeaders(HttpHeaders.SET_COOKIE);
				if (Utils.isCollectionNotEmpty(setCookieHeaders)) {
					for (String header : setCookieHeaders) {
						header = String.format("%s; %s=%s", header, SAMESITE_NAME, samesite);
						response.setHeader(HttpHeaders.SET_COOKIE, header);
					}
				}
			}
		}
	}

}
