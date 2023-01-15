package software.isratech.easy_file_transferer.net;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.*;
import java.net.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static software.isratech.easy_file_transferer.Constants.*;

/**
 * Handles basic communication between two tcp clients.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Communication {

    /**
     * Write some text to a printWriter.
     *
     * @param writer  - the writer to write the text to.
     * @param message - the message to write.
     */
    public static void sendMessage(@NonNull final PrintWriter writer, @NonNull final String message) {
        writer.println(message);
    }

    /**
     * Receive a message from an input stream.
     *
     * @param bufferedReader - the buffered reader to read the message from.
     * @return the read message.
     */
    @NonNull
    public static String receiveMessage(@NonNull final BufferedReader bufferedReader) throws IOException {
        return bufferedReader.readLine();
    }

    /**
     * Receive a single long value from an input stream.
     *
     * @param bufferedReader - the buffered reader to read the long from.
     * @return a single long.
     */
    @NonNull
    public static Long receiveLong(@NonNull final BufferedReader bufferedReader) throws IOException {
        return Long.parseLong(receiveMessage(bufferedReader));
    }

    /**
     * Receive a file from the remote
     *
     * @param is                     - the socket's input stream to read data from
     * @param fileInfoQuadruple      - a quadruple containing file name, size, file exists and existing file size
     * @return the file after it was received
     */
    @NonNull
    public static File receiveFile(
            @NonNull final InputStream is,
            @NonNull final Client.Quadruple<String, Long, Boolean, Long> fileInfoQuadruple
    ) throws IOException {
        try (
                final BufferedOutputStream bufferedWriter = new BufferedOutputStream(
                        new FileOutputStream(fileInfoQuadruple.getFirst(), fileInfoQuadruple.getThird()))
        ) {
            long receivedLength = fileInfoQuadruple.getFourth();
            while (receivedLength < fileInfoQuadruple.getSecond()) {
                int code;
                byte[] buffer = new byte[Math.toIntExact(getBufferSize(fileInfoQuadruple.getSecond(), receivedLength))];
                code = is.read(buffer);
                bufferedWriter.write(buffer, 0, code);
                receivedLength += code;
            }
            bufferedWriter.flush();
            return new File(fileInfoQuadruple.getFirst());
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Determines how many bytes of data should be read from the socket.
     * The standard transfer speed shall be 2 MB/cycle.
     *
     * @param fileSize - the size of the file to receive
     * @param readSize - the amount of bytes already read
     * @return how many bytes should be read in the next cycle
     */
    private static long getBufferSize(final long fileSize, final long readSize) {
        if (fileSize < DEFAULT_BYTES) {
            if (readSize > 0L) {
                return Math.max(fileSize - readSize, 0L);
            }
            return fileSize;
        }
        return Math.min(fileSize - readSize, DEFAULT_BYTES);
    }

    /**
     * Get the IPv4 Address of the client by pinging google dns using the currently used network interface.
     *
     * @return the client's IPv4 address.
     */
    public static String getIpAddress() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName(GOOGLE_DNS), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            return DEFAULT_LOOPBACK_ADDRESS;
        }
    }

    /**
     * Ping all addresses in subnet to find a server listening for connections
     *
     * @param port          - the port to ping on
     * @param stopScanning  - used to stop scanning for hosts
     * @return a list of all available servers and their mac addresses
     */
    public static void getAvailableServers(
            final int port,
            final AtomicBoolean stopScanning,
            final ListView<String> listView,
            final Label infoTextField
            ) {
        final List<String> result = new ArrayList<>();
        final String hostAddress = getIpAddress();
        if (DEFAULT_LOOPBACK_ADDRESS.equalsIgnoreCase(hostAddress)) {
            Platform.runLater(() -> {
                infoTextField.setText("Cant ping yourself!");
                infoTextField.setStyle("-fx-text-fill: red !important;");
                infoTextField.setVisible(true);
            });
            return;
        }
        final String[] hostAddressSplit = Objects.requireNonNull(hostAddress).split("\\.");
        if (hostAddressSplit.length < 3) throw new IllegalArgumentException("Cannot find subnet!");
        final String subnet = String.format(
                "%s.%s.%s.",
                hostAddressSplit[0],
                hostAddressSplit[1],
                hostAddressSplit[2]
        );
        for (int i = 100; i < 256; i++) {
            if (stopScanning.get()) break;
            final String hostName = subnet + i;
            try (final Socket socket = new Socket()) {
                Platform.runLater(() -> infoTextField.setText("Pinging " + hostName + "..."));
                final SocketAddress socketAddress = new InetSocketAddress(hostName, port);
                socket.connect(socketAddress, 250);
                if (pingServer(socket)) {
                    result.add(hostName);
                    Platform.runLater(() -> listView.getItems().add(hostName));
                }
            } catch (Exception e) {
                // ignored
            }
        }
        if (result.isEmpty()) {
            Platform.runLater(() -> {
                infoTextField.setText("No servers found!");
                infoTextField.setStyle("-fx-text-fill: red !important;");
                infoTextField.setVisible(true);
            });
            return;
        }
        Platform.runLater(() -> infoTextField.setVisible(false));
    }

    /**
     * Sends a ping to a socket
     *
     * @param socket - the connected socket
     * @return true if the server responds with REPLY, else false
     */
    private static boolean pingServer(final Socket socket) throws IOException {
        final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        sendMessage(printWriter, "PING");
        return "REPLY".equalsIgnoreCase(receiveMessage(bufferedReader));
    }

    /**
     * Get a file's human readable size
     *
     * @param fileSize - the file's size in bytes
     * @return a human readable file size
     */
    public static String getHumanReadableFileSize(long fileSize) {
        if (-1000 < fileSize && fileSize < 1000) {
            return fileSize + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (fileSize <= -999_950 || fileSize >= 999_950) {
            fileSize /= 1000;
            ci.next();
        }
        return String.format(Locale.ENGLISH, "%.1f %cB", fileSize / 1000.0, ci.current());
    }
}
