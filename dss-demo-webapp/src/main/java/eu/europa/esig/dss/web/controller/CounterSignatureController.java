package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.AdvancedSignature;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.editor.EnumPropertyEditor;
import eu.europa.esig.dss.web.exception.SignatureOperationException;
import eu.europa.esig.dss.web.model.CounterSignatureForm;
import eu.europa.esig.dss.web.model.CounterSignatureHelperResponse;
import eu.europa.esig.dss.web.model.DataToSignParams;
import eu.europa.esig.dss.web.model.GetDataToSignResponse;
import eu.europa.esig.dss.web.model.SignDocumentResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@SessionAttributes(value = { "counterSignatureForm", "signedDocument" })
@RequestMapping(value = "/counter-sign")
public class CounterSignatureController {

	private static final Logger LOG = LoggerFactory.getLogger(CounterSignatureController.class);

	private static final String COUNTER_SIGN = "counter-signature";
	private static final String SIGNATURE_PROCESS = "nexu-signature-process";
	
	private static final String[] ALLOWED_FIELDS = { "counterSignatureForm", "documentToCounterSign", "signatureIdToCounterSign", 
			"signatureForm", "signatureLevel", "digestAlgorithm", "signWithExpiredCertificate" };

	@Value("${nexuUrl}")
	private String nexuUrl;

	@Value("${nexuDownloadUrl}")
	private String nexuDownloadUrl;

	@Value("${default.digest.algo}")
	private String defaultDigestAlgo;

	@Autowired
	private SigningService signingService;

	@Autowired
	protected CertificateVerifier certificateVerifier;

	@InitBinder
	public void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.registerCustomEditor(SignatureForm.class, new EnumPropertyEditor(SignatureForm.class));
		webDataBinder.registerCustomEditor(SignatureLevel.class, new EnumPropertyEditor(SignatureLevel.class));
		webDataBinder.registerCustomEditor(DigestAlgorithm.class, new EnumPropertyEditor(DigestAlgorithm.class));
		webDataBinder.registerCustomEditor(EncryptionAlgorithm.class, new EnumPropertyEditor(EncryptionAlgorithm.class));
	}
	
	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String showSignatureParameters(Model model, HttpServletRequest request) {
		CounterSignatureForm counterSignatureForm = new CounterSignatureForm();
		counterSignatureForm.setDigestAlgorithm(DigestAlgorithm.forName(defaultDigestAlgo, DigestAlgorithm.SHA256));
		model.addAttribute("counterSignatureForm", counterSignatureForm);
		model.addAttribute("nexuDownloadUrl", nexuDownloadUrl);
		return COUNTER_SIGN;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String sendSignatureParameters(Model model, HttpServletRequest response,
			@ModelAttribute("counterSignatureForm") @Valid CounterSignatureForm counterSignatureForm, BindingResult result) {
		if (result.hasErrors()) {
			if (LOG.isDebugEnabled()) {
				List<ObjectError> allErrors = result.getAllErrors();
				for (ObjectError error : allErrors) {
					LOG.debug(error.getDefaultMessage());
				}
			}
			return COUNTER_SIGN;
		}
		model.addAttribute("counterSignatureForm", counterSignatureForm);
		model.addAttribute("digestAlgorithm", counterSignatureForm.getDigestAlgorithm());
		model.addAttribute("rootUrl", "counter-sign");
		model.addAttribute("nexuUrl", nexuUrl);
		return SIGNATURE_PROCESS;
	}

	@RequestMapping(value = "/get-data-to-sign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public GetDataToSignResponse getDataToCounterSign(Model model, @RequestBody @Valid DataToSignParams params,
			@ModelAttribute("counterSignatureForm") @Valid CounterSignatureForm counterSignatureForm, BindingResult result) {
		counterSignatureForm.setBase64Certificate(params.getSigningCertificate());
		counterSignatureForm.setBase64CertificateChain(params.getCertificateChain());
		CertificateToken signingCertificate = DSSUtils.loadCertificateFromBase64EncodedString(params.getSigningCertificate());
		counterSignatureForm.setEncryptionAlgorithm(EncryptionAlgorithm.forName(signingCertificate.getPublicKey().getAlgorithm()));
		counterSignatureForm.setSigningDate(new Date());

		model.addAttribute("counterSignatureForm", counterSignatureForm);

		ToBeSigned dataToSign = signingService.getDataToCounterSign(counterSignatureForm);
		if (dataToSign == null) {
			return null;
		}

		GetDataToSignResponse responseJson = new GetDataToSignResponse();
		responseJson.setDataToSign(DatatypeConverter.printBase64Binary(dataToSign.getBytes()));
		return responseJson;
	}

	@RequestMapping(value = "/sign-document", method = RequestMethod.POST)
	@ResponseBody
	public SignDocumentResponse counterSignSignature(Model model, @RequestBody @Valid SignatureValueAsString signatureValue,
			@ModelAttribute("counterSignatureForm") @Valid CounterSignatureForm counterSignatureForm, BindingResult result) {

		counterSignatureForm.setBase64SignatureValue(signatureValue.getSignatureValue());

		DSSDocument document = signingService.counterSignSignature(counterSignatureForm);
		InMemoryDocument signedDocument = new InMemoryDocument(DSSUtils.toByteArray(document), document.getName(), document.getMimeType());
		model.addAttribute("signedDocument", signedDocument);

		SignDocumentResponse signedDocumentResponse = new SignDocumentResponse();
		signedDocumentResponse.setUrlToDownload("download");
		return signedDocumentResponse;
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public String downloadSignedFile(@ModelAttribute("signedDocument") InMemoryDocument signedDocument, HttpServletResponse response) {
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

	@RequestMapping(value = "/signatureIds", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	@ResponseBody
	public CounterSignatureHelperResponse getSignatureIds(@RequestParam("documentToCounterSign") MultipartFile documentToCounterSign) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Extraction of signatures...");
		}
		
		try {
			DSSDocument toCounterSignDSSDocument = WebAppUtils.toDSSDocument(documentToCounterSign);
			SignedDocumentValidator validator = SignedDocumentValidator.fromDocument(toCounterSignDSSDocument);
			validator.setCertificateVerifier(certificateVerifier);
			
			List<AdvancedSignature> signatures = validator.getSignatures();
			if (Utils.isCollectionNotEmpty(signatures)) {
				CounterSignatureHelperResponse counterSignatureResponse = new CounterSignatureHelperResponse();
				counterSignatureResponse.setSignatureIds(getSignatureIds(signatures));
				SignatureForm signatureForm = getSignatureForm(signatures);
				counterSignatureResponse.setSignatureForm(signatureForm);
				counterSignatureResponse.setSignatureLevels(getSignatureLevels(signatureForm));
				
				return counterSignatureResponse;
			}
			
			throw new DSSException("The uploaded file does not contain signatures.");
		
		} catch (Exception e) {
			throw new SignatureOperationException(e.getMessage(), e);
		}
	}
	
	private List<String> getSignatureIds(List<AdvancedSignature> signatures) {
		List<String> signatureIds = new ArrayList<>();
		for (AdvancedSignature signature : signatures) {
			extractIdsRecursively(signature, signatureIds);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("The following signatures found : " + signatureIds);
		}
		
		return signatureIds;
	}
	
	private void extractIdsRecursively(AdvancedSignature signature, List<String> signatureIds) {
		String id = Utils.isStringNotEmpty(signature.getDAIdentifier()) ? signature.getDAIdentifier() : signature.getId();
		signatureIds.add(id);
		for (AdvancedSignature counterSignature : signature.getCounterSignatures()) {
			extractIdsRecursively(counterSignature, signatureIds);
		}
	}
	
	private SignatureForm getSignatureForm(List<AdvancedSignature> advancedSignatures) {
		AdvancedSignature advancedSignature = advancedSignatures.iterator().next();
		return advancedSignature.getSignatureForm();
	}
	
	private List<SignatureLevel> getSignatureLevels(SignatureForm signatureForm) {
		List<SignatureLevel> levels = new ArrayList<>();
		switch (signatureForm) {
			case CAdES:
				levels.add(SignatureLevel.CAdES_BASELINE_B);
				// TODO : add a support of T+ levels
				break;
			case XAdES:
				levels.add(SignatureLevel.XAdES_BASELINE_B);
				levels.add(SignatureLevel.XAdES_BASELINE_T);
				levels.add(SignatureLevel.XAdES_BASELINE_LT);
				levels.add(SignatureLevel.XAdES_BASELINE_LTA);
				break;
			case JAdES:
				levels.add(SignatureLevel.JAdES_BASELINE_B);
				levels.add(SignatureLevel.JAdES_BASELINE_T);
				levels.add(SignatureLevel.JAdES_BASELINE_LT);
				levels.add(SignatureLevel.JAdES_BASELINE_LTA);
				break;
			default:
				throw new DSSException(String.format("Counter signature is not supported with %s!", signatureForm));
		}
		return levels;
	}

	@ModelAttribute("digestAlgos")
	public DigestAlgorithm[] getDigestAlgorithms() {
		DigestAlgorithm[] algos = new DigestAlgorithm[] { DigestAlgorithm.SHA1, DigestAlgorithm.SHA256, DigestAlgorithm.SHA384,
				DigestAlgorithm.SHA512 };
		return algos;
	}

}
