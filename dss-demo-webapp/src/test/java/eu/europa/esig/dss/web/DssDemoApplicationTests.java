package eu.europa.esig.dss.web;

import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.web.config.DSSBeanConfig;
import eu.europa.esig.dss.web.service.FOPService;
import eu.europa.esig.dss.web.service.XSLTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootTest(classes = { DSSBeanConfig.class })
@EnableWebSecurity
public abstract class DssDemoApplicationTests {

    @Autowired
    private CertificateVerifier certificateVerifier;

    @Autowired
    private FOPService fopService;

    @Autowired
    private TSPSource tspSource;

    @Autowired
    private XSLTService xsltService;

    protected CertificateVerifier getCertificateVerifier() {
        return certificateVerifier;
    }

    protected FOPService getFopService() {
        return fopService;
    }

    protected TSPSource getTspSource() {
        return tspSource;
    }

    protected XSLTService getXsltService() {
        return xsltService;
    }

}
