package eu.europa.esig.dss.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.TimestampContainerForm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.editor.ASiCContainerTypePropertyEditor;
import eu.europa.esig.dss.web.model.TimestampForm;
import eu.europa.esig.dss.web.service.SigningService;

@Controller
@RequestMapping(value = "/timestamp-a-document")
public class TimestampController {

	private static final Logger LOG = LoggerFactory.getLogger(TimestampController.class);

	private static final String TIMESTAMP_TILE = "timestamp";
	
	private static final String[] ALLOWED_FIELDS = { "originalFiles", "containerType" };

	@Autowired
	private SigningService signingService;

	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.registerCustomEditor(ASiCContainerType.class, new ASiCContainerTypePropertyEditor());
	}
	
	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showTimestampParameters(Model model) {
		TimestampForm timestampForm = new TimestampForm();
		model.addAttribute("timestampForm", timestampForm);
		return TIMESTAMP_TILE;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String timestamp(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("timestampForm") @Valid TimestampForm timestampForm,
			BindingResult result) {
		if (result.hasErrors()) {
			if (LOG.isDebugEnabled()) {
				List<ObjectError> allErrors = result.getAllErrors();
				for (ObjectError error : allErrors) {
					LOG.debug(error.getDefaultMessage());
				}
			}
			return TIMESTAMP_TILE;
		}
		
		DSSDocument timestampedDocument = signingService.timestamp(timestampForm);

		response.setContentType(timestampedDocument.getMimeType().getMimeTypeString());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + timestampedDocument.getName() + "\"");
		try {
			Utils.copy(timestampedDocument.openStream(), response.getOutputStream());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	@ModelAttribute("timestampContainerForms")
	public TimestampContainerForm[] getTimestampContainerForms() {
		return TimestampContainerForm.values();
	}

	@ModelAttribute("isMockUsed")
	public boolean isMockUsed() {
		return signingService.isMockTSPSourceUsed();
	}

}
