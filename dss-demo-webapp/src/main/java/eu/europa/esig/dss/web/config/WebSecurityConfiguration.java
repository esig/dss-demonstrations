package eu.europa.esig.dss.web.config;

import eu.europa.esig.dss.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.io.IOException;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(WebSecurityConfiguration.class);

	@Value("${web.security.cookie.samesite}")
	private String samesite;

	@Value("${web.security.csp}")
	private String csp;

	@Value("${web.strict.transport.security:}")
	private String strictTransportSecurity;
	
	/** API urls (REST/SOAP webServices) */
	private static final String[] API_URLS = new String[] {
			"/services/rest/**", "/services/soap/**"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		// javadoc uses frames
		http.headers(headers -> {
			headers.addHeaderWriter(javadocHeaderWriter())
					.addHeaderWriter(svgHeaderWriter())
					.addHeaderWriter(serverEsigDSS());
			if (Utils.isStringNotEmpty(strictTransportSecurity)) {
				headers.addHeaderWriter(strictTransportSecurity());
			}
			if (Utils.isStringNotEmpty(csp)) {
				headers.contentSecurityPolicy(policy -> policy.policyDirectives(csp));
			}
		});

		http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.anyRequest().permitAll());

		// disable CSRF for API calls (REST/SOAP webServices)
		http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.ignoringRequestMatchers(getAntMatchers()));

		return http.build();
	}

	private RequestMatcher[] getAntMatchers() {
		RequestMatcher[] requestMatchers = new RequestMatcher[API_URLS.length];
		for (int i = 0; i < API_URLS.length; i++) {
			requestMatchers[i] = new AntPathRequestMatcher(API_URLS[i]);
		}
		return requestMatchers;
	}

	@Bean
	public HeaderWriter javadocHeaderWriter() {
		final AntPathRequestMatcher javadocAntPathRequestMatcher = new AntPathRequestMatcher("/apidocs/**");
		final HeaderWriter hw = new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN);
		return new DelegatingRequestMatcherHeaderWriter(javadocAntPathRequestMatcher, hw);
	}

	@Bean
	public HeaderWriter svgHeaderWriter() {
		final AntPathRequestMatcher javadocAntPathRequestMatcher = new AntPathRequestMatcher("/validation/diag-data.svg");
		final HeaderWriter hw = new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN);
		return new DelegatingRequestMatcherHeaderWriter(javadocAntPathRequestMatcher, hw);
	}
	
	@Bean
	public HeaderWriter serverEsigDSS() {
		return new StaticHeadersWriter("Server", "ESIG-DSS");
	}

	@Bean
	public HeaderWriter strictTransportSecurity() {
		return new StaticHeadersWriter("Strict-Transport-Security", strictTransportSecurity);
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

	@Bean
	public RequestRejectedHandler requestRejectedHandler() {
		// Transforms Tomcat interrupted exceptions to a BAD_REQUEST error
		return new RequestRejectedHandler() {
			@Override
			public void handle(HttpServletRequest request, HttpServletResponse response,
							   RequestRejectedException requestRejectedException) throws IOException {
				LOG.error("An error occurred : " + requestRejectedException.getMessage(), requestRejectedException);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Bad request : " + requestRejectedException.getMessage());
			}
		};
	}

	@Bean
	public AuthenticationManager noAuthenticationManager() {
		return authentication -> {
			throw new AuthenticationServiceException("Authentication is disabled");
		};
	}

}
