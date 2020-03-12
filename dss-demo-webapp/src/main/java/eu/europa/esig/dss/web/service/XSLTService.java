package eu.europa.esig.dss.web.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.europa.esig.dss.detailedreport.DetailedReportFacade;
import eu.europa.esig.dss.diagnostic.DiagnosticDataFacade;
import eu.europa.esig.dss.diagnostic.jaxb.XmlDiagnosticData;
import eu.europa.esig.dss.simplecertificatereport.SimpleCertificateReportXmlDefiner;
import eu.europa.esig.dss.simplereport.SimpleReportFacade;

@Component
public class XSLTService {

	private static final Logger LOG = LoggerFactory.getLogger(XSLTService.class);

	@Value("${tl.browser.trustmark.root.url}")
	private String rootTrustmarkUrlInTlBrowser;

	@Value("${tl.browser.country.root.url}")
	private String rootCountryUrlInTlBrowser;

	public String generateSimpleReport(String simpleReport) {
		try {
			return SimpleReportFacade.newFacade().generateHtmlReport(simpleReport);
		} catch (Exception e) {
			LOG.error("Error while generating simple report : " + e.getMessage(), e);
			return null;
		}
	}

	public String generateSimpleCertificateReport(String simpleReport) {
		try (Writer writer = new StringWriter()) {
			Transformer transformer = SimpleCertificateReportXmlDefiner.getHtmlBootstrap4Templates().newTransformer();
			transformer.setParameter("rootTrustmarkUrlInTlBrowser", rootTrustmarkUrlInTlBrowser);
			transformer.setParameter("rootCountryUrlInTlBrowser", rootCountryUrlInTlBrowser);
			transformer.transform(new StreamSource(new StringReader(simpleReport)), new StreamResult(writer));
			return writer.toString();
		} catch (Exception e) {
			LOG.error("Error while generating simple certificate report : " + e.getMessage(), e);
			return null;
		}
	}

	public  String generateDetailedReport(String detailedReport) {
		try {
			return DetailedReportFacade.newFacade().generateHtmlReport(detailedReport);
		} catch (Exception e) {
			LOG.error("Error while generating detailed report : " + e.getMessage(), e);
			return null;
		}
	}

    public String generateSVG(String diagnosticDataXml) {
        try (Writer writer = new StringWriter()) {
            XmlDiagnosticData diagnosticData = DiagnosticDataFacade.newFacade().unmarshall(diagnosticDataXml);
            DiagnosticDataFacade.newFacade().generateSVG(diagnosticData, new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            LOG.error("Error while generating the SVG : " + e.getMessage(), e);
            return null;
        }
    }

}