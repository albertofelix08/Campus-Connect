package campusconnect.controller;

import campusconnect.Navigator;
import campusconnect.model.Resource;
import campusconnect.store.DataStore;
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

    private final DataStore store = DataStore.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Navigator.init(mainShell, statusBarLabel);
        Navigator.showDashboard();
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
            new Alert(Alert.AlertType.WARNING, "A resource named \"" + r.getName() + "\" already exists.").showAndWait();
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
            Platform.exit();
        }
    }
}
