package campusconnect.controller;

import campusconnect.Navigator;
import campusconnect.model.Activity;
import campusconnect.store.DataStore;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    private static final int DAYS_SHOWN = 5;

    @FXML private GridPane        calendarGrid;
    @FXML private ComboBox<String> venueFilterCombo;
    @FXML private Label           rangeLabel;

    private final DataStore store = DataStore.getInstance();

    // Time slots for rows
    private static final String[] TIME_SLOTS = {
        "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00",
        "12:00 - 13:00", "14:00 - 15:00", "15:00 - 16:00",
        "16:00 - 17:00"
    };

    // First day of the visible window (was fixed at "today", so anything later was unreachable)
    private LocalDate startDate = LocalDate.now();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Populate venue filter
        venueFilterCombo.getItems().add("All Venues");
        store.getResources().forEach(r -> {
            if ("Hall".equals(r.getSubType())) venueFilterCombo.getItems().add(r.getName());
        });
        venueFilterCombo.setValue("All Venues");

        rebuild();
    }

    @FXML private void handleVenueFilter() { rebuild(); }
    @FXML private void handlePrev()  { startDate = startDate.minusDays(DAYS_SHOWN); rebuild(); }
    @FXML private void handleNext()  { startDate = startDate.plusDays(DAYS_SHOWN);  rebuild(); }
    @FXML private void handleToday() { startDate = LocalDate.now();                 rebuild(); }

    private String currentVenueFilter() {
        String selected = venueFilterCombo.getValue();
        return (selected == null || "All Venues".equals(selected)) ? null : selected;
    }

    private void rebuild() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd MMM yyyy");
        rangeLabel.setText(startDate.format(f) + "  –  " + startDate.plusDays(DAYS_SHOWN - 1).format(f));
        buildCalendarGrid(currentVenueFilter());
    }

    private void buildCalendarGrid(String venueFilter) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Column constraints — first col is labels, rest are days
        calendarGrid.getColumnConstraints().add(new ColumnConstraints(120));
        for (int d = 0; d < DAYS_SHOWN; d++) {
            calendarGrid.getColumnConstraints().add(new ColumnConstraints(160));
        }

        // Header row — day names + dates
        Label cornerLabel = new Label("Time / Day");
        cornerLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
        calendarGrid.add(cornerLabel, 0, 0);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE dd/MM");
        for (int d = 0; d < DAYS_SHOWN; d++) {
            LocalDate day = startDate.plusDays(d);
            Label dayHeader = new Label(day.format(fmt));
            dayHeader.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-alignment: center;");
            dayHeader.setMaxWidth(Double.MAX_VALUE);
            calendarGrid.add(dayHeader, d + 1, 0);
        }

        // Time slot rows
        for (int r = 0; r < TIME_SLOTS.length; r++) {
            final String slot = TIME_SLOTS[r];

            Label slotLabel = new Label(slot);
            slotLabel.setStyle("-fx-padding: 5; -fx-font-size: 11;");
            calendarGrid.add(slotLabel, 0, r + 1);

            for (int d = 0; d < DAYS_SHOWN; d++) {
                final LocalDate cellDate = startDate.plusDays(d);
                List<Activity> here = findActivities(cellDate, slot, venueFilter);
                calendarGrid.add(buildCell(here, cellDate, slot, venueFilter), d + 1, r + 1);
            }
        }
    }

    private StackPane buildCell(List<Activity> activities, LocalDate date, String slot, String venueFilter) {
        StackPane cell = new StackPane();
        cell.setMinSize(150, 50);
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        if (!activities.isEmpty()) {
            // Occupied cell — every activity in this slot is listed (two venues can run in parallel;
            // the old grid only ever showed the first one)
            VBox box = new VBox(3);
            for (Activity a : activities) {
                VBox entry = new VBox(1);
                Label title = new Label(a.getTitle());
                title.setStyle("-fx-wrap-text: true; -fx-font-size: 11; -fx-font-weight: bold;");
                title.setMaxWidth(145);
                entry.getChildren().add(title);
                if (venueFilter == null && a.getVenue() != null) {
                    Label venue = new Label(a.getVenue());
                    venue.setStyle("-fx-font-size: 10; -fx-text-fill: #34495E;");
                    entry.getChildren().add(venue);
                }
                entry.setOnMouseClicked((MouseEvent e) -> {
                    if (e.getClickCount() == 2) Navigator.showConduct(a);
                });
                box.getChildren().add(entry);
            }
            box.setStyle("-fx-padding: 4;");
            cell.getChildren().add(box);
            cell.setStyle("-fx-background-color: #AED6F1; -fx-border-color: #85C1E9; -fx-border-width: 1;");

        } else {
            // Free cell — double-click to create a new activity in this slot
            Label freeLabel = new Label("+");
            freeLabel.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 18;");
            cell.getChildren().add(freeLabel);
            cell.setStyle("-fx-background-color: #FDFEFE; -fx-border-color: #D5D8DC; -fx-border-width: 1;");

            cell.setOnMouseClicked((MouseEvent e) -> {
                if (e.getClickCount() == 2) Navigator.showNewActivity(date, slot, venueFilter);
            });
        }

        return cell;
    }

    // Cancelled activities no longer occupy a slot. Overlapping longer slots ("09:00 - 13:00")
    // are shown in every hourly row they cover.
    private List<Activity> findActivities(LocalDate date, String slot, String venueFilter) {
        return store.getActivities().stream()
            .filter(a -> date.equals(a.getDate()) && a.getTimeSlot() != null)
            .filter(a -> !"Cancelled".equals(a.getStatus()))
            .filter(a -> campusconnect.model.Booking.slotsOverlap(a.getTimeSlot(), slot))
            .filter(a -> venueFilter == null || venueFilter.equals(a.getVenue()))
            .collect(Collectors.toList());
    }
}
