package campusconnect.controller;

import campusconnect.Navigator;
import campusconnect.model.Activity;
import campusconnect.store.DataStore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ActivityFormController implements Initializable {

    @FXML private Label formTitleLabel;
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

    private final DataStore store = DataStore.getInstance();
    private Activity editingActivity = null;   // null = creating a brand new activity

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Populate venue combo from resources in the store
        store.getResources().forEach(r -> {
            if ("Hall".equals(r.getSubType())) venueCombo.getItems().add(r.getName());
        });

        // Time slots
        timeSlotCombo.getItems().addAll(
            "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00",
            "12:00 - 13:00", "14:00 - 15:00", "15:00 - 16:00",
            "16:00 - 17:00", "09:00 - 13:00", "14:00 - 17:00", "09:00 - 17:00"
        );

        // Coordinators from human resources
        store.getResources().forEach(r -> {
            if ("Coordinator".equals(r.getSubType())) coordinatorCombo.getItems().add(r.getName());
        });

        // Digits only. A text listener (unlike a KEY_TYPED filter) also catches paste,
        // which used to slip letters through and crash Integer.parseInt on save.
        participantsField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null) return;
            String cleaned = newText.replaceAll("[^0-9]", "");
            if (cleaned.length() > 7) cleaned = cleaned.substring(0, 7);
            if (!cleaned.equals(newText)) participantsField.setText(cleaned);
        });

        // Default date to today
        datePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        // ── Validation ────────────────────────────────────────────────────────
        String title = titleField.getText().trim();
        if (title.isEmpty()) { errorLabel.setText("Title cannot be empty."); return; }
        if (categoryGroup.getSelectedToggle() == null) { errorLabel.setText("Please select a category."); return; }
        if (datePicker.getValue() == null) { errorLabel.setText("Please pick a date."); return; }
        if (timeSlotCombo.getValue() == null) { errorLabel.setText("Please select a time slot."); return; }
        if (venueCombo.getValue() == null) { errorLabel.setText("Please select a venue."); return; }

        int expected = 0;
        String participants = participantsField.getText().trim();
        if (!participants.isEmpty()) {
            try {
                expected = Integer.parseInt(participants);
            } catch (NumberFormatException ex) {
                errorLabel.setText("Expected participants must be a valid number.");
                return;
            }
        }

        // Venue clash with another (non-cancelled) activity
        Activity clash = store.findVenueConflict(editingActivity, venueCombo.getValue(),
                datePicker.getValue(), timeSlotCombo.getValue());
        if (clash != null) {
            errorLabel.setText(venueCombo.getValue() + " is already used by \"" + clash.getTitle()
                    + "\" on " + clash.getDate() + " (" + clash.getTimeSlot() + ").");
            return;
        }

        // ── Build / update Activity object ────────────────────────────────────
        Activity activity = (editingActivity != null) ? editingActivity : new Activity();

        activity.setTitle(title);
        activity.setDescription(descriptionArea.getText().trim());
        activity.setDate(datePicker.getValue());
        activity.setVenue(venueCombo.getValue());
        activity.setTimeSlot(timeSlotCombo.getValue());
        activity.setCoordinator(coordinatorCombo.getValue());
        activity.setFacilities(getSelectedFacilities());
        activity.setCategory(((RadioButton) categoryGroup.getSelectedToggle()).getText());
        activity.setExpectedParticipants(expected);   // clearing the field on edit now really clears it

        // Add to store only if it's brand new
        if (editingActivity == null) {
            store.getActivities().add(activity);
        }

        errorLabel.setText("");
        Navigator.showDashboard();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Navigator.showDashboard();
    }

    /** Edit an EXISTING activity: fills the form and makes Save update it in place. */
    public void setActivity(Activity activity) {
        this.editingActivity = activity;
        formTitleLabel.setText("Edit Activity");

        titleField.setText(activity.getTitle());
        descriptionArea.setText(activity.getDescription() != null ? activity.getDescription() : "");
        datePicker.setValue(activity.getDate());
        venueCombo.setValue(activity.getVenue());
        timeSlotCombo.setValue(activity.getTimeSlot());
        coordinatorCombo.setValue(activity.getCoordinator());

        participantsField.setText(activity.getExpectedParticipants() > 0
                ? String.valueOf(activity.getExpectedParticipants()) : "");

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

    /**
     * Pre-fill a NEW activity (used by the scheduler's free cells).
     * The scheduler used to pass a throw-away Activity to setActivity(), which made the form
     * think it was editing that object — so Save never added anything to the store.
     */
    public void prefill(java.time.LocalDate date, String slot, String venue) {
        editingActivity = null;
        if (date != null)  datePicker.setValue(date);
        if (slot != null)  timeSlotCombo.setValue(slot);
        if (venue != null) venueCombo.setValue(venue);
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
