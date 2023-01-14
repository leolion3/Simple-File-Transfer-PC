package software.isratech.easy_file_transferer.hashing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implements hashing functionality.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Hashing {

    /**
     * Returns the SHA-256 hash of a given file.
     *
     * @param file - the file, the hash of which shall be computed.
     * @return the file's SHA-256 hash, encoded in hex.
     */
    @NonNull
    public static String getSHA256FileHash(@NonNull final File file) throws IOException, NoSuchAlgorithmException {
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[4096];
        try (final DigestInputStream digestInputStream = new DigestInputStream(new FileInputStream(file), messageDigest)) {
            while (digestInputStream.read(buffer) != -1) ;
            return Hex.encodeHexString(messageDigest.digest());
        }
    }
}
