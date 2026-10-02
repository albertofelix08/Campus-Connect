package campusconnect.controller;

import campusconnect.Navigator;
import campusconnect.model.Activity;
import campusconnect.store.DataStore;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/** Controller for dashboard.fxml: searchable activity list. Navigation goes through {@link Navigator}. */
public class DashboardController implements Initializable {

    @FXML private ListView<Activity> activityListView;
    @FXML private TextField searchField;

    private final DataStore store = DataStore.getInstance();
    private FilteredList<Activity> filteredActivities;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Wrap the observable list in a FilteredList so search works live
        filteredActivities = new FilteredList<>(store.getActivities(), a -> true);
        activityListView.setItems(filteredActivities);

        // Live search on the text property — also catches paste / cut, not just key releases
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            String query = newText == null ? "" : newText.toLowerCase().trim();
            filteredActivities.setPredicate(a -> query.isEmpty()
                    || contains(a.getTitle(), query)
                    || contains(a.getCategory(), query)
                    || contains(a.getStatus(), query)
                    || contains(a.getVenue(), query));
            updateStatusBar();
        });

        // Double-click on an activity opens the Conduct/Detail screen
        activityListView.setOnMouseClicked((MouseEvent e) -> {
            if (e.getClickCount() == 2) {
                Activity selected = activityListView.getSelectionModel().getSelectedItem();
                if (selected != null) Navigator.showConduct(selected);
            }
        });

        activityListView.setContextMenu(buildContextMenu());

        Navigator.registerDashboard(this);
        updateStatusBar();
    }

    // ── Toolbar handlers ─────────────────────────────────────────────────────

    @FXML
    private void handleNewActivity() {
        Navigator.showActivityForm(null);
    }

    @FXML
    private void handleCancelActivity() {
        cancelSelected();
    }

    // ── Public actions (also used by the shell's Activity menu) ────────────────

    public void approveSelected() {
        Activity selected = activityListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Navigator.setStatus("Select an activity first.");
            return;
        }
        if ("Proposed".equals(selected.getStatus())) {
            selected.setStatus("Approved");
            activityListView.refresh();
            updateStatusBar();
            Navigator.setStatus("Approved: " + selected.getTitle());
        } else {
            Navigator.setStatus("Only Proposed activities can be approved.");
        }
    }

    public void cancelSelected() {
        Activity selected = activityListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Navigator.setStatus("Select an activity to cancel.");
            return;
        }
        if ("Cancelled".equals(selected.getStatus()) || "Completed".equals(selected.getStatus())) {
            Navigator.setStatus("A " + selected.getStatus() + " activity can't be cancelled.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Activity");
        confirm.setHeaderText("Cancel \"" + selected.getTitle() + "\"?");
        confirm.setContentText("This will mark the activity as Cancelled and free up its resource bookings.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selected.setStatus("Cancelled");
            activityListView.refresh();
            updateStatusBar();
            Navigator.setStatus("Cancelled: " + selected.getTitle());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    private ContextMenu buildContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem openItem = new MenuItem("Open Details");
        openItem.setOnAction(e -> {
            Activity selected = activityListView.getSelectionModel().getSelectedItem();
            if (selected != null) Navigator.showConduct(selected);
        });

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            Activity selected = activityListView.getSelectionModel().getSelectedItem();
            if (selected != null) Navigator.showActivityForm(selected);
        });

        MenuItem approveItem = new MenuItem("Approve");
        approveItem.setOnAction(e -> approveSelected());

        MenuItem cancelItem = new MenuItem("Cancel Activity");
        cancelItem.setOnAction(e -> cancelSelected());

        menu.getItems().addAll(openItem, editItem, approveItem, new SeparatorMenuItem(), cancelItem);
        return menu;
    }

    private void updateStatusBar() {
        int total     = store.getActivities().size();
        int showing   = filteredActivities.size();
        long approved = store.getActivities().stream()
                .filter(a -> "Approved".equals(a.getStatus())).count();
        Navigator.setStatus(
            "Showing " + showing + " of " + total + " activities  |  Approved: " + approved
            + "  |  Logged in as: " + (store.getCurrentUser() != null ? store.getCurrentUser().getUsername() : "—")
        );
    }
}
