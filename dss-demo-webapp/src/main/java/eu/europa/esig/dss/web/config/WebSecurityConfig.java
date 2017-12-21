package eu.europa.esig.dss.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${admin.username}")
	private String adminUsername;

	@Value("${admin.password}")
	private String adminPassword;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

		// javadoc uses frames
		http.antMatcher("/apidocs/*").headers().frameOptions().sameOrigin();

		http.authorizeRequests().antMatchers("/admin", "/admin/*").authenticated().anyRequest().permitAll().and().formLogin().loginPage("/login")
				.failureUrl("/login-error").defaultSuccessUrl("/admin");
		http.userDetailsService(userDetailsService());
	}

	@Override
	@Bean
	public UserDetailsService userDetailsService() {
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
		manager.createUser(User.withUsername(adminUsername).password(adminPassword).roles("ADMIN").build());
		return manager;
	}
}
