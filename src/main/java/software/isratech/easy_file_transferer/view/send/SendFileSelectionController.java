package software.isratech.easy_file_transferer.view.send;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.ToggleSwitch;
import software.isratech.easy_file_transferer.utils.FileUploadUtils;
import software.isratech.easy_file_transferer.view.NavigationController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static software.isratech.easy_file_transferer.Constants.DEFAULT_BIND_ADDRESS;
import static software.isratech.easy_file_transferer.Constants.DEFAULT_PORT;

/**
 * Handles file selection for sending files.
 */
public class SendFileSelectionController extends NavigationController implements Initializable {

    /**
     * Advanced network settings button.
     */
    @FXML
    protected ToggleSwitch sendFileAdvancedSwitch;

    /**
     * Advanced network settings vbox.
     */
    @FXML
    protected VBox advancedNetworkVBox;

    /**
     * Advanced network settings IP Address.
     */
    @FXML
    protected TextField ipAddressTextField;

    /**
     * Advanced network settings port.
     */
    @FXML
    protected TextField portTextField;

    /**
     * Send file button
     */
    @FXML
    protected Button sendFileButton;

    /**
     * Error message label.
     */
    @FXML
    protected Label errorMessageLabel;

    /**
     * Selected file text field.
     */
    @FXML
    protected TextField selectedFilePath;

    /** Selected file. */
    private File selectedFile;

    /**
     * File picker.
     */
    @FXML
    protected void selectFile() {
        final FileChooser fileChooser = new FileChooser();
        final File chosenFile = fileChooser.showOpenDialog(stage);
        if (chosenFile != null) {
            selectedFilePath.setPromptText(chosenFile.getPath());
            this.selectedFile = chosenFile;
            sendFileButton.setDisable(false);
        }
    }

    /**
     * Switch to send file scene and pass the selected file (and network details) to the new controller.
     */
    @FXML
    protected void sendingFileMenu() throws IOException {
        final FileUploadUtils fileUploadUtils = FileUploadUtils.getInstance();
        fileUploadUtils.setFile(selectedFile);
        final String selectedIPAddress = ipAddressTextField.getText();
        fileUploadUtils.setIpAddress(selectedIPAddress == null || selectedIPAddress.isBlank() ? DEFAULT_BIND_ADDRESS : selectedIPAddress);
        final String selectedPort = portTextField.getText();
        int port = DEFAULT_PORT;
        try {
            port = Integer.parseInt(selectedPort);
        } catch (Exception ignored) {
            // ignored
        }
        fileUploadUtils.setPort(port);
        setActiveMenu(CurrentScene.SENDING_FILE);
    }

    /** Disables the send button if any configurations are wrong.
     * @param additionalCondition - additional condition for disabling the button. */
    private void disableSendButtonIfNecessary(boolean additionalCondition) {
        boolean shouldDisable = selectedFile == null || (sendFileAdvancedSwitch.isSelected() && additionalCondition);
        sendFileButton.setDisable(shouldDisable);
    }

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        sendFileAdvancedSwitch.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            advancedNetworkVBox.setVisible(t1);
            errorMessageLabel.setVisible(t1);
            if (t1) errorMessageLabel.setText("");
        });
        portTextField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                portTextField.setText(newValue.replaceAll("[^\\d]", ""));
                errorMessageLabel.setText("Port must be a numeric value!");
                errorMessageLabel.setVisible(true);
            } else {
                errorMessageLabel.setVisible(false);
                disableSendButtonIfNecessary(false);
            }
        });
    }
}
