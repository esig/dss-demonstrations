package eu.europa.esig.dss.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	private static final String ADMIN_ROLE = "ADMIN";

	@Value("${admin.username}")
	private String adminUsername;

	@Value("${admin.password}")
	private String adminPassword;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable();

		http.authorizeRequests()

				.antMatchers("/admin", "/admin/*").hasRole(ADMIN_ROLE)

				.anyRequest().permitAll()

				.and().formLogin().loginPage("/login").failureUrl("/login-error").defaultSuccessUrl("/admin");

		// javadoc uses frames
		http.headers().addHeaderWriter(javadocHeaderWriter());
	}

	@Bean
	public HeaderWriter javadocHeaderWriter() {
		final AntPathRequestMatcher javadocAntPathRequestMatcher = new AntPathRequestMatcher("/apidocs/**");
		final HeaderWriter hw = new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN);
		return new DelegatingRequestMatcherHeaderWriter(javadocAntPathRequestMatcher, hw);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser(adminUsername).password(adminPassword).roles(ADMIN_ROLE);
	}

}
