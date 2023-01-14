package software.isratech.easy_file_transferer.view.send;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import software.isratech.easy_file_transferer.net.Server;
import software.isratech.easy_file_transferer.utils.FileUploadUtils;
import software.isratech.easy_file_transferer.view.NavigationController;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

import static software.isratech.easy_file_transferer.net.Communication.getHumanReadableFileSize;
import static software.isratech.easy_file_transferer.net.Communication.getIpAddress;

/**
 * Handles sending files.
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SendingFileController extends NavigationController implements Initializable {

    /**
     * File info label.
     */
    @FXML
    protected Label fileInfoLabel;

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
     * Starts the upload server.
     *
     * @param ipAddress    - the ip address to listen on.
     * @param port         - the port to listen on.
     * @param selectedFile - the file to upload.
     */
    private void executeUploadServer(@NonNull final String ipAddress, final int port, @NonNull final File selectedFile) {
        final Server server = new Server();
        new Thread(() -> {
            try (final ServerSocket serverSocket = new ServerSocket()) {
                server.serve(serverSocket, ipAddress, port, selectedFile, statusMessageLabel);
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
        final FileUploadUtils fileUploadUtils = FileUploadUtils.getInstance();
        final String ipAddress = fileUploadUtils.getIpAddress();
        final int port = fileUploadUtils.getPort();
        final File selectedFile = fileUploadUtils.getFile();
        try {
            fileInfoLabel.setText(String.format(
                    "File name: %s%nFile size: %s%nYour IP Address is: %s",
                    selectedFile.getName(),
                    getHumanReadableFileSize(Files.size(selectedFile.toPath())),
                    getIpAddress()
            ));
        }
        catch (IOException e) {
            fileInfoLabel.setText(String.format(
                    "File name: %s%nFile size: Unknown!%nYour IP Address is: %s",
                    selectedFile.getName(),
                    getIpAddress()
            ));
        }
        executeUploadServer(ipAddress, port, selectedFile);
    }
}
