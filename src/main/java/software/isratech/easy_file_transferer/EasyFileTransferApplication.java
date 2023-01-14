package software.isratech.easy_file_transferer;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.NonNull;

import java.io.IOException;

import static software.isratech.easy_file_transferer.view.NavigationController.setScene;

public class EasyFileTransferApplication extends Application {

    @Override
    public void start(@NonNull final Stage stage) throws IOException {
        setScene(Constants.APPLICATION_NAME, "main-menu.fxml", stage);
    }

    public static void main(String[] args) {
        launch();
    }
}