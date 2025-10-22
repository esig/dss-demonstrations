package eu.europa.esig.dss.web.service;


import eu.europa.esig.dss.detailedreport.jaxb.XmlDetailedReport;
import eu.europa.esig.dss.simplereport.jaxb.XmlSimpleReport;
import eu.europa.esig.dss.web.DssDemoApplicationTests;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FOPServiceTest extends DssDemoApplicationTests {

	private static final eu.europa.esig.dss.detailedreport.jaxb.ObjectFactory OF_DETAILED_REPORT = new eu.europa.esig.dss.detailedreport.jaxb.ObjectFactory();
	private static final eu.europa.esig.dss.simplereport.jaxb.ObjectFactory OF_SIMPLE_REPORT = new eu.europa.esig.dss.simplereport.jaxb.ObjectFactory();


	@BeforeAll
	public static void init() {
		// required to resolve classpath references for unit tests
		org.apache.catalina.webresources.TomcatURLStreamHandlerFactory.getInstance();
	}

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

		FileOutputStream fos = new FileOutputStream("target/simpleReport.pdf");
		getFopService().generateSimpleReport(writer.toString(), fos);
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

		FileOutputStream fos = new FileOutputStream("target/simpleReportMulti.pdf");
		getFopService().generateSimpleReport(writer.toString(), fos);
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

		FileOutputStream fos = new FileOutputStream("target/detailedReport.pdf");
		getFopService().generateDetailedReport(writer.toString(), fos);
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

		FileOutputStream fos = new FileOutputStream("target/detailedReportMulti.pdf");
		getFopService().generateDetailedReport(writer.toString(), fos);
	}

}
