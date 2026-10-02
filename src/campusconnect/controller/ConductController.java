package campusconnect.controller;

import campusconnect.Navigator;
import campusconnect.model.Activity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ConductController implements Initializable {

    @FXML private ToggleButton proposedBtn;
    @FXML private ToggleButton approvedBtn;
    @FXML private ToggleButton ongoingBtn;
    @FXML private ToggleButton completedBtn;
    @FXML private ToggleButton cancelledBtn;
    @FXML private ToggleGroup  statusGroup;

    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label venueLabel;
    @FXML private Label dateLabel;
    @FXML private Label coordinatorLabel;
    @FXML private Label expectedLabel;

    @FXML private TextField actualAttendanceField;
    @FXML private TextArea  remarksArea;
    @FXML private Label     errorLabel;

    private Activity currentActivity;
    private boolean programmaticChange = false;   // true while WE move the toggle (loading / snapping back)

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Guard illegal status jumps — only allow forward transitions
        statusGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (programmaticChange) return;

            if (newVal == null) {
                // Clicking the selected button would deselect everything — put it back
                if (oldVal != null) selectQuietly((ToggleButton) oldVal);
                return;
            }
            if (currentActivity == null) return;

            String current = currentActivity.getStatus();
            String target  = ((ToggleButton) newVal).getText();

            if (!isValidTransition(current, target)) {
                showMessage("Cannot move from \"" + current + "\" to \"" + target + "\" directly.", false);
                // snap back to whatever was selected before this click
                selectQuietly(oldVal != null ? (ToggleButton) oldVal : getButtonForStatus(current));
            } else {
                showMessage("", false);
            }
        });
    }

    public void setActivity(Activity activity) {
        this.currentActivity = activity;

        titleLabel.setText(activity.getTitle());
        categoryLabel.setText(activity.getCategory() != null ? activity.getCategory() : "—");
        venueLabel.setText(activity.getVenue() != null ? activity.getVenue() : "—");
        dateLabel.setText(activity.getDate() != null ? activity.getDate().toString() : "—");
        coordinatorLabel.setText(activity.getCoordinator() != null ? activity.getCoordinator() : "—");
        expectedLabel.setText(String.valueOf(activity.getExpectedParticipants()));

        if (activity.getActualParticipants() > 0) {
            actualAttendanceField.setText(String.valueOf(activity.getActualParticipants()));
        }
        if (activity.getRemarks() != null) {
            remarksArea.setText(activity.getRemarks());
        }

        // Show the CURRENT status without tripping the transition guard
        // (it used to fire "Cannot move from Proposed to Proposed" the moment the screen opened)
        selectQuietly(getButtonForStatus(activity.getStatus()));
        showMessage("", false);
    }

    @FXML
    private void handleSaveChanges(ActionEvent event) {
        if (currentActivity == null) return;

        // Validate attendance field before touching the activity
        String attendanceText = actualAttendanceField.getText().trim();
        int attendance = 0;
        boolean hasAttendance = !attendanceText.isEmpty();
        if (hasAttendance) {
            try {
                attendance = Integer.parseInt(attendanceText);
            } catch (NumberFormatException e) {
                showMessage("Attendance must be a whole number.", false);
                return;
            }
            if (attendance < 0) {
                showMessage("Attendance can't be negative.", false);
                return;
            }
        }

        if (hasAttendance) currentActivity.setActualParticipants(attendance);

        // Save selected status
        ToggleButton selectedBtn = (ToggleButton) statusGroup.getSelectedToggle();
        if (selectedBtn != null) {
            currentActivity.setStatus(selectedBtn.getText());
        }

        currentActivity.setRemarks(remarksArea.getText().trim());
        showMessage("Changes saved.", true);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        Navigator.showDashboard();
    }

    // Valid status flow: Proposed → Approved → Ongoing → Completed
    //                    Any → Cancelled
    private boolean isValidTransition(String from, String to) {
        if (from == null || from.equals(to)) return true;   // staying put is always fine
        if (to.equals("Cancelled")) return !from.equals("Completed") && !from.equals("Cancelled");
        switch (from) {
            case "Proposed":  return to.equals("Approved");
            case "Approved":  return to.equals("Ongoing") || to.equals("Proposed");
            case "Ongoing":   return to.equals("Completed");
            case "Completed": return false; // terminal state
            case "Cancelled": return false; // terminal state
            default:          return true;
        }
    }

    private ToggleButton getButtonForStatus(String status) {
        if (status == null) return proposedBtn;
        switch (status) {
            case "Approved":  return approvedBtn;
            case "Ongoing":   return ongoingBtn;
            case "Completed": return completedBtn;
            case "Cancelled": return cancelledBtn;
            default:          return proposedBtn;
        }
    }

    private void selectQuietly(ToggleButton button) {
        programmaticChange = true;
        try {
            button.setSelected(true);
        } finally {
            programmaticChange = false;
        }
    }

    private void showMessage(String text, boolean success) {
        errorLabel.getStyleClass().removeAll("error-text", "success-text");
        errorLabel.getStyleClass().add(success ? "success-text" : "error-text");
        errorLabel.setText(text);
    }
}
