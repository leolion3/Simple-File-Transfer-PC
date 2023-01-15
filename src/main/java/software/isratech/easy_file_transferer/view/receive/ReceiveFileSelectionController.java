package software.isratech.easy_file_transferer.view.receive;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.ToggleSwitch;
import software.isratech.easy_file_transferer.net.Communication;
import software.isratech.easy_file_transferer.utils.FileDownloadUtils;
import software.isratech.easy_file_transferer.view.NavigationController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static software.isratech.easy_file_transferer.Constants.DEFAULT_LOOPBACK_ADDRESS;
import static software.isratech.easy_file_transferer.Constants.DEFAULT_PORT;

/**
 * Handles election of where the received file should be saved.
 */
public class ReceiveFileSelectionController extends NavigationController implements Initializable {

    /**
     * Target location selection button
     */
    @FXML
    protected Button pathSelectionButton;

    /**
     * Text showing the selected path
     */
    @FXML
    protected TextField selectedPathTextField;

    /**
     * Contains all network elements
     */
    @FXML
    protected VBox networkSettingsContainer;

    /**
     * Network settings buttons HBox.
     */
    @FXML
    protected HBox networkSettingsHBox;

    /**
     * Network mode switch
     */
    @FXML
    protected ToggleSwitch networkModeSwitch;

    /**
     * Network mode label
     */
    @FXML
    protected Label networkModeLabel;

    /**
     * Scan/stop scan button
     */
    @FXML
    protected Button scanButton;

    /**
     * List view of IP Addresses
     */
    @FXML
    protected ListView<String> ipAddressList;

    /**
     * Automatic network mode.
     */
    @FXML
    protected VBox autoNetworkModeVBox;

    /**
     * VBox for advanced network settings
     */
    @FXML
    protected VBox advancedNetworkVBox;

    /**
     * IP Address text field
     */
    @FXML
    protected TextField ipAddressTextField;

    /**
     * Port text field
     */
    @FXML
    protected TextField portTextField;

    /**
     * Receive button
     */
    @FXML
    protected Button receiveButton;

    /**
     * Error message text label
     */
    @FXML
    protected Label errorMessageLabel;

    /**
     * Selected file path
     */
    protected String selectedFilePath;

    /**
     * Selected IP Address.
     */
    private String selectedIPAddress;

    /**
     * Selected port.
     */
    private int selectedPort;

    /**
     * Whether a a server scan is currently running.
     */
    private boolean isScanning = false;

    /**
     * Whether to stop scanning.
     */
    final AtomicBoolean stopScanning = new AtomicBoolean();

    /**
     * Change VBox content
     *
     * @param manualMode - whether the network mode is set to manual or auto
     */
    private void changeVBoxContent(boolean manualMode) {
        networkSettingsContainer.getChildren().clear();
        if (manualMode) {
            scanButton.setVisible(false);
            networkSettingsContainer.getChildren().add(advancedNetworkVBox);
            networkModeLabel.setText("Manual");
            return;
        }
        scanButton.setVisible(true);
        networkSettingsContainer.getChildren().add(autoNetworkModeVBox);
        networkModeLabel.setText("Auto");
    }

    /**
     * Scan for available IP Addresses.
     */
    @FXML
    protected void scanForServers() {
        if (isScanning) {
            this.isScanning = false;
            this.stopScanning.set(true);
            networkModeLabel.setText("Auto");
            if (ipAddressList.getItems().isEmpty()) {
                errorMessageLabel.setText("No servers found!");
                errorMessageLabel.setStyle("-fx-text-fill: red !important;");
                errorMessageLabel.setVisible(true);
            }
            scanButton.setStyle("-fx-background-color: orange !important; -fx-text-fill: white !important;");
            scanButton.setText("Scan");
            networkModeSwitch.setDisable(false);
            return;
        }
        errorMessageLabel.setText("");
        errorMessageLabel.setVisible(true);
        errorMessageLabel.setStyle("-fx-text-fill: grey !important;");
        new Thread(() -> Communication.getAvailableServers(DEFAULT_PORT, stopScanning, ipAddressList, errorMessageLabel)).start();
        isScanning = true;
        this.stopScanning.set(false);
        networkModeLabel.setText("Scanning...");
        scanButton.setStyle("-fx-background-color: red !important; -fx-text-fill: white !important;");
        scanButton.setText("Stop");
        networkModeSwitch.setDisable(true);
    }

    /**
     * Select save file location.
     */
    @FXML
    protected void selectSaveFilePath() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        final File selectedPath = directoryChooser.showDialog(stage);
        if (selectedPath != null) {
            this.selectedFilePath = selectedPath.getAbsolutePath();
            networkSettingsHBox.setVisible(true);
            changeVBoxContent(false);
            selectedPathTextField.setText(this.selectedFilePath);
            return;
        }
        networkSettingsHBox.setVisible(false);
    }

    /**
     * Connect to server when an address is clicked.
     */
    @FXML
    protected void handleAddressClicked(final MouseEvent ignored) throws IOException {
        final String selectedItem = ipAddressList.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.isBlank()) {
            this.selectedIPAddress = selectedItem;
            this.selectedPort = DEFAULT_PORT;
            startClient();
        }
    }

    /**
     * Handles clicking the receive button.
     */
    @FXML
    protected void handleReceiveClicked() throws IOException {
        this.selectedIPAddress = ipAddressTextField.getText();
        if (selectedIPAddress == null || selectedIPAddress.isBlank()) {
            selectedIPAddress = DEFAULT_LOOPBACK_ADDRESS;
        }
        final String port = portTextField.getText();
        try {
            if (port.isBlank()) throw new IllegalArgumentException();
            this.selectedPort = Integer.parseInt(port);
        } catch (Exception ignored) {
            this.selectedPort = DEFAULT_PORT;
        }
        startClient();
    }

    /**
     * Starts the client.
     */
    protected void startClient() throws IOException {
        this.stopScanning.set(true);
        this.isScanning = false;
        final FileDownloadUtils fileDownloadUtils = FileDownloadUtils.getInstance();
        fileDownloadUtils.setSelectedPath(this.selectedFilePath);
        fileDownloadUtils.setIpAddress(this.selectedIPAddress);
        fileDownloadUtils.setPort(this.selectedPort);
        setActiveMenu(CurrentScene.RECEIVING_FILE);
    }

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        networkSettingsContainer.getChildren().clear();
        networkSettingsHBox.setVisible(false);
        networkModeSwitch.selectedProperty().addListener((observed, old, manualMode) -> {
            errorMessageLabel.setVisible(false);
            isScanning = false;
            stopScanning.set(true);
            changeVBoxContent(manualMode);
        });
        portTextField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                portTextField.setText(newValue.replaceAll("[^\\d]", ""));
                errorMessageLabel.setText("Port must be a numeric value!");
                errorMessageLabel.setVisible(true);
                return;
            }
            errorMessageLabel.setVisible(false);
        });
    }
}
