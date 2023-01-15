package software.isratech.easy_file_transferer.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Contains data given to the receive file controller. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileDownloadUtils {

    /** Singleton instance. */
    private static FileDownloadUtils singletonInstance = null;

    /** Get the singleton instance.
     * @return this. */
    public static FileDownloadUtils getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new FileDownloadUtils();
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

    /** File download path. */
    @Setter
    @Getter
    private String selectedPath;
}
