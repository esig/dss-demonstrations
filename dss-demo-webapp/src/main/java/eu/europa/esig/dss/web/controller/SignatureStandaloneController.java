package eu.europa.esig.dss.web.controller;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class SignatureStandaloneController {

	private static final String[] ALLOWED_FIELDS = { }; // nothing

	@Autowired
	private ServletContext servletContext;

	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(value = "/signature-standalone", method = RequestMethod.GET)
	public String getInfo(Model model) {

		String downloadsPath = servletContext.getRealPath("/downloads");

		boolean windowsMinimalExists = Files.exists(Path.of(downloadsPath, "dss-app-minimal-windows-x64.zip"));
		boolean windowsCompleteExists = Files.exists(Path.of(downloadsPath, "dss-app-complete-windows-x64.zip"));
		boolean linuxMinimalExists = Files.exists(Path.of(downloadsPath, "dss-app-minimal-linux.tar.gz"));
		boolean linuxCompleteExists = Files.exists(Path.of(downloadsPath, "dss-app-complete-linux.tar.gz"));

		model.addAttribute("windowsMinimalExists", windowsMinimalExists);
		model.addAttribute("windowsCompleteExists", windowsCompleteExists);
		model.addAttribute("linuxMinimalExists", linuxMinimalExists);
		model.addAttribute("linuxCompleteExists", linuxCompleteExists);

		return "signature-standalone";
	}

}
