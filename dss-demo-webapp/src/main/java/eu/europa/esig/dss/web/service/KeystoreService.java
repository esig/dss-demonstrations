package eu.europa.esig.dss.web.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.model.CertificateDTO;

@Component
public class KeystoreService {

	public List<CertificateDTO> getCertificatesDTOFromKeyStore(List<CertificateToken> certificatesFromKeyStore) {
		List<CertificateDTO> list = new ArrayList<CertificateDTO>();
		for (CertificateToken certificateToken : certificatesFromKeyStore) {
			list.add(getCertificateDTO(certificateToken));
		}
		return list;
	}

	public CertificateDTO getCertificateDTO(CertificateToken certificate) {
		CertificateDTO dto = new CertificateDTO();

		dto.setDssId(certificate.getDSSIdAsString());
		dto.setIssuerName(certificate.getIssuer().getPrettyPrintRFC2253());
		dto.setSubjectName(certificate.getSubject().getPrettyPrintRFC2253());
		dto.setNotBefore(certificate.getNotBefore());
		dto.setNotAfter(certificate.getNotAfter());

		byte[] digestSHA256 = certificate.getDigest(DigestAlgorithm.SHA256);
		byte[] digestSHA1 = certificate.getDigest(DigestAlgorithm.SHA1);

		dto.setSha256Hex(getPrintableHex(digestSHA256));
		dto.setSha1Hex(getPrintableHex(digestSHA1));
		dto.setSha256Base64(Utils.toBase64(digestSHA256));
		dto.setSha1Base64(Utils.toBase64(digestSHA1));

		return dto;
	}

	/**
	 * This method adds space every two characters to the hexadecimal encoded digest
	 *
	 * @param digest
	 * @return
	 */
	private String getPrintableHex(byte[] digest) {
		String hexString = Utils.toHex(digest);
		return hexString.replaceAll("..", "$0 ");
	}

}
