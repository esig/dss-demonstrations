package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.JWSSerializationType;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.web.model.ProcessEnum;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping(value = "/data")
public class DataController {

	private static final String[] ALLOWED_FIELDS = { "form", "process" };
	
	@InitBinder
	public void setAllowedFields(WebDataBinder webDataBinder) {
		webDataBinder.setAllowedFields(ALLOWED_FIELDS);
	}

	@RequestMapping(value = "/packagingsByForm", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<SignaturePackaging> getAllowedPackagingsByForm(@RequestParam("form") SignatureForm signatureForm) {
		List<SignaturePackaging> packagings = new ArrayList<>();
		if (signatureForm != null) {
			switch (signatureForm) {
			case CAdES:
				packagings.add(SignaturePackaging.ENVELOPING);
				packagings.add(SignaturePackaging.DETACHED);
				break;
			case PAdES:
				packagings.add(SignaturePackaging.ENVELOPED);
				break;
			case XAdES:
				packagings.add(SignaturePackaging.ENVELOPED);
				packagings.add(SignaturePackaging.ENVELOPING);
				packagings.add(SignaturePackaging.DETACHED);
				packagings.add(SignaturePackaging.INTERNALLY_DETACHED);
				break;
			case JAdES:
				packagings.add(SignaturePackaging.ENVELOPING);
				packagings.add(SignaturePackaging.DETACHED);
			default:
				break;
			}
		}
		return packagings;
	}

	@RequestMapping(value = "/levelsByForm", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<SignatureLevel> getAllowedLevelsByForm(@RequestParam("form") SignatureForm signatureForm, @RequestParam("process") ProcessEnum process) {
		List<SignatureLevel> levels = new ArrayList<>();
		if (signatureForm != null) {
			switch (signatureForm) {
			case CAdES:                
				if (ProcessEnum.SIGNATURE.equals(process) || ProcessEnum.DIGEST_SIGN.equals(process)) {
					levels.add(SignatureLevel.CAdES_BASELINE_B);
				}
				levels.add(SignatureLevel.CAdES_BASELINE_T);
				levels.add(SignatureLevel.CAdES_BASELINE_LT);
				levels.add(SignatureLevel.CAdES_BASELINE_LTA);
				break;
			case PAdES:
				if (ProcessEnum.SIGNATURE.equals(process) || ProcessEnum.DIGEST_SIGN.equals(process)) {
					levels.add(SignatureLevel.PAdES_BASELINE_B);
				}
				levels.add(SignatureLevel.PAdES_BASELINE_T);
				levels.add(SignatureLevel.PAdES_BASELINE_LT);
				levels.add(SignatureLevel.PAdES_BASELINE_LTA);
				break;
			case XAdES:
				if (ProcessEnum.SIGNATURE.equals(process) || ProcessEnum.DIGEST_SIGN.equals(process)) {
					levels.add(SignatureLevel.XAdES_BASELINE_B);
				}
				levels.add(SignatureLevel.XAdES_BASELINE_T);
				levels.add(SignatureLevel.XAdES_BASELINE_LT);
				if (!ProcessEnum.DIGEST_SIGN.equals(process)) {
					levels.add(SignatureLevel.XAdES_BASELINE_LTA);
				}
				break;
			case JAdES:
				if (ProcessEnum.SIGNATURE.equals(process) || ProcessEnum.DIGEST_SIGN.equals(process)) {
					levels.add(SignatureLevel.JAdES_BASELINE_B);
				}
				levels.add(SignatureLevel.JAdES_BASELINE_T);
				levels.add(SignatureLevel.JAdES_BASELINE_LT);
				if (!ProcessEnum.DIGEST_SIGN.equals(process)) {
					levels.add(SignatureLevel.JAdES_BASELINE_LTA);
				}
				break;
			default:
				break;
			}
		}
		return levels;
	}

	@RequestMapping(value = "/digestAlgosByForm", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<DigestAlgorithm> getAllowedDigestAlgorithmsBySignatureForm(@RequestParam("form") SignatureForm signatureForm) {
		if (SignatureForm.JAdES.equals(signatureForm)) {
			return Arrays.asList(DigestAlgorithm.SHA256, DigestAlgorithm.SHA384, DigestAlgorithm.SHA512);
		} else {
			return Arrays.asList(DigestAlgorithm.SHA1, DigestAlgorithm.SHA256, DigestAlgorithm.SHA384, DigestAlgorithm.SHA512);
		}
	}

	@RequestMapping(value = "/levelsBySerialization", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<SignatureLevel> getAllowedLevelsByJWSSerialization(@RequestParam("serializationType") JWSSerializationType jwsSerializationType) {
		List<SignatureLevel> levels = new ArrayList<>();
		if (jwsSerializationType != null) {
			switch (jwsSerializationType) {
			case COMPACT_SERIALIZATION:
				levels.add(SignatureLevel.JAdES_BASELINE_B);
				break;
			case JSON_SERIALIZATION:
			case FLATTENED_JSON_SERIALIZATION:
				levels.add(SignatureLevel.JAdES_BASELINE_B);
				levels.add(SignatureLevel.JAdES_BASELINE_T);
				levels.add(SignatureLevel.JAdES_BASELINE_LT);
				levels.add(SignatureLevel.JAdES_BASELINE_LTA);
				break;
			default:
				break;
			}
		}
		return levels;
	}
	
}
