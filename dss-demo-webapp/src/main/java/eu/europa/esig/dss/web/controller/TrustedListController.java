package eu.europa.esig.dss.web.controller;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import eu.europa.esig.dss.web.exception.SourceNotFoundException;

@Controller
@RequestMapping(value = "/tl-info")
public class TrustedListController {
	
	private static final String TL_SUMMARY = "tl-summary";
	private static final String PIVOT_CHANGES = "pivot-changes";
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
	public String lotlInfoPage(@PathVariable(value = "id") String id, Model model, HttpServletRequest request) {
		LOTLInfo lotlInfo = getLOTLInfoById(id);
		if (lotlInfo == null) {
			throw new SourceNotFoundException(String.format("The LOTL with the specified id [%s] is not found!", id));
		}
		model.addAttribute("lotlInfo", lotlInfo);
		return LOTL_DATA;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/tl/{id}")
	public String tlInfoPageByCountry(@PathVariable(value = "id") String id, Model model, HttpServletRequest request) {
		TLInfo tlInfo = getTLInfoById(id);
		if (tlInfo == null) {
			throw new SourceNotFoundException(String.format("The TL with the specified id [%s] is not found!", id));
		}
		model.addAttribute("tlInfo", tlInfo);
		return TL_DATA;
	}
	
	
	@RequestMapping(value = "/pivot-changes/{lotlId}", method = RequestMethod.GET)
	public String getPivotChangesPage(@PathVariable("lotlId") String lotlId, Model model) {
		LOTLInfo lotlInfo = getLOTLInfoById(lotlId);
		if (lotlInfo != null) {
			model.addAttribute("lotl", lotlInfo);
			model.addAttribute("potentialSigners", lotlInfo.getValidationCacheInfo().isResultExist() ?
					lotlInfo.getValidationCacheInfo().getPotentialSigners() : Collections.emptyList());
			return PIVOT_CHANGES;
		} else {
			throw new SourceNotFoundException(String.format("The requested LOTL with id [%s] does not exist!", lotlId));
		}
	}
	
	private LOTLInfo getLOTLInfoById(String lotlId) {
		TLValidationJobSummary summary = trustedCertificateSource.getSummary();
		List<LOTLInfo> lotlInfos = summary.getLOTLInfos();
		for (LOTLInfo lotlInfo : lotlInfos) {
			if (lotlInfo.getDSSId().asXmlId().equals(lotlId)) {
				return lotlInfo;
			}
		}
		return null;
	}
	
	private TLInfo getTLInfoById(String tlId) {
		TLValidationJobSummary summary = trustedCertificateSource.getSummary();
		List<LOTLInfo> lotlInfos = summary.getLOTLInfos();
		for (LOTLInfo lotlInfo : lotlInfos) {
			TLInfo tlInfo = getTLInfoByIdFromList(tlId, lotlInfo.getTLInfos());
			if (tlInfo != null) {
				return tlInfo;
			}
		}
		List<TLInfo> otherTLInfos = summary.getOtherTLInfos();
		TLInfo tlInfo = getTLInfoByIdFromList(tlId, otherTLInfos);
		if (tlInfo != null) {
			return tlInfo;
		}
		return null;
	}
	
	private TLInfo getTLInfoByIdFromList(String tlId, List<TLInfo> tlInfos) {
		for (TLInfo tlInfo: tlInfos) {
			if (tlInfo.getDSSId().asXmlId().equals(tlId)) {
				return tlInfo;
			}
		}
		return null;
	}

}
