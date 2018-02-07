package eu.europa.esig.dss.web.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.annotation.PostConstruct;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import eu.europa.esig.dss.DSSXmlErrorListener;
import eu.europa.esig.dss.DomUtils;

@Component
public class XSLTService {

	private static final Logger logger = LoggerFactory.getLogger(XSLTService.class);

	private Templates templateSimpleReport;
	private Templates templateSimpleCertificateReport;
	private Templates templateDetailedReport;

	@Value("${tl.browser.trustmark.root.url}")
	private String rootTrustmarkUrlInTlBrowser;

	@Value("${tl.browser.country.root.url}")
	private String rootCountryUrlInTlBrowser;

	@PostConstruct
	public void init() throws TransformerConfigurationException, IOException {
		TransformerFactory transformerFactory = DomUtils.getSecureTransformerFactory();

		try (InputStream is = XSLTService.class.getResourceAsStream("/xslt/html/simple-report.xslt")) {
			templateSimpleReport = transformerFactory.newTemplates(new StreamSource(is));
		}

		try (InputStream is = XSLTService.class.getResourceAsStream("/xslt/html/simple-certificate-report.xslt")) {
			templateSimpleCertificateReport = transformerFactory.newTemplates(new StreamSource(is));
		}

		try (InputStream is = XSLTService.class.getResourceAsStream("/xslt/html/detailed-report.xslt")) {
			templateDetailedReport = transformerFactory.newTemplates(new StreamSource(is));
		}
	}

	public String generateSimpleReport(String simpleReport) {
		Writer writer = new StringWriter();
		try {
			Transformer transformer = templateSimpleReport.newTransformer();
			transformer.setErrorListener(new DSSXmlErrorListener());
			transformer.transform(new StreamSource(new StringReader(simpleReport)), new StreamResult(writer));
		} catch (Exception e) {
			logger.error("Error while generating simple report : " + e.getMessage(), e);
		}
		return writer.toString();
	}

	public String generateSimpleCertificateReport(String simpleReport) {
		Writer writer = new StringWriter();
		try {
			Transformer transformer = templateSimpleCertificateReport.newTransformer();
			transformer.setErrorListener(new DSSXmlErrorListener());
			transformer.setParameter("rootTrustmarkUrlInTlBrowser", rootTrustmarkUrlInTlBrowser);
			transformer.setParameter("rootCountryUrlInTlBrowser", rootCountryUrlInTlBrowser);
			transformer.transform(new StreamSource(new StringReader(simpleReport)), new StreamResult(writer));
		} catch (Exception e) {
			logger.error("Error while generating simple certificate report : " + e.getMessage(), e);
		}
		return writer.toString();
	}

	public String generateSimpleReport(Document dom) {
		Writer writer = new StringWriter();
		try {
			Transformer transformer = templateSimpleReport.newTransformer();
			transformer.setErrorListener(new DSSXmlErrorListener());
			transformer.transform(new DOMSource(dom), new StreamResult(writer));
		} catch (Exception e) {
			logger.error("Error while generating simple report : " + e.getMessage(), e);
		}
		return writer.toString();
	}

	public String generateDetailedReport(String detailedReport) {
		Writer writer = new StringWriter();
		try {
			Transformer transformer = templateDetailedReport.newTransformer();
			transformer.setErrorListener(new DSSXmlErrorListener());
			transformer.transform(new StreamSource(new StringReader(detailedReport)), new StreamResult(writer));
		} catch (Exception e) {
			logger.error("Error while generating detailed report : " + e.getMessage(), e);
		}
		return writer.toString();
	}

}