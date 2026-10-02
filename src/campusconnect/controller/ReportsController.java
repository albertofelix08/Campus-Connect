package campusconnect.controller;

import campusconnect.model.Activity;
import campusconnect.model.Booking;
import campusconnect.model.Resource;
import campusconnect.store.DataStore;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ReportsController implements Initializable {

    @FXML private ChoiceBox<String> reportTypeChoice;
    @FXML private ListView<String>  reportListView;
    @FXML private Label summaryLabel1;
    @FXML private Label summaryLabel2;
    @FXML private Label summaryLabel3;

    private DataStore store = DataStore.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reportTypeChoice.getItems().addAll(
            "Activity Calendar",
            "Resource Utilisation",
            "Participation Summary"
        );

        // Switch report when ChoiceBox changes. (This listener is the only trigger now: the FXML
        // also had onAction wired to the same method, so every change rendered the report twice.)
        reportTypeChoice.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> handleReportTypeChange()
        );

        // Show first report by default — setValue fires the listener above
        reportTypeChoice.setValue("Activity Calendar");
    }

    @FXML
    private void handleReportTypeChange() {
        String selected = reportTypeChoice.getValue();
        if (selected == null) return;
        switch (selected) {
            case "Activity Calendar":    showActivityCalendar();      break;
            case "Resource Utilisation": showResourceUtilisation();   break;
            case "Participation Summary": showParticipationSummary(); break;
        }
    }

    private void showActivityCalendar() {
        reportListView.getItems().clear();

        List<Activity> sorted = store.getActivities().stream()
            .sorted(Comparator.comparing(a -> a.getDate() != null ? a.getDate() : java.time.LocalDate.MIN))
            .collect(Collectors.toList());

        for (Activity a : sorted) {
            String line = String.format("%-30s  %-15s  %-12s  %s",
                a.getTitle(),
                a.getCategory() != null ? a.getCategory() : "—",
                a.getStatus() != null ? a.getStatus() : "—",
                a.getDate() != null ? a.getDate().toString() : "No date"
            );
            reportListView.getItems().add(line);
        }

        long approved  = store.getActivities().stream().filter(a -> "Approved".equals(a.getStatus())).count();
        long completed = store.getActivities().stream().filter(a -> "Completed".equals(a.getStatus())).count();

        summaryLabel1.setText("Total: "     + store.getActivities().size());
        summaryLabel2.setText("Approved: "  + approved);
        summaryLabel3.setText("Completed: " + completed);
    }

    private void showResourceUtilisation() {
        reportListView.getItems().clear();

        // Count bookings per resource
        Map<String, Long> bookingCounts = new LinkedHashMap<>();
        for (Resource r : store.getResources()) {
            bookingCounts.put(r.getName(), 0L);
        }
        for (Booking b : store.getBookings()) {
            if (!b.isActive() || b.getResource() == null) continue;   // cancelled activities free their resources
            String name = b.getResource().getName();
            bookingCounts.put(name, bookingCounts.getOrDefault(name, 0L) + 1);
        }

        bookingCounts.forEach((name, count) -> {
            String line = String.format("%-30s  Bookings: %d", name, count);
            reportListView.getItems().add(line);
        });

        long totalBookings = store.getBookings().stream().filter(Booking::isActive).count();
        long usedResources = bookingCounts.values().stream().filter(c -> c > 0).count();

        summaryLabel1.setText("Total Resources: " + store.getResources().size());
        summaryLabel2.setText("In Use: "          + usedResources);
        summaryLabel3.setText("Total Bookings: "  + totalBookings);
    }

    private void showParticipationSummary() {
        reportListView.getItems().clear();

        for (Activity a : store.getActivities()) {
            int expected = a.getExpectedParticipants();
            int actual   = a.getActualParticipants();
            String diff  = actual > 0 ? (actual >= expected ? "✓" : "↓ " + (expected - actual) + " short") : "Not conducted yet";

            String line = String.format("%-30s  Expected: %-5d  Actual: %-5d  %s",
                a.getTitle(), expected, actual, diff
            );
            reportListView.getItems().add(line);
        }

        int totalExpected = store.getActivities().stream()
            .mapToInt(Activity::getExpectedParticipants).sum();
        int totalActual = store.getActivities().stream()
            .mapToInt(Activity::getActualParticipants).sum();

        summaryLabel1.setText("Total Activities: " + store.getActivities().size());
        summaryLabel2.setText("Total Expected: "   + totalExpected);
        summaryLabel3.setText("Total Actual: "     + totalActual);
    }
}
