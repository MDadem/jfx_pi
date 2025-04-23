package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private BorderPane dashboardPane; // Main container
    @FXML private VBox sidebar;
    @FXML private Button usersButton;
    @FXML private Button profileButton; // Assuming you have a profile button
    @FXML private Button logoutButton;
    @FXML private Label adminLabel; // In the header
    @FXML
    private VBox center;
    // No user service, user list, or table elements needed here anymore
    public void setCenterContent(javafx.scene.Node node) {
        center.getChildren().setAll(node);
    }
    @FXML
    public void initialize() {
        // Set the admin role label (or get from logged-in user data)
        adminLabel.setText("Admin");
        // Optionally load a default view like the user management view on startup
        loadView("/UserManagementView.fxml");
    }

    @FXML
    public void showUsers(ActionEvent actionEvent) {
        loadView("/UserManagementView.fxml");
    }

    @FXML

    public void showProfile(ActionEvent actionEvent) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminProfile.fxml"));

            Parent root = loader.load(); // can be BorderPane, VBox, etc.

            AdminProfileController profileController = loader.getController();

            profileController.setAdminDashboardController(this);

            profileController.loadAdminDetails();

            dashboardPane.setCenter(root); // this is enough

        } catch (IOException e) {

            showError("Failed to load admin profile: " + e.getMessage());

        }
    }
    // Helper method to load different views into the center pane
    private void loadView(String fxmlPath) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent viewRoot = loader.load();
            dashboardPane.setCenter(viewRoot);

        } catch (IOException e) {
            e.printStackTrace(); // Log the error
            showError("Failed to load view: " + fxmlPath + "\n" + e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace(); // Log the error for null resources
            showError("Failed to find FXML file: " + fxmlPath + "\n Check the path.\n" + e.getMessage());
        }
    }


    @FXML
    public void logout(ActionEvent actionEvent) {
        try {
            // Ensure Login.fxml is in the correct resource path
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            // Get the current stage and set the new scene
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Login"); // Update title if needed
            stage.show();
        } catch (IOException e) {
            showError("Failed to logout: " + e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            showError("Failed to find FXML file: /Login.fxml \n" + e.getMessage());
        }
    }

    // --- Utility Methods ---
    // Keep these or move to a shared utility class
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
}