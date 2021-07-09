package eu.europa.esig.dss.standalone.controller;

import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureForm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.SignatureTokenType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.MimeType;
import eu.europa.esig.dss.standalone.fx.FileToStringConverter;
import eu.europa.esig.dss.standalone.model.SignatureModel;
import eu.europa.esig.dss.standalone.task.JobBuilder;
import eu.europa.esig.dss.standalone.task.RefreshLOTLTask;
import eu.europa.esig.dss.standalone.task.SigningTask;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.utils.Utils;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SignatureController implements Initializable {

	private static final Logger LOG = LoggerFactory.getLogger(SignatureController.class);
	
	private final String nbCertificatesTest = "Number of Trusted Certificates : ";
	
	private final List<DigestAlgorithm> supportedDigestAlgorithms = Arrays.asList(DigestAlgorithm.SHA1, DigestAlgorithm.SHA224, DigestAlgorithm.SHA256, 
			DigestAlgorithm.SHA384, DigestAlgorithm.SHA512, DigestAlgorithm.SHA3_224, DigestAlgorithm.SHA3_256, DigestAlgorithm.SHA3_384, DigestAlgorithm.SHA3_512);
	
	/** A list of DigestAlgorithms supported by the current chosen SignatureFormat */
	private List<DigestAlgorithm> sigFormSupportedDigestAlgorithms;

	/** A list of DigestAlgorithms supported by the current chosen SignatureTokenType */
	private List<DigestAlgorithm> sigTokenTypeSupportedDigestAlgorithms;

	@FXML
	private Button fileSelectButton;

	@FXML
	private RadioButton asicsRadio;

	@FXML
	private RadioButton asiceRadio;

	@FXML
	private ToggleGroup toggleAsicContainerType;

	@FXML
	private ToggleGroup toogleSigFormat;

	@FXML
	private ToggleGroup toggleSigPackaging;

	@FXML
	private RadioButton cadesRadio;

	@FXML
	private RadioButton padesRadio;

	@FXML
	private RadioButton xadesRadio;

	@FXML
	private RadioButton jadesRadio;

	@FXML
	private HBox hSignaturePackaging;
	
	@FXML
	private HBox hBoxDigestAlgos;

	@FXML
	private RadioButton envelopedRadio;

	@FXML
	private RadioButton envelopingRadio;

	@FXML
	private RadioButton detachedRadio;

	@FXML
	private RadioButton internallyDetachedRadio;

	@FXML
	private ComboBox<SignatureLevel> comboLevel;

	@FXML
	private Label warningLabel;

	@FXML
	private ToggleGroup toggleDigestAlgo;

	@FXML
	private ToggleGroup toggleSigToken;

	@FXML
	private RadioButton pkcs11Radio;

	@FXML
	private RadioButton pkcs12Radio;
	
	@FXML
	private RadioButton mscapiRadio;

	@FXML
	private HBox hPkcsFile;

	@FXML
	private Label labelPkcs11File;

	@FXML
	private Label labelPkcs12File;

	@FXML
	private HBox hPkcsPassword;

	@FXML
	private Button pkcsFileButton;

	@FXML
	private PasswordField pkcsPassword;

	@FXML
	private Button signButton;

	@FXML
	private ProgressIndicator progressSign;
	
	@FXML
	private Button refreshLOTL;
	
	@FXML
	private HBox refreshBox;
	
	@FXML
	private Label nbCertificates;
	
	private ProgressIndicator progressRefreshLOTL;
	
	private JobBuilder jobBuilder;
	
	private TLValidationJob tlValidationJob;
		
	private Stage stage;

	private SignatureModel model;
			
	static {
		// Fix a freeze in Windows 10, JDK 8 and touchscreen
		System.setProperty("glass.accessible.force", "false");
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		model = new SignatureModel();

		//Create JobBuilder && TLValidationJob
		jobBuilder = new JobBuilder();
		tlValidationJob = jobBuilder.job();
		tlValidationJob.offlineRefresh();
		warningLabel.setVisible(false);
		updateLabelText();
		
		// Allows to collapse items
		hPkcsFile.managedProperty().bind(hPkcsFile.visibleProperty());
		hPkcsPassword.managedProperty().bind(hPkcsPassword.visibleProperty());
		labelPkcs11File.managedProperty().bind(labelPkcs11File.visibleProperty());
		labelPkcs12File.managedProperty().bind(labelPkcs12File.visibleProperty());

		fileSelectButton.setOnAction(new EventHandler<>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("File to sign");
				File fileToSign = fileChooser.showOpenDialog(stage);
				model.setFileToSign(fileToSign);
			}
		});
		fileSelectButton.textProperty().bindBidirectional(model.fileToSignProperty(), new FileToStringConverter());

		asicsRadio.setUserData(ASiCContainerType.ASiC_S);
		asiceRadio.setUserData(ASiCContainerType.ASiC_E);
		toggleAsicContainerType.selectedToggleProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if (newValue != null) {
					ASiCContainerType newContainerType = (ASiCContainerType) newValue.getUserData();
					updateSignatureFormForASiC(newContainerType);
				} else {
					updateSignatureFormForASiC(null);
				}
			}
		});

		cadesRadio.setUserData(SignatureForm.CAdES);
		xadesRadio.setUserData(SignatureForm.XAdES);
		padesRadio.setUserData(SignatureForm.PAdES);
		jadesRadio.setUserData(SignatureForm.JAdES);
		toogleSigFormat.selectedToggleProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if (newValue != null) {
					SignatureForm newSigForm = (SignatureForm) newValue.getUserData();
					updateSignatureForm(newSigForm);
				} else {
					updateSignatureForm(null);
				}
			}
		});
		
		envelopedRadio.setUserData(SignaturePackaging.ENVELOPED);
		envelopingRadio.setUserData(SignaturePackaging.ENVELOPING);
		detachedRadio.setUserData(SignaturePackaging.DETACHED);
		internallyDetachedRadio.setUserData(SignaturePackaging.INTERNALLY_DETACHED);
		toggleSigPackaging.selectedToggleProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if (newValue != null) {
					SignaturePackaging newPackaging = (SignaturePackaging) newValue.getUserData();
					model.setSignaturePackaging(newPackaging);
				} else {
					model.setSignaturePackaging(null);
				}
			}
		});
		
		for (DigestAlgorithm digestAlgo : supportedDigestAlgorithms) {
			RadioButton rb = new RadioButton(digestAlgo.getName());
			rb.setUserData(digestAlgo);
			rb.setToggleGroup(toggleDigestAlgo);
			hBoxDigestAlgos.getChildren().add(rb);
		}
		
		toggleDigestAlgo.selectedToggleProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if (newValue != null) {
					DigestAlgorithm digestAlgorithm = (DigestAlgorithm) newValue.getUserData();
					model.setDigestAlgorithm(digestAlgorithm);
				} else {
					model.setDigestAlgorithm(null);
				}
			}
		});

		comboLevel.valueProperty().bindBidirectional(model.signatureLevelProperty());

		pkcs11Radio.setUserData(SignatureTokenType.PKCS11);
		pkcs12Radio.setUserData(SignatureTokenType.PKCS12);
		mscapiRadio.setUserData(SignatureTokenType.MSCAPI);
		toggleSigToken.selectedToggleProperty().addListener(new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if (newValue != null) {
					SignatureTokenType tokenType = (SignatureTokenType) newValue.getUserData();
					model.setTokenType(tokenType);
				}
				model.setPkcsFile(null);
				model.setPassword(null);

				updateSigTokenType(newValue);
			}
		});
		
		pkcsFileButton.setOnAction(new EventHandler<>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				if (SignatureTokenType.PKCS11.equals(model.getTokenType())) {
					fileChooser.setTitle("Library");
					fileChooser.getExtensionFilters().add(
							new FileChooser.ExtensionFilter("PKCS11 library (*.dll)", "*.dll"));
				} else if (SignatureTokenType.PKCS12.equals(model.getTokenType())) {
					fileChooser.setTitle("Keystore");
					fileChooser.getExtensionFilters().add(
							new FileChooser.ExtensionFilter("PKCS12 keystore (*.p12, *.pfx)", "*.p12", "*.pfx"));
				}

				FileChooser.ExtensionFilter allFilesExtensionFilter = new FileChooser.ExtensionFilter("All files", "*");
				fileChooser.getExtensionFilters().add(allFilesExtensionFilter);

				File pkcsFile = fileChooser.showOpenDialog(stage);
				model.setPkcsFile(pkcsFile);
			}
		});
		pkcsFileButton.textProperty().bindBidirectional(model.pkcsFileProperty(), new FileToStringConverter());

		pkcsPassword.textProperty().bindBidirectional(model.passwordProperty());

		BooleanBinding isPkcs11Or12 = model.tokenTypeProperty().isEqualTo(SignatureTokenType.PKCS11)
				.or(model.tokenTypeProperty().isEqualTo(SignatureTokenType.PKCS12));

		hPkcsFile.visibleProperty().bind(isPkcs11Or12);
		hPkcsPassword.visibleProperty().bind(isPkcs11Or12);

		labelPkcs11File.visibleProperty().bind(model.tokenTypeProperty().isEqualTo(SignatureTokenType.PKCS11));
		labelPkcs12File.visibleProperty().bind(model.tokenTypeProperty().isEqualTo(SignatureTokenType.PKCS12));

		BooleanBinding isMandatoryFieldsEmpty = model.fileToSignProperty().isNull()
				.or(model.signatureFormProperty().isNull()).or(model.digestAlgorithmProperty().isNull())
				.or(model.tokenTypeProperty().isNull());

		BooleanBinding isASiCorPackagingPresent = model.asicContainerTypeProperty().isNull()
				.and(model.signaturePackagingProperty().isNull());

		BooleanBinding isEmptyFileOrPassword = model.pkcsFileProperty().isNull().or(model.passwordProperty().isEmpty());

		BooleanBinding isPKCSIncomplete = model.tokenTypeProperty().isEqualTo(SignatureTokenType.PKCS11)
				.or(model.tokenTypeProperty().isEqualTo(SignatureTokenType.PKCS12)).and(isEmptyFileOrPassword);

		final BooleanBinding disableSignButton = isMandatoryFieldsEmpty.or(isASiCorPackagingPresent)
				.or(isPKCSIncomplete);

		signButton.disableProperty().bind(disableSignButton);

		signButton.setOnAction(new EventHandler<>() {
			@Override
			public void handle(ActionEvent event) {
				progressSign.setDisable(false);

				final Service<DSSDocument> service = new Service<>() {
					@Override
					protected Task<DSSDocument> createTask() {
						return new SigningTask(model, jobBuilder.getCertificateSources());
					}
				};
				service.setOnSucceeded(new EventHandler<>() {
					@Override
					public void handle(WorkerStateEvent event) {
						save(service.getValue());
						signButton.disableProperty().bind(disableSignButton);
						model.setPassword(null);
					}
				});
				service.setOnFailed(new EventHandler<>() {
					@Override
					public void handle(WorkerStateEvent event) {
						String errorMessage = "Oops an error occurred : " + service.getMessage();
						LOG.error(errorMessage, service.getException());
						Alert alert = new Alert(AlertType.ERROR, errorMessage, ButtonType.CLOSE);
						alert.showAndWait();
						signButton.disableProperty().bind(disableSignButton);
						model.setPassword(null);
					}
				});

				progressSign.progressProperty().bind(service.progressProperty());
				signButton.disableProperty().bind(service.runningProperty());
				service.start();
			}
		});

		
		refreshLOTL.setOnAction(new EventHandler<>() {
			@Override
			public void handle(ActionEvent event) {
				final RefreshLOTLTask task = new RefreshLOTLTask(tlValidationJob);
				task.setOnRunning(new EventHandler<>() {
					@Override
					public void handle(WorkerStateEvent event) {
						warningLabel.setVisible(false);
						addLoader();
					}
				});

				task.setOnSucceeded(new EventHandler<>() {
					@Override
					public void handle(WorkerStateEvent event) {
						removeLoader();
						updateLabelText();
					}
				});

				task.setOnFailed(new EventHandler<>() {
					@Override
					public void handle(WorkerStateEvent event) {
						removeLoader();
						warningLabel.setVisible(true);
					}
				});

				//start Task
				Thread readValThread = new Thread(task);
				readValThread.setDaemon(true);
				readValThread.start();
			}
		});
	}
	
	private void removeLoader() {
		refreshBox.getChildren().remove(progressRefreshLOTL);
	}
	
	private void addLoader() {
		removeLoader();
		progressRefreshLOTL = new ProgressIndicator();
    	refreshBox.getChildren().add(progressRefreshLOTL);
	}
	
	private void updateLabelText() {
		nbCertificates.setText(nbCertificatesTest + jobBuilder.getCertificateSources().getNumberOfCertificates());
	}

	protected void updateSignatureFormForASiC(ASiCContainerType newValue) {
		model.setAsicContainerType(newValue);

		reinitSignatureFormats();
		reinitSignaturePackagings();

		if (newValue != null) { // ASiC
			cadesRadio.setDisable(false);
			xadesRadio.setDisable(false);
			hSignaturePackaging.setVisible(false);
		} else {
			cadesRadio.setDisable(false);
			padesRadio.setDisable(false);
			xadesRadio.setDisable(false);
			jadesRadio.setDisable(false);
			hSignaturePackaging.setVisible(true);
		}

	}

	protected void updateSignatureForm(SignatureForm signatureForm) {
		model.setSignatureForm(signatureForm);

		reinitSignaturePackagings();
		
		sigFormSupportedDigestAlgorithms = supportedDigestAlgorithms;

		comboLevel.setDisable(false);
		comboLevel.getItems().removeAll(comboLevel.getItems());

		if (signatureForm != null) {
			switch (signatureForm) {
			case CAdES:
				envelopingRadio.setDisable(false);
				detachedRadio.setDisable(false);

				comboLevel.getItems().addAll(SignatureLevel.CAdES_BASELINE_B, SignatureLevel.CAdES_BASELINE_T,
						SignatureLevel.CAdES_BASELINE_LT, SignatureLevel.CAdES_BASELINE_LTA);
				comboLevel.setValue(SignatureLevel.CAdES_BASELINE_B);
				break;
			case PAdES:
				envelopedRadio.setDisable(false);

				envelopedRadio.setSelected(true);

				comboLevel.getItems().addAll(SignatureLevel.PAdES_BASELINE_B, SignatureLevel.PAdES_BASELINE_T,
						SignatureLevel.PAdES_BASELINE_LT, SignatureLevel.PAdES_BASELINE_LTA);
				comboLevel.setValue(SignatureLevel.PAdES_BASELINE_B);
				break;
			case XAdES:
				envelopingRadio.setDisable(false);
				envelopedRadio.setDisable(false);
				detachedRadio.setDisable(false);
				internallyDetachedRadio.setDisable(false);
				
				sigFormSupportedDigestAlgorithms = Arrays.asList(DigestAlgorithm.SHA1, DigestAlgorithm.SHA224, DigestAlgorithm.SHA256, 
						DigestAlgorithm.SHA384, DigestAlgorithm.SHA512);

				comboLevel.getItems().addAll(SignatureLevel.XAdES_BASELINE_B, SignatureLevel.XAdES_BASELINE_T,
						SignatureLevel.XAdES_BASELINE_LT, SignatureLevel.XAdES_BASELINE_LTA);
				comboLevel.setValue(SignatureLevel.XAdES_BASELINE_B);
				break;
			case JAdES:
				envelopingRadio.setDisable(false);
				detachedRadio.setDisable(false);
				
				sigFormSupportedDigestAlgorithms =  Arrays.asList(DigestAlgorithm.SHA256, DigestAlgorithm.SHA384, DigestAlgorithm.SHA512);

				comboLevel.getItems().addAll(SignatureLevel.JAdES_BASELINE_B, SignatureLevel.JAdES_BASELINE_T,
						SignatureLevel.JAdES_BASELINE_LT, SignatureLevel.JAdES_BASELINE_LTA);
				comboLevel.setValue(SignatureLevel.JAdES_BASELINE_B);
			default:
				break;
			}
		}
		
		reinitDigestAlgos();
	}
	
	private void updateSigTokenType(Toggle newValue) {
		SignatureTokenType tokenType = (SignatureTokenType) newValue.getUserData();
		
		sigTokenTypeSupportedDigestAlgorithms = new ArrayList<>(supportedDigestAlgorithms);
		
		switch (tokenType) {
			case MSCAPI:
				// SHA224 not supported
				sigTokenTypeSupportedDigestAlgorithms.remove(DigestAlgorithm.SHA224);
			default:
				break;
		}
		
		reinitDigestAlgos();
	}

	private void reinitSignatureFormats() {
		cadesRadio.setDisable(true);
		padesRadio.setDisable(true);
		xadesRadio.setDisable(true);
		jadesRadio.setDisable(true);

		cadesRadio.setSelected(false);
		padesRadio.setSelected(false);
		xadesRadio.setSelected(false);
		jadesRadio.setSelected(false);
	}

	private void reinitSignaturePackagings() {
		envelopingRadio.setDisable(true);
		envelopedRadio.setDisable(true);
		detachedRadio.setDisable(true);
		internallyDetachedRadio.setDisable(true);

		envelopingRadio.setSelected(false);
		envelopedRadio.setSelected(false);
		detachedRadio.setSelected(false);
		internallyDetachedRadio.setSelected(false);
	}
	
	private void reinitDigestAlgos() {
		ArrayList<DigestAlgorithm> digestAlgos = new ArrayList<>(supportedDigestAlgorithms);
		if (sigFormSupportedDigestAlgorithms != null) {
			digestAlgos.retainAll(sigFormSupportedDigestAlgorithms);
		}
		if (sigTokenTypeSupportedDigestAlgorithms != null) {
			digestAlgos.retainAll(sigTokenTypeSupportedDigestAlgorithms);
		}
		
		for (Node daButton : hBoxDigestAlgos.getChildren()) {
			DigestAlgorithm digestAlgorithm = (DigestAlgorithm) daButton.getUserData();
			if (digestAlgorithm == null) {
				// nothing chosen case
			} else if (digestAlgos.contains(digestAlgorithm)) {
				daButton.setDisable(false);
			} else {
				daButton.setDisable(true);
				Toggle selectedToggle = toggleDigestAlgo.getSelectedToggle();
				if (selectedToggle != null && digestAlgorithm.equals(selectedToggle.getUserData())) {
					selectedToggle.setSelected(false);
				}
			}
		}
	}

	private void save(DSSDocument signedDocument) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialFileName(signedDocument.getName());
		MimeType mimeType = signedDocument.getMimeType();
		
		String extension = MimeType.getExtension(mimeType);
		String filterPattern = extension != null ? "*." + extension : "*";
		ExtensionFilter extFilter = new ExtensionFilter(mimeType.getMimeTypeString(), filterPattern);
		fileChooser.getExtensionFilters().add(extFilter);
		File fileToSave = fileChooser.showSaveDialog(stage);

		if (fileToSave != null) {
			try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
				Utils.copy(signedDocument.openStream(), fos);
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR, "Unable to save file : " + e.getMessage(), ButtonType.CLOSE);
				alert.showAndWait();
			}
		}
	}

}
