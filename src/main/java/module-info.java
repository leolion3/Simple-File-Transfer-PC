module software.isratech.easy_file_transferer {
    requires javafx.controls;
    requires javafx.fxml;
            
        requires org.controlsfx.controls;
                    requires org.kordamp.ikonli.javafx;
            requires org.kordamp.bootstrapfx.core;
            
    opens software.isratech.easy_file_transferer to javafx.fxml;
    exports software.isratech.easy_file_transferer;
}