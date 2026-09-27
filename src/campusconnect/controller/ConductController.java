package campusconnect.controller;

import campusconnect.model.Activity;
import campusconnect.store.DataStore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

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

    private DataStore store = DataStore.getInstance();
    private Activity currentActivity;
    private BorderPane mainShell;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Guard illegal status jumps — only allow forward transitions
        statusGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                // Don't allow deselecting all
                oldVal.setSelected(true);
                return;
            }
            if (currentActivity == null) return;

            String current = currentActivity.getStatus();
            String target  = ((ToggleButton) newVal).getText();

            if (!isValidTransition(current, target)) {
                errorLabel.setText("Cannot move from \"" + current + "\" to \"" + target + "\" directly.");
                // snap back to the correct button
                getButtonForStatus(current).setSelected(true);
            } else {
                errorLabel.setText("");
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

        getButtonForStatus(activity.getStatus()).setSelected(true);
    }

    public void setShell(BorderPane shell) {
        this.mainShell = shell;
    }

    @FXML
    private void handleSaveChanges(ActionEvent event) {
        if (currentActivity == null) return;

        // Validate attendance field
        String attendanceText = actualAttendanceField.getText().trim();
        if (!attendanceText.isEmpty()) {
            try {
                int attendance = Integer.parseInt(attendanceText);
                currentActivity.setActualParticipants(attendance);
            } catch (NumberFormatException e) {
                errorLabel.setText("Attendance must be a number.");
                return;
            }
        }

        // Save selected status
        ToggleButton selectedBtn = (ToggleButton) statusGroup.getSelectedToggle();
        if (selectedBtn != null) {
            currentActivity.setStatus(selectedBtn.getText());
        }

        currentActivity.setRemarks(remarksArea.getText().trim());
        errorLabel.setText("Changes saved.");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (mainShell == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/campusconnect/view/dashboard.fxml"));
            mainShell.setCenter(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Valid status flow: Proposed → Approved → Ongoing → Completed
    //                    Any → Cancelled
    private boolean isValidTransition(String from, String to) {
        if (to.equals("Cancelled")) return true;
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
        switch (status) {
            case "Approved":  return approvedBtn;
            case "Ongoing":   return ongoingBtn;
            case "Completed": return completedBtn;
            case "Cancelled": return cancelledBtn;
            default:          return proposedBtn;
        }
    }
}
