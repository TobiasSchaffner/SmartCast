package hm.edu.main;

import hm.edu.controller.SmartCastFXMLController;
import hm.edu.model.Connection;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Hammerschall
 */
public class MainApp extends Application {

    @Override
    public final void start(final Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SmartCastFXML.fxml"));
        Parent root = (Parent) loader.load();
        SmartCastFXMLController smartCastFXMLController = (SmartCastFXMLController) loader.getController();
        Connection connection = new Connection();
        smartCastFXMLController.setConnection(connection);

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("SmartCast");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        launch(args);
    }

}
