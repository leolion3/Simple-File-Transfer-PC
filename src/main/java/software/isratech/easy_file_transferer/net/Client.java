package software.isratech.easy_file_transferer.net;

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
            @NonNull final String exportFilePath
    ) throws IOException, NoSuchAlgorithmException {
        final SocketAddress socketAddress = new InetSocketAddress(remoteHost, remotePort);
        try (final Socket socket = new Socket()) {
            socket.connect(socketAddress);
            final InputStream socketInputStream = socket.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socketInputStream));
            final PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            final Quadruple<String, Long, Boolean, Long> fileInfoQuadruple = handleInitialCommunication(reader, writer, exportFilePath);
            final File receivedFile = receiveFile(socketInputStream, fileInfoQuadruple);
            compareFileHashes(reader, writer, receivedFile);
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
            @NonNull final String exportFilePath
    ) throws IOException {
        long existingFileSize = 0L;
        sendMessage(writer, "init");
        final String fileName = receiveMessage(reader);
        sendMessage(writer, "Received Name");
        final long fileSize = receiveLong(reader);
        final AtomicBoolean fileExists = new AtomicBoolean(false);
        final File existingFile = getExistingFileUri(exportFilePath);
        if (existingFile != null) {
            existingFileSize = Files.size(existingFile.toPath());
            sendMessage(writer, String.format("SIZE:%s", existingFileSize));
            fileExists.set(true);
        } else {
            sendMessage(writer, "NONEXISTANT");
        }
        receiveMessage(reader);
        sendMessage(writer, "Beginning files transfer...");
        return new Quadruple<>(fileName, fileSize, fileExists.get(), existingFileSize);
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
            @NonNull final String fileHash
    ) {
        if (!receivedFileHash.equalsIgnoreCase(fileHash)) {
            // todo
        } else {
            // todo
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
            @NonNull final File file
    ) throws IOException, NoSuchAlgorithmException {
        sendMessage(writer, "GIVE_ME_HASH");
        final String receivedFileHash = receiveMessage(reader);
        final String fileHash = Hashing.getSHA256FileHash(file);
        verifyHashesMatch(receivedFileHash, fileHash);
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
