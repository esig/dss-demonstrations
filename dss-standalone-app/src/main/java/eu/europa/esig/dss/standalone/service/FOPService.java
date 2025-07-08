package eu.europa.esig.dss.standalone.service;

import eu.europa.esig.dss.detailedreport.DetailedReportFacade;
import eu.europa.esig.dss.detailedreport.jaxb.XmlDetailedReport;
import eu.europa.esig.dss.simplereport.SimpleReportFacade;
import eu.europa.esig.dss.simplereport.jaxb.XmlSimpleReport;
import eu.europa.esig.dss.standalone.exception.ApplicationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.fop.apps.EnvironmentalProfileFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

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

            FopFactoryBuilder builder = new FopConfParser(is,
                    EnvironmentalProfileFactory.createRestrictedIO(new URI("file:/"), new ClasspathResolver())).getFopFactoryBuilder();

            builder.setAccessibility(true);

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
