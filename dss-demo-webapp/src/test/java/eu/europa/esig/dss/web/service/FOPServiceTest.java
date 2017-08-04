package eu.europa.esig.dss.web.service;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import eu.europa.esig.dss.jaxb.detailedreport.DetailedReport;
import eu.europa.esig.dss.jaxb.simplereport.SimpleReport;
import eu.europa.esig.dss.web.config.DSSBeanConfig;

@WebAppConfiguration
@ContextConfiguration(classes = { DSSBeanConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class FOPServiceTest {

	@Autowired
	private FOPService service;

	@Test
	public void generateSimpleReport() throws Exception {
		JAXBContext context = JAXBContext.newInstance(SimpleReport.class.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Marshaller marshaller = context.createMarshaller();

		SimpleReport simpleReport = (SimpleReport) unmarshaller.unmarshal(new File("src/test/resources/simpleReport.xml"));
		assertNotNull(simpleReport);

		StringWriter writer = new StringWriter();
		marshaller.marshal(simpleReport, writer);

		FileOutputStream fos = new FileOutputStream("target/simpleReport.pdf");
		service.generateSimpleReport(writer.toString(), fos);
	}

	@Test
	public void generateSimpleReportMulti() throws Exception {
		JAXBContext context = JAXBContext.newInstance(SimpleReport.class.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Marshaller marshaller = context.createMarshaller();

		SimpleReport simpleReport = (SimpleReport) unmarshaller.unmarshal(new File("src/test/resources/simple-report-multi-signatures.xml"));
		assertNotNull(simpleReport);

		StringWriter writer = new StringWriter();
		marshaller.marshal(simpleReport, writer);

		FileOutputStream fos = new FileOutputStream("target/simpleReportMulti.pdf");
		service.generateSimpleReport(writer.toString(), fos);
	}

	@Test
	public void generateDetailedReport() throws Exception {
		JAXBContext context = JAXBContext.newInstance(DetailedReport.class.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Marshaller marshaller = context.createMarshaller();

		DetailedReport detailedReport = (DetailedReport) unmarshaller.unmarshal(new File("src/test/resources/detailedReport.xml"));
		assertNotNull(detailedReport);

		StringWriter writer = new StringWriter();
		marshaller.marshal(detailedReport, writer);

		FileOutputStream fos = new FileOutputStream("target/detailedReport.pdf");
		service.generateDetailedReport(writer.toString(), fos);
	}

	@Test
	public void generateDetailedReportMultiSignatures() throws Exception {
		JAXBContext context = JAXBContext.newInstance(DetailedReport.class.getPackage().getName());
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Marshaller marshaller = context.createMarshaller();

		DetailedReport detailedReport = (DetailedReport) unmarshaller.unmarshal(new File("src/test/resources/detailed-report-multi-signatures.xml"));
		assertNotNull(detailedReport);

		StringWriter writer = new StringWriter();
		marshaller.marshal(detailedReport, writer);

		FileOutputStream fos = new FileOutputStream("target/detailedReportMulti.pdf");
		service.generateDetailedReport(writer.toString(), fos);
	}

}
