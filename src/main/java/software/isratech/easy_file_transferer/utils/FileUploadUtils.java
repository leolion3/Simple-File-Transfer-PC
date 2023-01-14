package software.isratech.easy_file_transferer.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

/** Stores data between javaFX scenes */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUploadUtils {

    /** Singleton instance. */
    private static FileUploadUtils singletonInstance = null;

    /** Get the singleton instance.
     * @return this. */
    public static FileUploadUtils getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new FileUploadUtils();
        }
        return singletonInstance;
    }

    /** IP Address */
    @Setter
    @Getter
    private String ipAddress;

    /** Port. */
    @Setter
    @Getter
    private int port;

    /** File to upload. */
    @Setter
    @Getter
    private File file;
}
