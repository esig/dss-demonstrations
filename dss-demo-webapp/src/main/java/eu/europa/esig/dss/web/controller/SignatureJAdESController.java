package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.enumerations.JWSSerializationType;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.SigDMechanism;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.editor.EnumPropertyEditor;
import eu.europa.esig.dss.web.model.DataToSignParams;
import eu.europa.esig.dss.web.model.GetDataToSignResponse;
import eu.europa.esig.dss.web.model.SignDocumentResponse;
import eu.europa.esig.dss.web.model.SignResponse;
import eu.europa.esig.dss.web.model.SignatureJAdESForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

@Controller
@SessionAttributes(value = { "signatureJAdESForm", "signedJAdESDocument" })
@RequestMapping(value = "/sign-with-jades")
public class SignatureJAdESController extends AbstractSignatureController {

	private static final Logger LOG = LoggerFactory.getLogger(SignatureJAdESController.class);

	private static final String SIGNATURE_JAdES = "signature-jades";
	
	private static final String[] ALLOWED_FIELDS = { "documentsToSign", "jwsSerializationType", "signaturePackaging",
			"signatureLevel", "sigDMechanism", "base64UrlEncodedPayload", "base64UrlEncodedEtsiU", "digestAlgorithm",
			"signWithExpiredCertificate", "addContentTimestamp" };

	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.registerCustomEditor(JWSSerializationType.class, new EnumPropertyEditor(JWSSerializationType.class));
		webDataBinder.registerCustomEditor(SigDMechanism.class, new EnumPropertyEditor(SigDMechanism.class));
		webDataBinder.registerCustomEditor(SignatureLevel.class, new EnumPropertyEditor(SignatureLevel.class));
		webDataBinder.registerCustomEditor(SignaturePackaging.class, new EnumPropertyEditor(SignaturePackaging.class));
		webDataBinder.registerCustomEditor(DigestAlgorithm.class, new EnumPropertyEditor(DigestAlgorithm.class));
		webDataBinder.registerCustomEditor(EncryptionAlgorithm.class, new EnumPropertyEditor(EncryptionAlgorithm.class));
	}
	
	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showSignatureParameters(Model model, HttpServletRequest request) {
		SignatureJAdESForm signatureJAdESForm = new SignatureJAdESForm();

		// Pre-configure for JAdES
		signatureJAdESForm.setSignatureForm(SignatureForm.JAdES);
		signatureJAdESForm.setSignatureLevel(SignatureLevel.JAdES_BASELINE_B);
		signatureJAdESForm.setDigestAlgorithm(DigestAlgorithm.forName(defaultDigestAlgo, DigestAlgorithm.SHA256));
		signatureJAdESForm.setJwsSerializationType(JWSSerializationType.COMPACT_SERIALIZATION);

		model.addAttribute("signatureJAdESForm", signatureJAdESForm);
		return SIGNATURE_JAdES;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String sendSignatureParameters(Model model, HttpServletRequest response,
										  @ModelAttribute("signatureJAdESForm") @Valid SignatureJAdESForm signatureJAdESForm, BindingResult result) {
		if (result.hasErrors()) {
			if (LOG.isDebugEnabled()) {
				List<ObjectError> allErrors = result.getAllErrors();
				for (ObjectError error : allErrors) {
					LOG.debug(error.getDefaultMessage());
				}
			}
			return SIGNATURE_JAdES;
		}

		model.addAttribute("signatureJAdESForm", signatureJAdESForm);
		model.addAttribute("digestAlgorithm", signatureJAdESForm.getDigestAlgorithm());
		model.addAttribute("rootUrl", "sign-with-jades");
		model.addAttribute("nexuUrl", nexuUrl);
		return SIGNATURE_PROCESS;
	}

	@RequestMapping(value = "/get-data-to-sign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public GetDataToSignResponse getDataToSign(Model model, @RequestBody @Valid DataToSignParams params,
			@ModelAttribute("signatureJAdESForm") @Valid SignatureJAdESForm signatureJAdESForm, BindingResult result) {
		signatureJAdESForm.setCertificate(params.getSigningCertificate());
		signatureJAdESForm.setCertificateChain(params.getCertificateChain());

		CertificateToken signingCertificate = DSSUtils.loadCertificate(params.getSigningCertificate());
		signatureJAdESForm.setEncryptionAlgorithm(EncryptionAlgorithm.forName(signingCertificate.getPublicKey().getAlgorithm()));
		signatureJAdESForm.setSigningDate(new Date());

		if (signatureJAdESForm.isAddContentTimestamp()) {
			signatureJAdESForm.setContentTimestamp(WebAppUtils.fromTimestampToken(signingService.getContentTimestamp(signatureJAdESForm)));
		}

		model.addAttribute("signatureJAdESForm", signatureJAdESForm);

		ToBeSigned dataToSign = signingService.getDataToSign(signatureJAdESForm);
		if (dataToSign == null) {
			return null;
		}

		GetDataToSignResponse responseJson = new GetDataToSignResponse();
		responseJson.setDataToSign(dataToSign.getBytes());
		return responseJson;
	}

	@RequestMapping(value = "/sign-document", method = RequestMethod.POST)
	@ResponseBody
	public SignDocumentResponse signDocument(Model model, @RequestBody @Valid SignResponse signatureValue,
			@ModelAttribute("signatureJAdESForm") @Valid SignatureJAdESForm signatureJAdESForm, BindingResult result) {

		signatureJAdESForm.setSignatureValue(signatureValue.getSignatureValue());

		DSSDocument document = signingService.signDocument(signatureJAdESForm);
		InMemoryDocument signedJAdESDocument = new InMemoryDocument(DSSUtils.toByteArray(document), document.getName(), document.getMimeType());
		model.addAttribute("signedJAdESDocument", signedJAdESDocument);

		SignDocumentResponse signedDocumentResponse = new SignDocumentResponse();
		signedDocumentResponse.setUrlToDownload("download");
		return signedDocumentResponse;
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public String downloadSignedFile(@ModelAttribute("signedJAdESDocument") InMemoryDocument signedDocument, HttpServletResponse response) {
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

	@ModelAttribute("jwsSerializationTypes")
	public JWSSerializationType[] getJwsSerializationTypes() {
		return JWSSerializationType.values();
	}

	@ModelAttribute("signaturePackagings")
	public SignaturePackaging[] getSignaturePackagings() {
		return new SignaturePackaging[] { SignaturePackaging.ENVELOPING, SignaturePackaging.DETACHED };
	}

	@ModelAttribute("sigDMechanisms")
	public SigDMechanism[] getSigdMechanisms() {
		return new SigDMechanism[] { SigDMechanism.HTTP_HEADERS, SigDMechanism.OBJECT_ID_BY_URI, SigDMechanism.OBJECT_ID_BY_URI_HASH };
	}

	@ModelAttribute("digestAlgos")
	public DigestAlgorithm[] getDigestAlgorithms() {
		return new DigestAlgorithm[] { DigestAlgorithm.SHA256, DigestAlgorithm.SHA384, DigestAlgorithm.SHA512 };
	}

	@ModelAttribute("isMockUsed")
	public boolean isMockUsed() {
		return signingService.isMockTSPSourceUsed();
	}

}
