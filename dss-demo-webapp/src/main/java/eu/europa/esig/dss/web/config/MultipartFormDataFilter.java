package eu.europa.esig.dss.web.config;

import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;

public class MultipartFormDataFilter extends MultipartFilter {
	
	private CommonsMultipartResolver multipartResolver;
	
	/**
	 * Returns a configured multipartResolver
	 * 
	 * @return {@link MultipartResolver}
	 */
	public MultipartResolver multipartResolver() {
		if (multipartResolver == null) {
			multipartResolver = new CommonsMultipartResolver();
			multipartResolver.setMaxInMemorySize(52428800);
			multipartResolver.setMaxUploadSize(52428800);
			multipartResolver.setResolveLazily(true);
		}
		return multipartResolver;
	}
	
	@Override
	protected MultipartResolver lookupMultipartResolver() {
		return multipartResolver();
	}
	
}