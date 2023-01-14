package software.isratech.easy_file_transferer.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EasyFileTransferController {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}