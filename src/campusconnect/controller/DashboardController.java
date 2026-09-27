package campusconnect.controller;

import campusconnect.model.Activity;
import campusconnect.store.DataStore;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private ListView<Activity> activityListView;
    @FXML private TextField searchField;
    @FXML private Label statusBarLabel;
    @FXML private BorderPane mainShell;

    private DataStore store = DataStore.getInstance();
    private FilteredList<Activity> filteredActivities;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Wrap the observable list in a FilteredList so search works live
        filteredActivities = new FilteredList<>(store.getActivities(), a -> true);
        activityListView.setItems(filteredActivities);

        // Live search — fires on every key typed in the search field (KeyEvent)
        searchField.setOnKeyReleased((KeyEvent e) -> {
            String query = searchField.getText().toLowerCase().trim();
            filteredActivities.setPredicate(activity -> {
                if (query.isEmpty()) return true;
                return activity.getTitle().toLowerCase().contains(query)
                        || activity.getCategory().toLowerCase().contains(query)
                        || activity.getStatus().toLowerCase().contains(query);
            });
            updateStatusBar();
        });

        // Double-click on an activity opens the Conduct/Detail screen (MouseEvent)
        activityListView.setOnMouseClicked((MouseEvent e) -> {
            if (e.getClickCount() == 2) {
                Activity selected = activityListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openConductScreen(selected);
                }
            }
        });

        // Right-click context menu on list items
        activityListView.setContextMenu(buildContextMenu());

        updateStatusBar();
    }

    // ── Menu / sidebar action handlers ────────────────────────────────────────

    @FXML
    private void handleNewActivity() {
        loadCenterScreen("/campusconnect/view/activity_form.fxml");
    }

    @FXML
    private void handleNavDashboard() {
        loadCenterScreen("/campusconnect/view/dashboard.fxml");
    }

    @FXML
    private void handleOpenResources() {
        loadCenterScreen("/campusconnect/view/resource_gallery.fxml");
    }

    @FXML
    private void handleOpenScheduler() {
        loadCenterScreen("/campusconnect/view/scheduler.fxml");
    }

    @FXML
    private void handleOpenReports() {
        loadCenterScreen("/campusconnect/view/reports.fxml");
    }

    @FXML
    private void handleApproveActivity() {
        Activity selected = activityListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusBarLabel.setText("Select an activity first.");
            return;
        }
        if (selected.getStatus().equals("Proposed")) {
            selected.setStatus("Approved");
            activityListView.refresh();
            statusBarLabel.setText("Approved: " + selected.getTitle());
        } else {
            statusBarLabel.setText("Only Proposed activities can be approved.");
        }
    }

    @FXML
    private void handleDeleteActivity() {
        Activity selected = activityListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusBarLabel.setText("Select an activity to cancel.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Activity");
        confirm.setHeaderText("Cancel \"" + selected.getTitle() + "\"?");
        confirm.setContentText("This will mark the activity as Cancelled.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selected.setStatus("Cancelled");
            activityListView.refresh();
            statusBarLabel.setText("Cancelled: " + selected.getTitle());
        }
    }

    @FXML
    private void handleAddResource() {
        loadCenterScreen("/campusconnect/view/resource_gallery.fxml");
    }

    @FXML
    private void handleAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About CampusConnect");
        about.setHeaderText("CampusConnect v1.0");
        about.setContentText("An Integrated Activity Planning, Scheduling and Execution System.\n\nUnit V JavaFX Mini Project\nB.E./B.Tech. Computer Science and Engineering");
        about.showAndWait();
    }

    @FXML
    private void handleExit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Exit");
        confirm.setContentText("Are you sure you want to exit CampusConnect?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void openConductScreen(Activity activity) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/campusconnect/view/conduct.fxml"));
            Parent view = loader.load();

            ConductController cc = loader.getController();
            cc.setActivity(activity);
            cc.setShell(mainShell);

            mainShell.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
            statusBarLabel.setText("Could not open activity detail.");
        }
    }

    // Swap the BorderPane center with a new screen
    private void loadCenterScreen(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Pass the shell reference to sub-controllers that need to navigate back
            Object ctrl = loader.getController();
            if (ctrl instanceof ConductController) {
                ((ConductController) ctrl).setShell(mainShell);
            }

            mainShell.setCenter(view);
            updateStatusBar();
        } catch (Exception e) {
            e.printStackTrace();
            statusBarLabel.setText("Error loading screen: " + fxmlPath);
        }
    }

    private ContextMenu buildContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem openItem = new MenuItem("Open / Edit");
        openItem.setOnAction(e -> {
            Activity selected = activityListView.getSelectionModel().getSelectedItem();
            if (selected != null) openConductScreen(selected);
        });

        MenuItem approveItem = new MenuItem("Approve");
        approveItem.setOnAction(e -> handleApproveActivity());

        MenuItem cancelItem = new MenuItem("Cancel Activity");
        cancelItem.setOnAction(e -> handleDeleteActivity());

        menu.getItems().addAll(openItem, approveItem, new SeparatorMenuItem(), cancelItem);
        return menu;
    }

    private void updateStatusBar() {
        int total    = store.getActivities().size();
        int showing  = filteredActivities.size();
        long approved = store.getActivities().stream()
                .filter(a -> a.getStatus().equals("Approved")).count();
        statusBarLabel.setText(
            "Showing " + showing + " of " + total + " activities  |  Approved: " + approved
            + "  |  Logged in as: " + (store.getCurrentUser() != null ? store.getCurrentUser().getUsername() : "—")
        );
    }
}
