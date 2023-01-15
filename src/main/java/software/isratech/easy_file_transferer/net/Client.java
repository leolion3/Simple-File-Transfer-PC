package software.isratech.easy_file_transferer.net;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import lombok.*;
import software.isratech.easy_file_transferer.hashing.Hashing;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import static software.isratech.easy_file_transferer.net.Communication.*;

/**
 * Handles receiving files from a remote.
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Client {

    /**
     * Connect to remote and receive a file
     *
     * @param remoteHost - ip address of the remote host
     * @param remotePort - the port of the remote host
     */
    public void connect(
            @NonNull final String remoteHost,
            final int remotePort,
            @NonNull final String exportFilePath,
            @NonNull final Label connectionInfoLabel,
            @NonNull final Label statusMessageLabel
    ) throws IOException, NoSuchAlgorithmException {
        final SocketAddress socketAddress = new InetSocketAddress(remoteHost, remotePort);
        try (final Socket socket = new Socket()) {
            String connectionStatusText = String.format("Connecting to %s:%s...", remoteHost, remotePort);
            updateTextLabel(connectionStatusText, connectionInfoLabel);
            socket.connect(socketAddress);
            connectionStatusText += "\nConnection successful!";
            updateTextLabel(connectionStatusText, connectionInfoLabel);
            final InputStream socketInputStream = socket.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socketInputStream));
            final PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            final Quadruple<String, Long, Boolean, Long> fileInfoQuadruple = handleInitialCommunication(reader, writer, exportFilePath, statusMessageLabel);
            final File receivedFile = receiveFile(socketInputStream, fileInfoQuadruple, statusMessageLabel);
            updateTextLabel(String.format("%s%n%s", statusMessageLabel.getText(), "Computing hashes..."), statusMessageLabel);
            compareFileHashes(reader, writer, receivedFile, statusMessageLabel);
            updateTextLabel(String.format("%s%n%s", statusMessageLabel.getText(), "Transfer complete."), statusMessageLabel);
        }
    }

    /**
     * Handles initial client-server communication and returns info about the file that will be received.
     *
     * @param reader - the input reader to read incoming messages from.
     * @param writer - the writer to send messages to the remote with.
     * @return a quadruple containing file name, file size, file exists on own system and the existing file size.
     */
    @NonNull
    private Quadruple<String, Long, Boolean, Long> handleInitialCommunication(
            @NonNull final BufferedReader reader,
            @NonNull final PrintWriter writer,
            @NonNull final String exportFilePath,
            @NonNull final Label statusMessageLabel
    ) throws IOException {
        long existingFileSize = 0L;
        sendMessage(writer, "init");
        String transferStatusText = "Retrieving file info...";
        updateTextLabel(transferStatusText, statusMessageLabel);
        final String fileName = receiveMessage(reader);
        sendMessage(writer, "Received Name");
        final long fileSize = receiveLong(reader);
        transferStatusText += String.format("%nFile name: %s%nFile size: %s", fileName, getHumanReadableFileSize(fileSize));
        updateTextLabel(transferStatusText, statusMessageLabel);
        final AtomicBoolean fileExists = new AtomicBoolean(false);
        final Path absolutePath = Path.of(exportFilePath, fileName);
        final File existingFile = getExistingFileUri(absolutePath.toAbsolutePath().toString());
        if (existingFile != null) {
            existingFileSize = Files.size(existingFile.toPath());
            sendMessage(writer, String.format("SIZE:%s", existingFileSize));
            fileExists.set(true);
        } else {
            sendMessage(writer, "NONEXISTANT");
        }
        receiveMessage(reader);
        sendMessage(writer, "Beginning files transfer...");
        return new Quadruple<>(absolutePath.toAbsolutePath().toString(), fileSize, fileExists.get(), existingFileSize);
    }

    private File getExistingFileUri(
            @NonNull final String filePath
    ) {
        if (Files.exists(Path.of(filePath))) {
            return new File(filePath);
        }
        return null;
    }

    /**
     * Checks if the hash of the received file matches that of the sent file.
     *
     * @param receivedFileHash - hash of the received file.
     * @param fileHash         - the actual file hash.
     */
    private void verifyHashesMatch(
            @NonNull final String receivedFileHash,
            @NonNull final String fileHash,
            @NonNull final File file,
            @NonNull final Label statusMessageLabel
    ) {
        if (!receivedFileHash.equalsIgnoreCase(fileHash)) {
            updateTextLabel(String.format("%s%n%s", statusMessageLabel.getText(), "Hashes mismatch!"), statusMessageLabel);
            statusMessageLabel.setStyle("-fx-text-fill: red !important;");
            Platform.runLater(() -> {
                final Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Hashes mismatch!");
                alert.setContentText("Hashes mismatch! Do you want to delete the file?");
                final ButtonType delete = new ButtonType("Keep", ButtonBar.ButtonData.YES);
                final ButtonType keep = new ButtonType("Delete", ButtonBar.ButtonData.NO);
                alert.getButtonTypes().setAll(delete, keep);
                alert.showAndWait().ifPresent(type -> {
                    if (type == ButtonType.NO) {
                        try {
                            Files.delete(file.toPath());
                        }
                        catch (IOException e) {
                            updateTextLabel(String.format("%s%n%s", statusMessageLabel.getText(), "Failed to delete file!"), statusMessageLabel);
                        }
                    }
                });
            });
        } else {
            updateTextLabel(String.format("%s%n%s", statusMessageLabel.getText(), "File hashes match!"), statusMessageLabel);
            statusMessageLabel.setStyle("-fx-text-fill: green !important;");
        }
    }

    /**
     * Receives the hash of the file that was sent from the remote and compares it to the received
     * file's hash.
     *
     * @param reader - the reader to receive data from.
     * @param writer - the writer to send messages to the remote.
     * @param file   - the received file.
     */
    private void compareFileHashes(
            @NonNull final BufferedReader reader,
            @NonNull final PrintWriter writer,
            @NonNull final File file,
            @NonNull final Label statusMessageLabel
    ) throws IOException, NoSuchAlgorithmException {
        sendMessage(writer, "GIVE_ME_HASH");
        final String receivedFileHash = receiveMessage(reader);
        final String fileHash = Hashing.getSHA256FileHash(file);
        verifyHashesMatch(receivedFileHash, fileHash, file, statusMessageLabel);
    }

    /**
     * Implements a quadruple, which contains 4 different data types.
     */
    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    static class Quadruple<X, Y, Z, W> {
        private X first;
        private Y second;
        private Z third;
        private W fourth;
    }
}
