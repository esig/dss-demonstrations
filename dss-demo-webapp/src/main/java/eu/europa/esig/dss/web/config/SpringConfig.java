package eu.europa.esig.dss.web.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Marshaller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import eu.europa.esig.dss.tsl.service.TSLValidationJob;

@Configuration
@ComponentScan(basePackages = { "eu.europa.esig.dss.web.service" })
@PropertySource("classpath:dss.properties")
public class SpringConfig {
	
	@Value("${tl.refresh.interval}")
	private String tlRefreshInterval;
	
	@Bean
	public MappingJackson2HttpMessageConverter jsonMessageConverter() {
		return new MappingJackson2HttpMessageConverter();
	}
	
	@Bean
	public RequestMappingHandlerAdapter requestMappingHandlerAdapter(MappingJackson2HttpMessageConverter jsonMessageConverter) {
		RequestMappingHandlerAdapter requestMappingHandlerAdapter = new RequestMappingHandlerAdapter();
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(jsonMessageConverter);
		requestMappingHandlerAdapter.setMessageConverters(messageConverters);
		return requestMappingHandlerAdapter;
	}
	
	@Bean
	public Jaxb2Marshaller policyMarshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setPackagesToScan("eu.europa.esig.jaxb.policy");
		Map<String, Boolean> properties = new HashMap<String, Boolean>();
		properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setMarshallerProperties(properties);
		return marshaller;
	}
	
	@Bean
	public MethodInvokingJobDetailFactoryBean tslValidationJobDetail(TSLValidationJob tslValidationJob) {
		MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
		jobDetailFactoryBean.setTargetObject(tslValidationJob);
		jobDetailFactoryBean.setTargetMethod("refresh");
		return jobDetailFactoryBean;
	}
	
	@Bean
	public SimpleTriggerFactoryBean simpleTrigger(MethodInvokingJobDetailFactoryBean tslValidationJobDetail) {
		SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
		simpleTriggerFactoryBean.setJobDetail(tslValidationJobDetail.getObject());
		simpleTriggerFactoryBean.setStartDelay(5000);
		simpleTriggerFactoryBean.setRepeatInterval(Long.valueOf(tlRefreshInterval));
		return simpleTriggerFactoryBean;
	}
	
	@Bean
	public SchedulerFactoryBean schedulerFactoryBean(SimpleTriggerFactoryBean simpleTrigger) {
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		schedulerFactoryBean.setTriggers(simpleTrigger.getObject());
		return schedulerFactoryBean;
	}
}
