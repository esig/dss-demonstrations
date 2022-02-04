package eu.europa.esig.dss.web.service;

import eu.europa.esig.dss.detailedreport.DetailedReportFacade;
import eu.europa.esig.dss.simplereport.SimpleReportFacade;
import org.apache.commons.io.FilenameUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

@Component
public class FOPService {

	private static final String FOP_CONFIG = "fop.xconf";

	private FopFactory fopFactory;
	private FOUserAgent foUserAgent;

	@PostConstruct
	public void init() throws Exception {

		FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI(), new ClasspathResolver());
		builder.setAccessibility(true);

		try (InputStream is = new ClassPathResource(FOP_CONFIG).getInputStream()) {
			DefaultConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder();
			Configuration configuration = configurationBuilder.build(is);
			builder.setConfiguration(configuration);
		}

		fopFactory = builder.build();

		foUserAgent = fopFactory.newFOUserAgent();
		foUserAgent.setCreator("DSS Webapp");
		foUserAgent.setAccessibility(true);

	}

	public void generateSimpleReport(String simpleReport, OutputStream os) throws Exception {
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, os);
		Result result = new SAXResult(fop.getDefaultHandler());
		SimpleReportFacade.newFacade().generatePdfReport(simpleReport, result);
	}

	public void generateDetailedReport(String detailedReport, OutputStream os) throws Exception {
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, os);
		Result result = new SAXResult(fop.getDefaultHandler());
		DetailedReportFacade.newFacade().generatePdfReport(detailedReport, result);
	}

	private static class ClasspathResolver implements ResourceResolver {

		@Override
		public Resource getResource(URI uri) throws IOException {
			return new Resource(FOPService.class.getResourceAsStream("/fonts/" + FilenameUtils.getName(uri.toString())));
		}

		@Override
		public OutputStream getOutputStream(URI uri) throws IOException {
			return Thread.currentThread().getContextClassLoader().getResource(uri.toString()).openConnection()
					.getOutputStream();
		}

	}

}
