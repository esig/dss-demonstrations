package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.KeyStoreSignatureTokenConnection;
import eu.europa.esig.dss.web.model.SignResponse;
import eu.europa.esig.dss.web.model.serversign.TokenId;
import eu.europa.esig.dss.web.model.serversign.SignRequest;
import eu.europa.esig.dss.web.model.serversign.CertificatesResponse;
import eu.europa.esig.dss.web.model.serversign.ServerSignResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/server-sign")
public class ServerSignController {

    @Autowired
    protected KeyStoreSignatureTokenConnection remoteToken;

    @RequestMapping(value = "/certificates", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ServerSignResponse certificates() {

        ServerSignResponse serverSignResponse = new ServerSignResponse();

        CertificatesResponse certificatesResponse = new CertificatesResponse();
        certificatesResponse.setTokenId(new TokenId());

        CertificateToken signingCertificate = getSigningCertificate();
        certificatesResponse.setKeyId(signingCertificate.getDSSIdAsString());
        certificatesResponse.setCertificate(signingCertificate.getEncoded());
        certificatesResponse.setCertificateChain(getCertificateChain());
        certificatesResponse.setEncryptionAlgorithm(getEncryptionAlgorithm());

        serverSignResponse.setResponse(certificatesResponse);

        return serverSignResponse;
    }

    @RequestMapping(value = "/sign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ServerSignResponse sign(@RequestBody SignRequest signRequest) {

        ServerSignResponse serverSignResponse = new ServerSignResponse();

        SignResponse signResponse = new SignResponse();
        signResponse.setTokenId(signRequest.getTokenId());
        signResponse.setSignatureValue(sign(signRequest.getToBeSigned(), signRequest.getDigestAlgorithm()));

        serverSignResponse.setResponse(signResponse);

        return serverSignResponse;
    }

    protected CertificateToken getSigningCertificate() {
        return getKey().getCertificate();
    }

    protected List<byte[]> getCertificateChain() {
        return Arrays.stream(getKey().getCertificateChain()).map(CertificateToken::getEncoded).collect(Collectors.toList());
    }

    protected EncryptionAlgorithm getEncryptionAlgorithm() {
        return getKey().getEncryptionAlgorithm();
    }

    protected byte[] sign(ToBeSigned dtbs, DigestAlgorithm digestAlgorithm) {
        return remoteToken.sign(dtbs, digestAlgorithm, getKey()).getValue();
    }

    private DSSPrivateKeyEntry getKey() {
        return remoteToken.getKeys().iterator().next();
    }

}
