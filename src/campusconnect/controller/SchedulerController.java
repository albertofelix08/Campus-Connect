package campusconnect.controller;

import campusconnect.Navigator;
import campusconnect.model.Activity;
import campusconnect.store.DataStore;
import campusconnect.ui.Theme;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
        // Label column is fixed; day columns share whatever width is left, so all five days always fit.
        calendarGrid.getColumnConstraints().add(new ColumnConstraints(112));
        for (int d = 0; d < DAYS_SHOWN; d++) {
            ColumnConstraints day = new ColumnConstraints(100, 150, Double.MAX_VALUE);
            day.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(day);
        }

        // Header row — day names + dates
        Label cornerLabel = new Label("Time / Day");
        cornerLabel.getStyleClass().add("cal-header");
        calendarGrid.add(cornerLabel, 0, 0);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE dd/MM");
        for (int d = 0; d < DAYS_SHOWN; d++) {
            LocalDate day = startDate.plusDays(d);
            Label dayHeader = new Label(day.format(fmt));
            dayHeader.getStyleClass().add("cal-header");
            if (day.equals(LocalDate.now())) dayHeader.getStyleClass().add("today");
            dayHeader.setMaxWidth(Double.MAX_VALUE);
            calendarGrid.add(dayHeader, d + 1, 0);
        }

        // Time slot rows
        for (int r = 0; r < TIME_SLOTS.length; r++) {
            final String slot = TIME_SLOTS[r];

            Label slotLabel = new Label(slot);
            slotLabel.getStyleClass().add("cal-slot");
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
        cell.setMinSize(100, 50);
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        cell.getStyleClass().add("cal-cell");
        if (date.equals(LocalDate.now())) cell.getStyleClass().add("today");

        if (!activities.isEmpty()) {
            // Occupied cell — every activity in this slot is listed (two venues can run in parallel;
            // the old grid only ever showed the first one)
            VBox box = new VBox(3);
            for (Activity a : activities) {
                VBox entry = new VBox(1);
                Label title = new Label(a.getTitle());
                title.getStyleClass().add("cal-title");
                title.setWrapText(true);
                title.setMaxWidth(Double.MAX_VALUE);
                entry.getChildren().add(title);
                if (venueFilter == null && a.getVenue() != null) {
                    Label venue = new Label(a.getVenue());
                    venue.getStyleClass().add("cal-venue");
                    entry.getChildren().add(venue);
                }
                entry.setOnMouseClicked((MouseEvent e) -> {
                    if (e.getClickCount() == 2) Navigator.showConduct(a);
                });
                box.getChildren().add(entry);
            }
            box.setPadding(new javafx.geometry.Insets(6));
            cell.getChildren().add(box);
            cell.setAlignment(Pos.TOP_LEFT);
            // tint the whole cell with the (first) activity's status colour
            cell.getStyleClass().addAll("cal-cell-booked", Theme.statusClass(activities.get(0).getStatus()));

        } else {
            // Free cell — double-click to create a new activity in this slot
            Label freeLabel = new Label("+");
            freeLabel.getStyleClass().add("cal-plus");
            cell.getChildren().add(freeLabel);
            cell.getStyleClass().add("cal-cell-free");

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
