package eu.europa.esig.dss.web.controller;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.europa.esig.dss.spi.tsl.LOTLInfo;
import eu.europa.esig.dss.spi.tsl.TLInfo;
import eu.europa.esig.dss.spi.tsl.TLValidationJobSummary;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;

@Controller
@RequestMapping(value = "/tsl-info")
public class TrustedListController {

	private static final Logger LOG = LoggerFactory.getLogger(TrustedListController.class);
	
	private static final String TL_SUMMARY = "tl-summary";
	private static final String PIVOT_CHANGES = "pivot-changes";
	private static final String ERROR = "error";
	private static final String TL_DATA = "tl-info-country";
	private static final String LOTL_DATA = "lotl-info";

	@Autowired
	private TrustedListsCertificateSource trustedCertificateSource;

	@RequestMapping(method = RequestMethod.GET)
	public String tlInfoPage(Model model, HttpServletRequest request) {
		TLValidationJobSummary summary = trustedCertificateSource.getSummary();
		model.addAttribute("summary", summary);
		return TL_SUMMARY;
	}
	

	@RequestMapping(method = RequestMethod.GET, value = "/lotl/{id}")
	public String lotlInfoPaget(@PathVariable(value = "id") String id, Model model, HttpServletRequest request) {
		TLValidationJobSummary summary = trustedCertificateSource.getSummary();
		List<LOTLInfo> lotlInfos = summary.getLOTLInfos();
		for(LOTLInfo lotlInfo : lotlInfos) {
			if(lotlInfo.getIdentifier().asXmlId().equals(id)) 
				model.addAttribute("lotlInfo", lotlInfo);
		}
		
		return LOTL_DATA;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/tl/{id}")
	public String tlInfoPageByCountry(@PathVariable(value = "id") String id, Model model, HttpServletRequest request) {
		TLValidationJobSummary summary = trustedCertificateSource.getSummary();
		List<LOTLInfo> lotlInfos = summary.getLOTLInfos();
		for(LOTLInfo lotlInfo : lotlInfos) {
			List<TLInfo> tlInfos = lotlInfo.getTLInfos();
			for(TLInfo tlInfo: tlInfos) {
				if(tlInfo.getIdentifier().asXmlId().equals(id))
					model.addAttribute("tlInfo", tlInfo);
			}
		}
		
		return TL_DATA;
	}
	
	
	@RequestMapping(value = "/pivot-changes/{lotlId}", method = RequestMethod.GET)
	public String getPivotChangesPage(@PathVariable("lotlId") String lotlId, Model model) {
		LOTLInfo lotlInfo = getLOTLInfoById(lotlId);
		if (lotlInfo != null) {
			model.addAttribute("lotl", lotlInfo);
			model.addAttribute("originalKeystore", lotlInfo.getValidationCacheInfo().isResultExist() ? 
					lotlInfo.getValidationCacheInfo().getPotentialSigners() : Collections.emptyList());
			return PIVOT_CHANGES;
		} else {
			model.addAttribute("error", "The requested LOTL does not exist!");
			return ERROR;
		}
	}
	
	private LOTLInfo getLOTLInfoById(String lotlId) {
		TLValidationJobSummary summary = trustedCertificateSource.getSummary();
		List<LOTLInfo> lotlInfos = summary.getLOTLInfos();
		for (LOTLInfo lotlInfo : lotlInfos) {
			if (lotlInfo.getIdentifier().asXmlId().equals(lotlId)) {
				return lotlInfo;
			}
		}
		LOG.warn("The LOTL with the specified id [{}] is not found!", lotlId);
		return null;
	}

}
