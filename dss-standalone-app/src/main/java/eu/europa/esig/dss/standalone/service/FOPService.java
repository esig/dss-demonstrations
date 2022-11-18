package eu.europa.esig.dss.standalone.service;

import eu.europa.esig.dss.detailedreport.DetailedReportFacade;
import eu.europa.esig.dss.detailedreport.jaxb.XmlDetailedReport;
import eu.europa.esig.dss.simplereport.SimpleReportFacade;
import eu.europa.esig.dss.simplereport.jaxb.XmlSimpleReport;
import eu.europa.esig.dss.standalone.exception.ApplicationException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import java.io.InputStream;
import java.io.OutputStream;

public class FOPService {

    private static final Logger LOG = LoggerFactory.getLogger(FOPService.class);

    private static final String FOP_CONFIG = "/fop.xconf";

    private static FOPService instance;

    private FopFactory fopFactory;
    private FOUserAgent foUserAgent;

    private FOPService() {
        init();
    }

    private void init() {
        try (InputStream is = getClass().getResourceAsStream(FOP_CONFIG)) {
            FopFactoryBuilder builder = new FopFactoryBuilder(getClass().getResource("/").toURI());
            builder.setAccessibility(true);

            DefaultConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder();
            Configuration configuration = configurationBuilder.build(is);
            builder.setConfiguration(configuration);

            fopFactory = builder.build();

            foUserAgent = fopFactory.newFOUserAgent();
            foUserAgent.setCreator("DSS Standalone App");
            foUserAgent.setAccessibility(true);

        } catch (Exception e) {
            LOG.error(String.format("Unable to instantiate FOPService: %s", e.getMessage()),  e);
            throw new ApplicationException(e);
        }
    }

    public static FOPService getInstance() {
        if (instance == null) {
            instance = new FOPService();
        }
        return instance;
    }

    public void generateSimpleReport(XmlSimpleReport simpleReport, OutputStream os) throws Exception {
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, os);
        Result result = new SAXResult(fop.getDefaultHandler());
        SimpleReportFacade.newFacade().generatePdfReport(simpleReport, result);
    }

    public void generateDetailedReport(XmlDetailedReport detailedReport, OutputStream os) throws Exception {
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, os);
        Result result = new SAXResult(fop.getDefaultHandler());
        DetailedReportFacade.newFacade().generatePdfReport(detailedReport, result);
    }

}
