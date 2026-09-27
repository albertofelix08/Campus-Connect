package campusconnect.controller;

import campusconnect.model.Activity;
import campusconnect.store.DataStore;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SchedulerController implements Initializable {

    @FXML private GridPane        calendarGrid;
    @FXML private ComboBox<String> venueFilterCombo;
    @FXML private BorderPane      mainShell;

    private DataStore store = DataStore.getInstance();

    // Time slots for rows
    private static final String[] TIME_SLOTS = {
        "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00",
        "12:00 - 13:00", "14:00 - 15:00", "15:00 - 16:00",
        "16:00 - 17:00"
    };

    // Show 5 days starting from today
    private LocalDate startDate = LocalDate.now();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Populate venue filter
        venueFilterCombo.getItems().add("All Venues");
        store.getResources().forEach(r -> {
            if (r.getSubType().equals("Hall")) {
                venueFilterCombo.getItems().add(r.getName());
            }
        });
        venueFilterCombo.setValue("All Venues");

        buildCalendarGrid(null);
    }

    @FXML
    private void handleVenueFilter() {
        String selected = venueFilterCombo.getValue();
        buildCalendarGrid("All Venues".equals(selected) ? null : selected);
    }

    private void buildCalendarGrid(String venueFilter) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Column constraints — first col is labels, rest are days
        ColumnConstraints labelCol = new ColumnConstraints(120);
        calendarGrid.getColumnConstraints().add(labelCol);
        for (int d = 0; d < 5; d++) {
            ColumnConstraints dayCol = new ColumnConstraints(160);
            calendarGrid.getColumnConstraints().add(dayCol);
        }

        // Header row — day names + dates
        Label cornerLabel = new Label("Time / Day");
        cornerLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
        calendarGrid.add(cornerLabel, 0, 0);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE dd/MM");
        for (int d = 0; d < 5; d++) {
            LocalDate day = startDate.plusDays(d);
            Label dayHeader = new Label(day.format(fmt));
            dayHeader.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-alignment: center;");
            dayHeader.setMaxWidth(Double.MAX_VALUE);
            calendarGrid.add(dayHeader, d + 1, 0);
        }

        // Time slot rows
        for (int r = 0; r < TIME_SLOTS.length; r++) {
            final String slot = TIME_SLOTS[r];

            // Row label
            Label slotLabel = new Label(slot);
            slotLabel.setStyle("-fx-padding: 5; -fx-font-size: 11;");
            calendarGrid.add(slotLabel, 0, r + 1);

            // Day cells
            for (int d = 0; d < 5; d++) {
                final LocalDate cellDate = startDate.plusDays(d);
                Activity activity = findActivity(cellDate, slot, venueFilter);

                StackPane cell = buildCell(activity, cellDate, slot);
                calendarGrid.add(cell, d + 1, r + 1);
            }
        }
    }

    private StackPane buildCell(Activity activity, LocalDate date, String slot) {
        StackPane cell = new StackPane();
        cell.setMinSize(150, 50);
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        if (activity != null) {
            // Occupied cell — show activity name
            Label actLabel = new Label(activity.getTitle());
            actLabel.setStyle("-fx-wrap-text: true; -fx-font-size: 11; -fx-padding: 4;");
            actLabel.setMaxWidth(145);
            cell.getChildren().add(actLabel);
            cell.setStyle("-fx-background-color: #AED6F1; -fx-border-color: #85C1E9; -fx-border-width: 1;");

            // Double-click on occupied cell opens the activity detail
            cell.setOnMouseClicked((MouseEvent e) -> {
                if (e.getClickCount() == 2) {
                    openConductForActivity(activity);
                }
            });

        } else {
            // Free cell — double-click to create a new activity in this slot
            Label freeLabel = new Label("+");
            freeLabel.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 18;");
            cell.getChildren().add(freeLabel);
            cell.setStyle("-fx-background-color: #FDFEFE; -fx-border-color: #D5D8DC; -fx-border-width: 1;");

            cell.setOnMouseClicked((MouseEvent e) -> {
                if (e.getClickCount() == 2) {
                    openNewActivityForSlot(date, slot);
                }
            });
        }

        return cell;
    }

    private Activity findActivity(LocalDate date, String slot, String venueFilter) {
        List<Activity> matches = store.getActivities().stream()
            .filter(a -> date.equals(a.getDate()) && slot.equals(a.getTimeSlot()))
            .collect(Collectors.toList());

        if (venueFilter != null) {
            matches = matches.stream()
                .filter(a -> venueFilter.equals(a.getVenue()))
                .collect(Collectors.toList());
        }

        return matches.isEmpty() ? null : matches.get(0);
    }

    private void openConductForActivity(Activity activity) {
        if (mainShell == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/campusconnect/view/conduct.fxml"));
            Parent view = loader.load();
            ConductController cc = loader.getController();
            cc.setActivity(activity);
            cc.setShell(mainShell);
            mainShell.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openNewActivityForSlot(LocalDate date, String slot) {
        if (mainShell == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/campusconnect/view/activity_form.fxml"));
            Parent view = loader.load();
            ActivityFormController fc = loader.getController();

            // Pre-fill the date and time slot
            Activity prefilled = new Activity();
            prefilled.setDate(date);
            prefilled.setTimeSlot(slot);
            fc.setActivity(prefilled);
            fc.setShell(mainShell);

            mainShell.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setShell(BorderPane shell) {
        this.mainShell = shell;
    }
}
