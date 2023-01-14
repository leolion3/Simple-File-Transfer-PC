package software.isratech.easy_file_transferer.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import software.isratech.easy_file_transferer.Constants;
import software.isratech.easy_file_transferer.EasyFileTransferApplication;

import java.io.IOException;

/**
 * Handles navigation between scenes.
 */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class NavigationController {

    /**
     * FXML Stage
     */
    protected static Stage stage;

    @FXML
    protected void mainMenu() throws IOException {
        setActiveMenu(CurrentScene.MAIN_MENU);
    }

    @FXML
    protected void sendFileSelectorMenu() throws IOException {
        setActiveMenu(CurrentScene.SEND_FILE_SELECTOR);
    }

    @FXML
    protected void receiveFileSelectorMenu() throws IOException {
        setActiveMenu(CurrentScene.RECEIVE_FILE_SELECTOR);
    }

    @FXML
    protected void receivingFileMenu() throws IOException {
        setActiveMenu(CurrentScene.RECEIVING_FILE);
    }

    /**
     * Sets the active scene using the scene's name.
     *
     * @param scene - the scene's name.
     */
    @FXML
    protected void setActiveMenu(@NonNull final CurrentScene scene) throws IOException {
        switch (scene) {
            case SEND_FILE_SELECTOR:
                setScene("Select file - " + Constants.APPLICATION_NAME, Constants.SEND_SELECTION_MENU_FXML);
                break;
            case SENDING_FILE:
                setScene("Sending file - " + Constants.APPLICATION_NAME, Constants.SENDING_MENU_FXML);
                break;
            case RECEIVE_FILE_SELECTOR:
                setScene("Select file - " + Constants.APPLICATION_NAME, Constants.RECEIVE_SELECTION_MENU_FXML);
                break;
            case RECEIVING_FILE:
                setScene("Receiving file - " + Constants.APPLICATION_NAME, Constants.RECEIVING_MENU_FXML);
                break;
            default:
                setScene(Constants.APPLICATION_NAME, Constants.MAIN_MENU_FXML);
                break;
        }
    }

    /**
     * Sets the active scene.
     *
     * @param title    - the title the window of the application should have in this scene.
     * @param menuName - the name of the scene that should be switched to.
     */
    public static void setScene(@NonNull final String title, @NonNull final String menuName) throws IOException {
        if (stage == null) throw new IOException("Stage is null!");
        setScene(title, menuName, stage);
    }

    /**
     * Sets the active scene.
     *
     * @param title        - the title the window of the application should have in this scene.
     * @param menuName     - the name of the scene that should be switched to.
     * @param currentStage - fxml stage.
     */
    public static void setScene(@NonNull final String title, @NonNull final String menuName, @NonNull final Stage currentStage)
            throws IOException {
        if (stage == null) {
            stage = currentStage;
        }
        final FXMLLoader fxmlLoader = new FXMLLoader(EasyFileTransferApplication.class.getResource(menuName));
        final Scene scene = new Scene(fxmlLoader.load(), 520, 440);
        currentStage.setTitle(title);
        currentStage.setScene(scene);
        currentStage.show();
    }

    /**
     * Available scenes.
     */
    protected enum CurrentScene {
        MAIN_MENU, SEND_FILE_SELECTOR, RECEIVE_FILE_SELECTOR, SENDING_FILE, RECEIVING_FILE
    }
}
