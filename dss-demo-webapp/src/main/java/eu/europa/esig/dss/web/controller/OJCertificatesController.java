package eu.europa.esig.dss.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.europa.esig.dss.spi.tsl.LOTLInfo;
import eu.europa.esig.dss.spi.tsl.ParsingInfoRecord;
import eu.europa.esig.dss.spi.tsl.TLValidationJobSummary;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.service.KeystoreService;

@Controller
@RequestMapping(value = "/oj-certificates" )
public class OJCertificatesController {

	private static final String CERTIFICATE_TILE = "oj-certificates";

	@Autowired
	@Qualifier("european-lotl-source")
	private LOTLSource lotlSource;
	
	@Autowired
	@Qualifier("european-trusted-list-certificate-source")
	private TrustedListsCertificateSource trustedCertificateSource;

	@Autowired
	private KeystoreService keystoreService;

	@RequestMapping(method = RequestMethod.GET)
	public String showCertificates(Model model, HttpServletRequest request) {
		// From Config
		model.addAttribute("keystoreCertificates", keystoreService.getCertificatesDTOFromKeyStore(lotlSource.getCertificateSource().getCertificates()));

		OfficialJournalSchemeInformationURI ojUriInfo = (OfficialJournalSchemeInformationURI) lotlSource.getSigningCertificatesAnnouncementPredicate();
		model.addAttribute("currentOjUrl", ojUriInfo.getOfficialJournalURL());

		// From Job
		model.addAttribute("actualOjUrl", getActualOjUrl());

		return CERTIFICATE_TILE;
	}

	private String getActualOjUrl() {
		TLValidationJobSummary summary = trustedCertificateSource.getSummary();
		if (summary != null) {
			List<LOTLInfo> lotlInfos = summary.getLOTLInfos();
			for (LOTLInfo lotlInfo : lotlInfos) {
				if (Utils.areStringsEqual(lotlSource.getUrl(), lotlInfo.getUrl())) {
					ParsingInfoRecord parsingCacheInfo = lotlInfo.getParsingCacheInfo();
					if (parsingCacheInfo != null) {
						return parsingCacheInfo.getSigningCertificateAnnouncementUrl();
					}
				}
			}
		}
		return null;
	}

}