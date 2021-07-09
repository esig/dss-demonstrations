package eu.europa.esig.dss.standalone.task;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.scene.control.ChoiceDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

public class SelectCertificateTask implements Callable<DSSPrivateKeyEntry> {

	private List<DSSPrivateKeyEntry> keys;

	public SelectCertificateTask(List<DSSPrivateKeyEntry> keys) {
		this.keys = keys;
	}

	@Override
	public DSSPrivateKeyEntry call() {
		Map<String, DSSPrivateKeyEntry> map = new HashMap<>();
		for (DSSPrivateKeyEntry dssPrivateKeyEntry : keys) {
			CertificateToken certificate = dssPrivateKeyEntry.getCertificate();
			String text = DSSASN1Utils.getHumanReadableName(certificate) + " (" + certificate.getSerialNumber() + ")";
			map.put(text, dssPrivateKeyEntry);
		}
		Set<String> keySet = map.keySet();
		ChoiceDialog<String> dialog = new ChoiceDialog<>(keySet.iterator().next(), keySet);
		dialog.setHeaderText("Select your certificate");
		Optional<String> result = dialog.showAndWait();

		if (result.isPresent()) {
			return map.get(result.get());
		}
		return null;
	}

}
