package eu.europa.esig.dss.web.model.serversign;

import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;

import java.util.List;

public class CertificatesResponse extends ServerSignResponseBody {

    private static final long serialVersionUID = 3389717160527796520L;

    private String keyId;

    private byte[] certificate;

    private List<byte[]> certificateChain;

    private EncryptionAlgorithm encryptionAlgorithm;

    public CertificatesResponse() {
        // empty
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public List<byte[]> getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(List<byte[]> certificateChain) {
        this.certificateChain = certificateChain;
    }

    public EncryptionAlgorithm getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

}
