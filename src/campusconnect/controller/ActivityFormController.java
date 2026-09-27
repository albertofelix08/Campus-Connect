package campusconnect.controller;

import campusconnect.model.Activity;
import campusconnect.store.DataStore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ActivityFormController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextArea  descriptionArea;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> venueCombo;
    @FXML private ComboBox<String> timeSlotCombo;
    @FXML private ComboBox<String> coordinatorCombo;
    @FXML private TextField participantsField;

    @FXML private RadioButton academicRadio;
    @FXML private RadioButton cocurricularRadio;
    @FXML private RadioButton extracurricularRadio;
    @FXML private RadioButton nonAcademicRadio;
    @FXML private RadioButton otherRadio;
    @FXML private ToggleGroup categoryGroup;

    @FXML private CheckBox projectorCheck;
    @FXML private CheckBox micCheck;
    @FXML private CheckBox wifiCheck;
    @FXML private CheckBox cateringCheck;
    @FXML private CheckBox photographyCheck;

    @FXML private Label errorLabel;

    private DataStore store = DataStore.getInstance();
    private Activity editingActivity = null;
    private BorderPane mainShell;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Populate venue combo from resources in the store
        store.getResources().forEach(r -> {
            if (r.getSubType().equals("Hall")) {
                venueCombo.getItems().add(r.getName());
            }
        });

        // Time slots
        timeSlotCombo.getItems().addAll(
            "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00",
            "12:00 - 13:00", "14:00 - 15:00", "15:00 - 16:00",
            "16:00 - 17:00", "09:00 - 13:00", "14:00 - 17:00", "09:00 - 17:00"
        );

        // Coordinators from human resources
        store.getResources().forEach(r -> {
            if (r.getSubType().equals("Coordinator")) {
                coordinatorCombo.getItems().add(r.getName());
            }
        });

        // Only allow numbers in the participants field (KeyEvent)
        participantsField.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (!e.getCharacter().matches("[0-9]")) {
                e.consume(); // block non-numeric input
            }
        });

        // Default date to today
        datePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        // ── Validation ────────────────────────────────────────────────────────
        if (titleField.getText().trim().isEmpty()) {
            errorLabel.setText("Title cannot be empty.");
            return;
        }
        if (categoryGroup.getSelectedToggle() == null) {
            errorLabel.setText("Please select a category.");
            return;
        }
        if (datePicker.getValue() == null) {
            errorLabel.setText("Please pick a date.");
            return;
        }
        if (venueCombo.getValue() == null) {
            errorLabel.setText("Please select a venue.");
            return;
        }

        // ── Build / update Activity object ────────────────────────────────────
        Activity activity = (editingActivity != null) ? editingActivity : new Activity();

        activity.setTitle(titleField.getText().trim());
        activity.setDescription(descriptionArea.getText().trim());
        activity.setDate(datePicker.getValue());
        activity.setVenue(venueCombo.getValue());
        activity.setTimeSlot(timeSlotCombo.getValue());
        activity.setCoordinator(coordinatorCombo.getValue());
        activity.setFacilities(getSelectedFacilities());

        // Get category label from selected radio button
        RadioButton selected = (RadioButton) categoryGroup.getSelectedToggle();
        activity.setCategory(selected.getText());

        if (!participantsField.getText().isEmpty()) {
            activity.setExpectedParticipants(Integer.parseInt(participantsField.getText()));
        }

        // Add to store only if it's brand new
        if (editingActivity == null) {
            store.getActivities().add(activity);
        }

        errorLabel.setText("");

        // Go back to dashboard after save
        if (mainShell != null) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/campusconnect/view/dashboard.fxml")
                );
                mainShell.setCenter(loader.load());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (mainShell != null) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/campusconnect/view/dashboard.fxml")
                );
                mainShell.setCenter(loader.load());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Called when editing an existing activity
    public void setActivity(Activity activity) {
        this.editingActivity = activity;

        titleField.setText(activity.getTitle());
        descriptionArea.setText(activity.getDescription() != null ? activity.getDescription() : "");
        datePicker.setValue(activity.getDate());
        venueCombo.setValue(activity.getVenue());
        timeSlotCombo.setValue(activity.getTimeSlot());
        coordinatorCombo.setValue(activity.getCoordinator());

        if (activity.getExpectedParticipants() > 0) {
            participantsField.setText(String.valueOf(activity.getExpectedParticipants()));
        }

        // Set the right radio button
        switch (activity.getCategory() != null ? activity.getCategory() : "") {
            case "Academic":         academicRadio.setSelected(true);        break;
            case "Co-curricular":    cocurricularRadio.setSelected(true);    break;
            case "Extra-curricular": extracurricularRadio.setSelected(true); break;
            case "Non-academic":     nonAcademicRadio.setSelected(true);     break;
            default:                 otherRadio.setSelected(true);           break;
        }

        // Re-tick the facility checkboxes
        List<String> fac = activity.getFacilities();
        if (fac != null) {
            projectorCheck.setSelected(fac.contains("Projector"));
            micCheck.setSelected(fac.contains("Mic Set"));
            wifiCheck.setSelected(fac.contains("Wi-Fi"));
            cateringCheck.setSelected(fac.contains("Catering"));
            photographyCheck.setSelected(fac.contains("Photography"));
        }
    }

    public void setShell(BorderPane shell) {
        this.mainShell = shell;
    }

    private List<String> getSelectedFacilities() {
        List<String> selected = new ArrayList<>();
        if (projectorCheck.isSelected())   selected.add("Projector");
        if (micCheck.isSelected())         selected.add("Mic Set");
        if (wifiCheck.isSelected())        selected.add("Wi-Fi");
        if (cateringCheck.isSelected())    selected.add("Catering");
        if (photographyCheck.isSelected()) selected.add("Photography");
        return selected;
    }
}
