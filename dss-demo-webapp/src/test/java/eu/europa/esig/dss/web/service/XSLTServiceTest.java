package eu.europa.esig.dss.web.service;


import eu.europa.esig.dss.detailedreport.jaxb.XmlDetailedReport;
import eu.europa.esig.dss.simplereport.jaxb.XmlSimpleReport;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.DssDemoApplicationTests;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XSLTServiceTest extends DssDemoApplicationTests {

	private static final Logger LOG = LoggerFactory.getLogger(XSLTServiceTest.class);

	private static final eu.europa.esig.dss.detailedreport.jaxb.ObjectFactory OF_DETAILED_REPORT = new eu.europa.esig.dss.detailedreport.jaxb.ObjectFactory();
	private static final eu.europa.esig.dss.simplereport.jaxb.ObjectFactory OF_SIMPLE_REPORT = new eu.europa.esig.dss.simplereport.jaxb.ObjectFactory();


	@Test
	@SuppressWarnings("unchecked")
	public void generateSimpleReport() throws Exception {
		JAXBContext context = JAXBContext.newInstance(XmlSimpleReport.class.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Marshaller marshaller = context.createMarshaller();

		JAXBElement<XmlSimpleReport> unmarshal = (JAXBElement<XmlSimpleReport>) unmarshaller.unmarshal(new File("src/test/resources/simpleReport.xml"));
		assertNotNull(unmarshal);
		XmlSimpleReport simpleReport = unmarshal.getValue();
		assertNotNull(simpleReport);

		StringWriter writer = new StringWriter();
		marshaller.marshal(OF_SIMPLE_REPORT.createSimpleReport(simpleReport), writer);

		String htmlSimpleReport = getXsltService().generateSimpleReport(writer.toString());
		assertTrue(Utils.isStringNotEmpty(htmlSimpleReport));
		LOG.debug("Simple report html : " + htmlSimpleReport);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void generateSimpleReportMulti() throws Exception {
		JAXBContext context = JAXBContext.newInstance(XmlSimpleReport.class.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Marshaller marshaller = context.createMarshaller();

		JAXBElement<XmlSimpleReport> unmarshal = (JAXBElement<XmlSimpleReport>) unmarshaller
				.unmarshal(new File("src/test/resources/simple-report-multi-signatures.xml"));
		assertNotNull(unmarshal);
		XmlSimpleReport simpleReport = unmarshal.getValue();
		assertNotNull(simpleReport);

		StringWriter writer = new StringWriter();
		marshaller.marshal(OF_SIMPLE_REPORT.createSimpleReport(simpleReport), writer);

		String htmlSimpleReport = getXsltService().generateSimpleReport(writer.toString());
		assertTrue(Utils.isStringNotEmpty(htmlSimpleReport));
		LOG.debug("Simple report html : " + htmlSimpleReport);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void generateDetailedReport() throws Exception {
		JAXBContext context = JAXBContext.newInstance(XmlDetailedReport.class.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Marshaller marshaller = context.createMarshaller();

		JAXBElement<XmlDetailedReport> unmarshal = (JAXBElement<XmlDetailedReport>) unmarshaller.unmarshal(new File("src/test/resources/detailedReport.xml"));
		assertNotNull(unmarshal);
		XmlDetailedReport detailedReport = unmarshal.getValue();
		assertNotNull(detailedReport);

		StringWriter writer = new StringWriter();
		marshaller.marshal(OF_DETAILED_REPORT.createDetailedReport(detailedReport), writer);

		String htmlDetailedReport = getXsltService().generateDetailedReport(writer.toString());
		assertTrue(Utils.isStringNotEmpty(htmlDetailedReport));
		LOG.debug("Detailed report html : " + htmlDetailedReport);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void generateDetailedReportMultiSignatures() throws Exception {
		JAXBContext context = JAXBContext.newInstance(XmlDetailedReport.class.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Marshaller marshaller = context.createMarshaller();

		JAXBElement<XmlDetailedReport> unmarshal = (JAXBElement<XmlDetailedReport>) unmarshaller
				.unmarshal(new File("src/test/resources/detailed-report-multi-signatures.xml"));
		assertNotNull(unmarshal);
		XmlDetailedReport detailedReport = unmarshal.getValue();
		assertNotNull(detailedReport);

		StringWriter writer = new StringWriter();
		marshaller.marshal(OF_DETAILED_REPORT.createDetailedReport(detailedReport), writer);

		String htmlDetailedReport = getXsltService().generateDetailedReport(writer.toString());
		assertTrue(Utils.isStringNotEmpty(htmlDetailedReport));
		LOG.debug("Detailed report html : " + htmlDetailedReport);
	}

}
