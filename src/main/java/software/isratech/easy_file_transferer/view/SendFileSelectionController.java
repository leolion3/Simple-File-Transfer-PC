package software.isratech.easy_file_transferer.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.NonNull;
import org.controlsfx.control.ToggleSwitch;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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
    protected Label errorMessageText;

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
        final File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            selectedFilePath.setPromptText(selectedFile.getPath());
            this.selectedFile = selectedFile;
            sendFileButton.setDisable(false);
        }
    }

    /**
     * Switch to send file scene and pass the selected file to the new controller.
     */
    @FXML
    protected void sendingFileMenu() throws IOException {
        setActiveMenu(CurrentScene.SENDING_FILE);
    }

    /** Displays an error message.
     * @param errorMessage - the error message to display. */
    private void displayErrorMessage(@NonNull final String errorMessage) {
        errorMessageText.setText(errorMessage);
        errorMessageText.setVisible(true);
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
            errorMessageText.setVisible(t1);
            if (t1) errorMessageText.setText("");
        });
        portTextField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                portTextField.setText(newValue.replaceAll("[^\\d]", ""));
                displayErrorMessage("Port must be a numeric value!");
            } else {
                errorMessageText.setVisible(false);
                disableSendButtonIfNecessary(false);
            }
        });
    }
}
