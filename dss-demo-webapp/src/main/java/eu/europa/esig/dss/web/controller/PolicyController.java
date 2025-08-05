package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.exception.SourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Controller
@RequestMapping(value = "/policy")
public class PolicyController {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyController.class);

    @Autowired
    private Resource defaultPolicy;

    @Autowired(required = false)
    private Resource cryptographicSuiteXml;

    @Autowired(required = false)
    private Resource cryptographicSuiteJson;

    @RequestMapping(value = "/download-default-policy")
    public void downloadDefaultPolicy(HttpSession session, HttpServletResponse response) {
        response.setContentType(MimeTypeEnum.XML.getMimeTypeString());
        response.setHeader("Content-Disposition", "attachment; filename=" + defaultPolicy.getFilename());
        try (InputStream is = defaultPolicy.getInputStream(); OutputStream os = response.getOutputStream()) {
            Utils.copy(is, os);
        } catch (IOException e) {
            LOG.error("An error occurred while downloading a default validation policy : {}", e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/download-cryptographic-suite-xml")
    public void downloadCryptographicSuiteSampleXml(HttpSession session, HttpServletResponse response) {
        if (cryptographicSuiteXml == null) {
            throw new SourceNotFoundException("Cryptographic suite sample XML is not found!");
        }
        response.setContentType(MimeTypeEnum.XML.getMimeTypeString());
        response.setHeader("Content-Disposition", "attachment; filename=" + cryptographicSuiteXml.getFilename());
        try (InputStream is = cryptographicSuiteXml.getInputStream(); OutputStream os = response.getOutputStream()) {
            Utils.copy(is, os);
        } catch (IOException e) {
            LOG.error("An error occurred while downloading a cryptographic suite sample XML : {}", e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/download-cryptographic-suite-json")
    public void downloadCryptographicSuiteSampleJson(HttpSession session, HttpServletResponse response) {
        if (cryptographicSuiteJson == null) {
            throw new SourceNotFoundException("Cryptographic suite sample JSON is not found!");
        }
        response.setContentType(MimeTypeEnum.JSON.getMimeTypeString());
        response.setHeader("Content-Disposition", "attachment; filename=" + cryptographicSuiteJson.getFilename());
        try (InputStream is = cryptographicSuiteJson.getInputStream(); OutputStream os = response.getOutputStream()) {
            Utils.copy(is, os);
        } catch (IOException e) {
            LOG.error("An error occurred while downloading a cryptographic suite sample JSON : {}", e.getMessage(), e);
        }
    }

}
