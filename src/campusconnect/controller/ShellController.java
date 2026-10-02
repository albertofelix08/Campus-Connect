package campusconnect.controller;

import campusconnect.Navigator;
import campusconnect.model.Resource;
import campusconnect.model.User;
import campusconnect.store.DataStore;
import campusconnect.ui.Theme;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/** Controller for main_shell.fxml: menu bar, sidebar, status bar. Screens live in the centre. */
public class ShellController implements Initializable {

    @FXML private BorderPane mainShell;
    @FXML private Label statusBarLabel;

    // Sidebar: nav buttons (for the active highlight) and the logged-in user block
    @FXML private Button navDashboard;
    @FXML private Button navNewActivity;
    @FXML private Button navResources;
    @FXML private Button navScheduler;
    @FXML private Button navReports;
    @FXML private Label  avatarLabel;
    @FXML private Label  userNameLabel;
    @FXML private Label  userRoleLabel;

    private final DataStore store = DataStore.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = store.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText(user.getRole());
            avatarLabel.setText(user.getUsername().substring(0, 1).toUpperCase());
        }

        Navigator.init(mainShell, statusBarLabel);
        Navigator.setNavListener(this::highlightNav);
        Navigator.showDashboard();
    }

    /** Marks the sidebar button that matches the screen that was just opened. */
    private void highlightNav(String fxml) {
        Button active;
        switch (fxml) {
            case "activity_form.fxml":    active = navNewActivity; break;
            case "resource_gallery.fxml": active = navResources;   break;
            case "scheduler.fxml":        active = navScheduler;   break;
            case "reports.fxml":          active = navReports;     break;
            default:                      active = navDashboard;   break;   // dashboard + activity detail
        }
        for (Button b : new Button[] { navDashboard, navNewActivity, navResources, navScheduler, navReports }) {
            b.getStyleClass().remove("active");
        }
        active.getStyleClass().add("active");
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @FXML private void handleNewActivity()   { Navigator.showActivityForm(null); }
    @FXML private void handleNavDashboard()  { Navigator.showDashboard(); }
    @FXML private void handleOpenResources() { Navigator.showResources(); }
    @FXML private void handleOpenScheduler() { Navigator.showScheduler(); }
    @FXML private void handleOpenReports()   { Navigator.showReports(); }

    // ── Activity menu (acts on the dashboard's selection) ───────────────────

    @FXML
    private void handleApproveActivity() {
        DashboardController d = Navigator.getDashboard();
        if (d == null) { Navigator.setStatus("Open the Dashboard and select an activity first."); return; }
        d.approveSelected();
    }

    @FXML
    private void handleCancelActivity() {
        DashboardController d = Navigator.getDashboard();
        if (d == null) { Navigator.setStatus("Open the Dashboard and select an activity first."); return; }
        d.cancelSelected();
    }

    // ── Resources > Add Resource ────────────────────────────────────────────

    @FXML
    private void handleAddResource() {
        Dialog<Resource> dialog = new Dialog<>();
        dialog.setTitle("Add Resource");
        dialog.setHeaderText("Add a new resource to the gallery");

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Projector - P03");
        ChoiceBox<String> kindChoice = new ChoiceBox<>();
        kindChoice.getItems().addAll("Hall", "Equipment", "Coordinator", "Volunteer");
        kindChoice.setValue("Equipment");
        TextField contactField = new TextField();
        contactField.setPromptText("optional (people only)");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(15));
        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Kind:"), kindChoice);
        grid.addRow(2, new Label("Contact:"), contactField);
        dialog.getDialogPane().setContent(grid);
        Theme.style(dialog);

        ButtonType addType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        Node addButton = dialog.getDialogPane().lookupButton(addType);
        addButton.setDisable(true);
        nameField.textProperty().addListener((o, ov, nv) -> addButton.setDisable(nv.trim().isEmpty()));

        dialog.setResultConverter(bt -> {
            if (bt != addType) return null;
            String kind = kindChoice.getValue();
            String type = (kind.equals("Hall") || kind.equals("Equipment")) ? "Physical" : "Human";
            Resource r = new Resource(nameField.getText().trim(), type, kind);
            if (!contactField.getText().trim().isEmpty()) r.setContactInfo(contactField.getText().trim());
            return r;
        });

        Optional<Resource> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        Resource r = result.get();
        boolean duplicate = store.getResources().stream()
                .anyMatch(x -> x.getName().equalsIgnoreCase(r.getName()));
        if (duplicate) {
            Alert dup = new Alert(Alert.AlertType.WARNING, "A resource named \"" + r.getName() + "\" already exists.");
            Theme.style(dup);
            dup.showAndWait();
            return;
        }
        store.getResources().add(r);
        Navigator.showResources();
        Navigator.setStatus("Added resource: " + r.getName());
    }

    // ── Help / Exit ─────────────────────────────────────────────────────────

    @FXML
    private void handleAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About CampusConnect");
        about.setHeaderText("CampusConnect v1.0 — Polished Edition");
        about.setContentText("An Integrated Activity Planning, Scheduling and Execution System.\n\n"
                + "Unit V JavaFX Mini Project\nB.E./B.Tech. Computer Science and Engineering\n\n"
                + Theme.AI_NOTE + ".\n"
                + "The main branch is the original hand-built project; this branch shows what the same "
                + "Java app looks like when AI and modern tooling are used on the interface. "
                + "The app logic is unchanged.");
        Theme.style(about);
        about.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE); // stops long text truncating to "..."
        about.showAndWait();
    }

    @FXML
    private void handleExit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Exit");
        confirm.setContentText("Are you sure you want to exit CampusConnect?");
        Theme.style(confirm);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }
}
