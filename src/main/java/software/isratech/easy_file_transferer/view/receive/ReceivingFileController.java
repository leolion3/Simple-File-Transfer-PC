package software.isratech.easy_file_transferer.view.receive;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import software.isratech.easy_file_transferer.net.Client;
import software.isratech.easy_file_transferer.utils.FileDownloadUtils;
import software.isratech.easy_file_transferer.view.NavigationController;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

/**
 * Handles receiving files
 */
public class ReceivingFileController extends NavigationController implements Initializable {

    /**
     * File info label.
     */
    @FXML
    protected Label connectionInfoLabel;

    /**
     * Status message label.
     */
    @FXML
    protected Label statusMessageLabel;

    /**
     * Error message label.
     */
    @FXML
    protected Label errorMessageLabel;

    /**
     * Navigation buttons VBox.
     */
    @FXML
    protected VBox buttonsVBox;

    /**
     * Executes the download operation.
     */
    private void executeDownload() {
        final Client client = new Client();
        final FileDownloadUtils fileDownloadUtils = FileDownloadUtils.getInstance();
        new Thread(() -> {
            try {
                client.connect(
                        fileDownloadUtils.getIpAddress(),
                        fileDownloadUtils.getPort(),
                        fileDownloadUtils.getSelectedPath(),
                        connectionInfoLabel,
                        statusMessageLabel
                );
            } catch (IOException | NoSuchAlgorithmException e) {
                Platform.runLater(() -> {
                    errorMessageLabel.setText("Error occurred!\n" + e.getMessage());
                    errorMessageLabel.setVisible(true);
                });
            }
            Platform.runLater(() -> buttonsVBox.setVisible(true));
        }).start();
    }

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        errorMessageLabel.setVisible(false);
        buttonsVBox.setVisible(false);
        executeDownload();
    }
}
