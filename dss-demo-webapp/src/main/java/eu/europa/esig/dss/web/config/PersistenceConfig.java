package eu.europa.esig.dss.web.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = {"eu.europa.esig.dss.web.model.repository"})
@EnableTransactionManagement
@PropertySource("classpath:dss.properties")
public class PersistenceConfig {

	@Value("${datasource.username}")
	private String username;
	
	@Value("${datasource.password}")
	private String password;
	
	@Value("${datasource.url}")
	private String dataSourceUrl;
	
	@Value("${datasource.driver.class}")
	private String dataSourceDriverClassName;
	
	@Bean
	public DataSource datasource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setPassword(password);
		ds.setUsername(username);
		ds.setUrl(dataSourceUrl);
		ds.setDriverClassName(dataSourceDriverClassName);
		return ds;
	}
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean lcemfb = new LocalContainerEntityManagerFactoryBean();
		if(dataSource == null) {
			System.out.println("1");
		} else {
			System.out.println("2");
		}
		lcemfb.setDataSource(dataSource);
		lcemfb.setPackagesToScan("eu.europa.esig.dss.web.model.db");

		HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
		lcemfb.setJpaVendorAdapter(va);

		Properties ps = new Properties();
		ps.put("hibernate.hbm2ddl.auto", "create-drop");
		ps.put("hibernate.hbm2ddl.import_files", "load.sql");
		lcemfb.setJpaProperties(ps);
		lcemfb.afterPropertiesSet();
		return lcemfb;
	}
	
	@Bean
	public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
		JpaTransactionManager tm = new JpaTransactionManager();
		tm.setEntityManagerFactory(entityManagerFactory.getObject());
		return tm;
	}
}
