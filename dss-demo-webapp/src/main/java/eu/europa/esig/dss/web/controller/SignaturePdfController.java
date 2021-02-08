package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.model.DataToSignParams;
import eu.europa.esig.dss.web.model.GetDataToSignResponse;
import eu.europa.esig.dss.web.model.SignDocumentResponse;
import eu.europa.esig.dss.web.model.SignatureDocumentForm;
import eu.europa.esig.dss.web.model.SignatureValueAsString;
import eu.europa.esig.dss.web.service.SigningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

@Controller
@SessionAttributes(value = { "signaturePdfForm", "signedPdfDocument" })
@RequestMapping(value = "/sign-a-pdf")
public class SignaturePdfController {

	private static final Logger LOG = LoggerFactory.getLogger(SignaturePdfController.class);

	private static final String SIGNATURE_PDF_PARAMETERS = "signature-pdf";
	private static final String SIGNATURE_PROCESS = "nexu-signature-process";
	
	private static final String[] ALLOWED_FIELDS = { "documentToSign" };

	@Value("${nexuUrl}")
	private String nexuUrl;

	@Value("${nexuDownloadUrl}")
	private String nexuDownloadUrl;

	@Autowired
	private SigningService signingService;
	
	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showSignatureParameters(Model model, HttpServletRequest request) {
		SignatureDocumentForm signaturePdfForm = new SignatureDocumentForm();

		// Pre-configure for PAdES
		signaturePdfForm.setSignatureForm(SignatureForm.PAdES);
		signaturePdfForm.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
		signaturePdfForm.setDigestAlgorithm(DigestAlgorithm.SHA256);
		signaturePdfForm.setSignaturePackaging(SignaturePackaging.ENVELOPED);

		model.addAttribute("signaturePdfForm", signaturePdfForm);
		model.addAttribute("nexuDownloadUrl", nexuDownloadUrl);
		return SIGNATURE_PDF_PARAMETERS;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String sendSignatureParameters(Model model, HttpServletRequest response,
			@ModelAttribute("signaturePdfForm") @Valid SignatureDocumentForm signaturePdfForm, BindingResult result) {
		if (result.hasErrors()) {
			if (LOG.isDebugEnabled()) {
				List<ObjectError> allErrors = result.getAllErrors();
				for (ObjectError error : allErrors) {
					LOG.debug(error.getDefaultMessage());
				}
			}
			return SIGNATURE_PDF_PARAMETERS;
		}

		model.addAttribute("signaturePdfForm", signaturePdfForm);
		model.addAttribute("digestAlgorithm", signaturePdfForm.getDigestAlgorithm());
		model.addAttribute("rootUrl", "sign-a-pdf");
		model.addAttribute("nexuUrl", nexuUrl);
		return SIGNATURE_PROCESS;
	}

	@RequestMapping(value = "/get-data-to-sign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public GetDataToSignResponse getDataToSign(Model model, @RequestBody @Valid DataToSignParams params,
			@ModelAttribute("signaturePdfForm") @Valid SignatureDocumentForm signaturePdfForm, BindingResult result) {

		signaturePdfForm.setBase64Certificate(params.getSigningCertificate());
		signaturePdfForm.setBase64CertificateChain(params.getCertificateChain());
		signaturePdfForm.setEncryptionAlgorithm(params.getEncryptionAlgorithm());
		signaturePdfForm.setSigningDate(new Date());

		model.addAttribute("signaturePdfForm", signaturePdfForm);

		ToBeSigned dataToSign = signingService.getDataToSign(signaturePdfForm);
		if (dataToSign == null) {
			return null;
		}

		GetDataToSignResponse responseJson = new GetDataToSignResponse();
		responseJson.setDataToSign(DatatypeConverter.printBase64Binary(dataToSign.getBytes()));
		return responseJson;
	}

	@RequestMapping(value = "/sign-document", method = RequestMethod.POST)
	@ResponseBody
	public SignDocumentResponse signDocument(Model model, @RequestBody @Valid SignatureValueAsString signatureValue,
			@ModelAttribute("signaturePdfForm") @Valid SignatureDocumentForm signaturePdfForm, BindingResult result) {

		signaturePdfForm.setBase64SignatureValue(signatureValue.getSignatureValue());

		DSSDocument document = signingService.signDocument(signaturePdfForm);
		InMemoryDocument signedPdfDocument = new InMemoryDocument(DSSUtils.toByteArray(document), document.getName(), document.getMimeType());
		model.addAttribute("signedPdfDocument", signedPdfDocument);

		SignDocumentResponse signedDocumentResponse = new SignDocumentResponse();
		signedDocumentResponse.setUrlToDownload("download");
		return signedDocumentResponse;
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public String downloadSignedFile(@ModelAttribute("signedPdfDocument") InMemoryDocument signedDocument, HttpServletResponse response) {
		try {
			MimeType mimeType = signedDocument.getMimeType();
			if (mimeType != null) {
				response.setContentType(mimeType.getMimeTypeString());
			}
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + signedDocument.getName() + "\"");
			Utils.copy(new ByteArrayInputStream(signedDocument.getBytes()), response.getOutputStream());

		} catch (Exception e) {
			LOG.error("An error occurred while pushing file in response : " + e.getMessage(), e);
		}
		return null;
	}

	@ModelAttribute("isMockUsed")
	public boolean isMockUsed() {
		return signingService.isMockTSPSourceUsed();
	}

}
