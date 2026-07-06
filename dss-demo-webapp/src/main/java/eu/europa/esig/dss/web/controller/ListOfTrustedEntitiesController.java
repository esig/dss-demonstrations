package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.model.lote.LoLoTEInfo;
import eu.europa.esig.dss.model.lote.LoTEInfo;
import eu.europa.esig.dss.model.lote.LoTEValidationJobSummary;
import eu.europa.esig.dss.spi.lote.TrustedEntitiesCertificateSource;
import eu.europa.esig.dss.web.exception.SourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping(value = "/lote-info")
public class ListOfTrustedEntitiesController {
	
	private static final String LoTE_SUMMARY = "lote-summary";
	private static final String LoTE_DATA = "lote-info";
	private static final String LoLoTE_DATA = "lolote-info";

	private static final String[] ALLOWED_FIELDS = { }; // nothing

	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@Autowired
	private TrustedEntitiesCertificateSource trustedCertificateSource;

	@RequestMapping(method = RequestMethod.GET)
	public String loteInfoPage(Model model, HttpServletRequest request) {
		LoTEValidationJobSummary summary = trustedCertificateSource.getSummary();
		model.addAttribute("summary", summary);
		return LoTE_SUMMARY;
	}
	

	@RequestMapping(method = RequestMethod.GET, value = "/lolote/{id}")
	public String loloteInfoPage(@PathVariable(value = "id") String id, Model model, HttpServletRequest request) {
		LoLoTEInfo loloteInfo = getLoLoTEInfoById(id);
		if (loloteInfo == null) {
			throw new SourceNotFoundException(String.format("The LoLoTE with the specified id [%s] is not found!", id));
		}
		model.addAttribute("loloteInfo", loloteInfo);
		return LoLoTE_DATA;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/lote/{id}")
	public String loteInfoPageByCountry(@PathVariable(value = "id") String id, Model model, HttpServletRequest request) {
		LoTEInfo loteInfo = getLoTEInfoById(id);
		if (loteInfo == null) {
			throw new SourceNotFoundException(String.format("The LoTE with the specified id [%s] is not found!", id));
		}
		model.addAttribute("loteInfo", loteInfo);
		return LoTE_DATA;
	}
	private LoLoTEInfo getLoLoTEInfoById(String loloteId) {
		LoTEValidationJobSummary summary = trustedCertificateSource.getSummary();
		List<LoLoTEInfo> loloteInfos = summary.getLoLoTEInfos();
		for (LoLoTEInfo loloteInfo : loloteInfos) {
			if (loloteInfo.getDSSId().asXmlId().equals(loloteId)) {
				return loloteInfo;
			}
		}
		return null;
	}
	
	private LoTEInfo getLoTEInfoById(String loteId) {
		LoTEValidationJobSummary summary = trustedCertificateSource.getSummary();
		List<LoLoTEInfo> loloteInfos = summary.getLoLoTEInfos();
		for (LoLoTEInfo loloteInfo : loloteInfos) {
			LoTEInfo loteInfo = getLoTEInfoByIdFromList(loteId, loloteInfo.getChildrenInfos());
			if (loteInfo != null) {
				return loteInfo;
			}
		}
		List<LoTEInfo> otherLoTEInfos = summary.getOtherLoTEInfos();
		LoTEInfo loteInfo = getLoTEInfoByIdFromList(loteId, otherLoTEInfos);
		if (loteInfo != null) {
			return loteInfo;
		}
		return null;
	}
	
	private LoTEInfo getLoTEInfoByIdFromList(String loteId, List<LoTEInfo> loteInfos) {
		for (LoTEInfo loteInfo: loteInfos) {
			if (loteInfo.getDSSId().asXmlId().equals(loteId)) {
				return loteInfo;
			}
		}
		return null;
	}

}
