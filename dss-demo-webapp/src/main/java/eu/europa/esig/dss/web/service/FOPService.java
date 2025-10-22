package eu.europa.esig.dss.web.service;

import eu.europa.esig.dss.detailedreport.DetailedReportXmlDefiner;
import eu.europa.esig.dss.simplecertificatereport.SimpleCertificateReportXmlDefiner;
import eu.europa.esig.dss.simplereport.SimpleReportXmlDefiner;
import eu.europa.esig.dss.xml.utils.DSSXmlErrorListener;
import jakarta.annotation.PostConstruct;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;

@Component
public class FOPService {

	private static final Logger LOG = LoggerFactory.getLogger(FOPService.class);

	private static final String FOP_CONFIG = "fop.xconf";

	@Value("${tl.browser.root.url}")
	private String rootUrlInTlBrowser;

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
		try (StringReader reader = new StringReader(simpleReport)) {
			Transformer transformer = SimpleReportXmlDefiner.getPdfTemplates().newTransformer();
			transformer.setErrorListener(new DSSXmlErrorListener());
			transformer.setParameter("rootUrlInTlBrowser", rootUrlInTlBrowser);
			transformer.transform(new StreamSource(reader), result);
		} catch (Exception e) {
			LOG.error("Error while generating simple report : " + e.getMessage(), e);
		}
	}

	public void generateSimpleCertificateReport(String simpleCertificateReport, OutputStream os) throws Exception {
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, os);
		Result result = new SAXResult(fop.getDefaultHandler());
		try (StringReader reader = new StringReader(simpleCertificateReport)) {
			Transformer transformer = SimpleCertificateReportXmlDefiner.getPdfTemplates().newTransformer();
			transformer.setErrorListener(new DSSXmlErrorListener());
			transformer.setParameter("rootUrlInTlBrowser", rootUrlInTlBrowser);
			transformer.transform(new StreamSource(reader), result);
		} catch (Exception e) {
			LOG.error("Error while generating simple certificate report : " + e.getMessage(), e);
		}
	}

	public void generateDetailedReport(String detailedReport, OutputStream os) throws Exception {
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, os);
		Result result = new SAXResult(fop.getDefaultHandler());
		try (StringReader reader = new StringReader(detailedReport)) {
			Transformer transformer = DetailedReportXmlDefiner.getPdfTemplates().newTransformer();
			transformer.setErrorListener(new DSSXmlErrorListener());
			transformer.transform(new StreamSource(reader), result);
		} catch (Exception e) {
			LOG.error("Error while generating detailed report : " + e.getMessage(), e);
		}
	}

	private static class ClasspathResolver implements ResourceResolver {

		@Override
		public Resource getResource(URI uri) {
			return new Resource(FOPService.class.getResourceAsStream("/fonts/" + FilenameUtils.getName(uri.toString())));
		}

		@Override
		public OutputStream getOutputStream(URI uri) throws IOException {
			return Thread.currentThread().getContextClassLoader().getResource(uri.toString()).openConnection()
					.getOutputStream();
		}

	}

}
