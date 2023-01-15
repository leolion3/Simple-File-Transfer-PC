module software.isratech.easy_file_transferer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.apache.commons.codec;
    requires static lombok;

    opens software.isratech.easy_file_transferer to javafx.fxml;
    exports software.isratech.easy_file_transferer;
    exports software.isratech.easy_file_transferer.view;
    opens software.isratech.easy_file_transferer.view to javafx.fxml;
    exports software.isratech.easy_file_transferer.view.send;
    exports software.isratech.easy_file_transferer.view.receive;
    opens software.isratech.easy_file_transferer.view.send to javafx.fxml;
    opens software.isratech.easy_file_transferer.view.receive to javafx.fxml;
}