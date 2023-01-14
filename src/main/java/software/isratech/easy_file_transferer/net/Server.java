package software.isratech.easy_file_transferer.net;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import software.isratech.easy_file_transferer.hashing.Hashing;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import static software.isratech.easy_file_transferer.Constants.DEFAULT_BYTES;
import static software.isratech.easy_file_transferer.net.Communication.*;

/**
 * Implements sending files to a remote client.
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Server {

    /**
     * Set the content of a javaFX label from the server thread.
     *
     * @param message - the message to put in the label.
     * @param label   - the label to update
     */
    private void updateTextLabel(@NonNull final String message, @NonNull final Label label) {
        Platform.runLater(() -> label.setText(message));
    }

    /**
     * Starts a listener which serves files to remote clients
     *
     * @param serverSocket - the server socket, passed so it can be closed externally
     * @param host         - the address to host on
     * @param port         - the port to listen on
     * @param file         - the file
     */
    public void serve(
            @NonNull final ServerSocket serverSocket,
            @NonNull final String host,
            final int port,
            @NonNull final File file,
            @NonNull final Label statusMessageLabel
    ) throws IOException, IllegalArgumentException, NoSuchAlgorithmException {
        String data = "";
        data += "Starting server...";
        updateTextLabel(data, statusMessageLabel);
        final SocketAddress socketAddress = new InetSocketAddress(host, port);
        serverSocket.bind(socketAddress);
        data += String.format("%nServer bound and listening on %s:%s...%nWaiting for client connection...", host, port);
        updateTextLabel(data, statusMessageLabel);
        final Socket clientSocket = getClientSocket(serverSocket);
        data += String.format("%nAccepted connection from %s", clientSocket.getRemoteSocketAddress().toString());
        updateTextLabel(data, statusMessageLabel);
        handleClient(clientSocket, file, statusMessageLabel);
    }

    /**
     * Replies to client pings until a client initiates an actual connection.
     *
     * @param serverSocket - the server socket.
     * @return the client's socket.
     */
    private Socket getClientSocket(@NonNull final ServerSocket serverSocket) throws IOException {
        Socket clientSocket = serverSocket.accept();
        while (!receiveMessage(new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))).equalsIgnoreCase("init")) {
            sendMessage(new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true), "REPLY");
            clientSocket = serverSocket.accept();
        }
        return clientSocket;
    }

    /**
     * Handles a connected client by sending them the selected file.
     *
     * @param clientSocket - the client's socket.
     * @param file         - the file.
     */
    private void handleClient(
            @NonNull final Socket clientSocket,
            @NonNull final File file,
            @NonNull final Label statusMessageLabel
    ) throws IOException, NoSuchAlgorithmException {
        String data = statusMessageLabel.getText();
        final InputStream socketInputStream = clientSocket.getInputStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(socketInputStream));
        final OutputStream socketOutputStream = clientSocket.getOutputStream();
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(socketOutputStream), true);
        data += "\nExchanging file info with client...";
        updateTextLabel(data, statusMessageLabel);
        final long existingFileSize = handleInitialCommunication(reader, writer, file);
        data += "\nSending file to client...";
        updateTextLabel(data, statusMessageLabel);
        handleFileTransfer(socketOutputStream, file, existingFileSize, statusMessageLabel);
        data += "\nSending file hash...";
        updateTextLabel(data, statusMessageLabel);
        checkFileHashes(reader, writer, file);
        data += "\nFile transfer complete.";
        final String endData = data;
        Platform.runLater(() -> {
            statusMessageLabel.setTextFill(Color.GREEN);
            statusMessageLabel.setText(endData);
        });
    }

    /**
     * Handles initial communication with a client for exchanging file info.
     *
     * @param reader - the reader used for receiving messages.
     * @param writer - the writer used for sending messages.
     * @param file   - the file to send
     * @return the size of the file on the client's system.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private long handleInitialCommunication(
            @NonNull final BufferedReader reader,
            @NonNull final PrintWriter writer,
            @NonNull final File file
    ) throws IOException {
        sendMessage(writer, file.getName());
        System.out.println(receiveMessage(reader));
        final long fileSize = Files.size(file.toPath());
        sendMessage(writer, Long.toString(fileSize));
        final String response = receiveMessage(reader);
        long existingSize = 0L;
        System.out.println(response);
        if (!"NONEXISTANT".equalsIgnoreCase(response)) {
            existingSize = Long.parseLong(response.split("SIZE:")[1]);
        }
        sendMessage(writer, "Ready for transfer!");
        System.out.println(receiveMessage(reader));
        return existingSize;
    }

    /**
     * Reads the first n bytes of a file that already exists on a client's system and then sends
     * the remaining bytes to the client.
     *
     * @param socketOutputStream - the client socket's output stream.
     * @param file               - the file.
     * @param currentFileSize    - the size of the file currently on the client's system.
     */
    private void handleFileTransfer(
            @NonNull final OutputStream socketOutputStream,
            @NonNull final File file,
            final long currentFileSize,
            @NonNull final Label statusMessageLabel
    ) throws IOException {
        try (final InputStream fileInputStream = readExistingData(file, currentFileSize)) {
            long readSize = currentFileSize;
            final long fileSize = Files.size(file.toPath());
            final String transferInfoText = statusMessageLabel.getText();
            String currentProgress = String.format("Progress: %s/%s", getHumanReadableFileSize(currentFileSize), getHumanReadableFileSize(fileSize));
            updateTextLabel(String.format("%s%n%s", transferInfoText, currentProgress), statusMessageLabel);
            while (readSize < fileSize) {
                int currentReadSize = getReadThreshold(fileSize - readSize);
                final byte[] buffer = new byte[currentReadSize];
                final int returnCode = fileInputStream.read(buffer);
                if (returnCode == -1) break;
                socketOutputStream.write(buffer);
                readSize += returnCode;
                socketOutputStream.flush();
                currentProgress = String.format("Progress: %s/%s", getHumanReadableFileSize(readSize), getHumanReadableFileSize(fileSize));
                updateTextLabel(String.format("%s%n%s", transferInfoText, currentProgress), statusMessageLabel);
            }
        }
    }

    /**
     * Checks if a file is existing on the client's receiving size.
     * If it exists, attempts to read the first n bytes that the client already has.
     *
     * @param file            - the file.
     * @param currentFileSize - the size of the file on the client's system.
     * @return an inputStream of the file, pre-read to the location of the client's data size.
     */
    @SuppressWarnings("java:S2095") // we do not want to close the stream here. It's closed in handleFileTransfer.
    private InputStream readExistingData(
            @NonNull final File file,
            final long currentFileSize
    ) throws IOException {
        final InputStream fileInputStream = new FileInputStream(file);
        if (currentFileSize == 0L) return fileInputStream;
        long readSize = 0L;
        while (readSize != currentFileSize) {
            int currentReadSize = getReadThreshold(currentFileSize - readSize);
            final byte[] buffer = new byte[currentReadSize];
            final int returnCode = fileInputStream.read(buffer);
            if (returnCode == -1) break;
            readSize += returnCode;
        }
        return fileInputStream;
    }

    /**
     * Computes the sent file's hash and sends it to the client who received the file.
     *
     * @param reader - the reader used for receiving messages from the client.
     * @param writer - the writer for sending messages to the client.
     * @param file   - the file.
     */
    private void checkFileHashes(
            @NonNull final BufferedReader reader,
            @NonNull final PrintWriter writer,
            @NonNull final File file
    ) throws IOException, NoSuchAlgorithmException {
        receiveMessage(reader);
        sendMessage(writer, Hashing.getSHA256FileHash(file));
    }

    /**
     * Computes how many bytes of a file should be read next.
     *
     * @return how many bytes to read next.
     */
    private int getReadThreshold(final long leftFileSize) {
        return Math.toIntExact(Math.min(leftFileSize, DEFAULT_BYTES));
    }
}