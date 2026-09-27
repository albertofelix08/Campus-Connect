package campusconnect.controller;

import campusconnect.model.User;
import campusconnect.store.DataStore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private DataStore store = DataStore.getInstance();

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        User user = store.login(username, password);

        if (user == null) {
            errorLabel.setText("Invalid username or password. Try again.");
            passwordField.clear();
            return;
        }

        // Login successful — load the main shell
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/campusconnect/view/main_shell.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 680));
            stage.setTitle("CampusConnect — " + user.getRole() + ": " + user.getUsername());
            stage.show();

        } catch (Exception e) {
            errorLabel.setText("Error loading application. Please restart.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setText("");
        usernameField.requestFocus();
    }
}
