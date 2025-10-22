package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.web.service.SigningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;

public abstract class AbstractSignatureController {

    protected static final String SIGNATURE_PROCESS = "signature-process";

    @Value("${nexuUrl}")
    protected String nexuUrl;

    @Value("${default.digest.algo}")
    protected String defaultDigestAlgo;

    @Autowired
    protected SigningService signingService;

    @ModelAttribute("isMockUsed")
    public boolean isMockUsed() {
        return signingService.isMockTSPSourceUsed();
    }

}
