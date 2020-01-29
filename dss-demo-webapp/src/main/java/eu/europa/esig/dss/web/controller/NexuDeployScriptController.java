package eu.europa.esig.dss.web.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Controller
public class NexuDeployScriptController {

	@Value("${nexuDownloadUrl}")
	private String nexuDownloadUrl;

	@Value("${nexuVersion}")
	private String nexuVersion;

	@Value("${nexuUrl}")
	private String nexuUrl;

	private Template template;

	public NexuDeployScriptController() {
		try {
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
			cfg.setClassForTemplateLoading(getClass(), "/");
			this.template = cfg.getTemplate("nexu_deploy.ftl.js", "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	@RequestMapping("/js/nexu-deploy.js")
	public ResponseEntity<String> loadScript() throws Exception {

		StringWriter outWriter = new StringWriter();

		Map<String, String> model = new HashMap<String, String>();

		model.put("nexuDownloadUrl", nexuDownloadUrl);
		model.put("nexuVersion", nexuVersion);
		model.put("nexuUrl", nexuUrl);

		template.process(model, outWriter);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("text/javascript"));

		return new ResponseEntity<String>(outWriter.toString(), headers, HttpStatus.ACCEPTED);
	}
}
