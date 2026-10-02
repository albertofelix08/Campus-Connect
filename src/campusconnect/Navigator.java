package campusconnect;

import campusconnect.controller.ActivityFormController;
import campusconnect.controller.ConductController;
import campusconnect.controller.DashboardController;
import campusconnect.model.Activity;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.time.LocalDate;

/**
 * Single place that swaps the centre of the main shell.
 * Every screen used to hand-roll its own FXMLLoader + setShell() plumbing and most of it
 * was never wired up (the shell reference was silently null). Controllers now just call
 * Navigator.showXxx().
 */
public final class Navigator {

    private static final String VIEW = "/campusconnect/view/";

    private static BorderPane shell;
    private static Label statusLabel;
    private static DashboardController dashboard;   // non-null only while the dashboard is on screen

    private Navigator() {}

    public static void init(BorderPane shellPane, Label status) {
        shell = shellPane;
        statusLabel = status;
    }

    public static void setStatus(String text) {
        if (statusLabel != null) statusLabel.setText(text);
    }

    /** The dashboard controller if the dashboard is the current screen, otherwise null. */
    public static DashboardController getDashboard() { return dashboard; }

    public static void registerDashboard(DashboardController d) { dashboard = d; }

    // ── Screens ──────────────────────────────────────────────────────────────

    public static void showDashboard() { load("dashboard.fxml"); }

    public static void showResources() { load("resource_gallery.fxml"); setStatus("Resource Gallery"); }

    public static void showScheduler() { load("scheduler.fxml"); setStatus("Scheduler"); }

    public static void showReports()   { load("reports.fxml"); setStatus("Reports"); }

    /** Open the form to edit an existing activity, or a blank one when {@code toEdit} is null. */
    public static void showActivityForm(Activity toEdit) {
        ActivityFormController fc = load("activity_form.fxml");
        if (fc != null && toEdit != null) fc.setActivity(toEdit);
        setStatus(toEdit == null ? "New activity" : "Editing: " + toEdit.getTitle());
    }

    /** Open a blank form with date / slot / venue pre-filled (used by the scheduler). */
    public static void showNewActivity(LocalDate date, String slot, String venue) {
        ActivityFormController fc = load("activity_form.fxml");
        if (fc != null) fc.prefill(date, slot, venue);
        setStatus("New activity");
    }

    public static void showConduct(Activity activity) {
        ConductController cc = load("conduct.fxml");
        if (cc != null) cc.setActivity(activity);
        setStatus("Activity detail: " + activity.getTitle());
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private static <T> T load(String fxml) {
        dashboard = null; // re-registered by DashboardController.initialize if that's what we load
        if (shell == null) return null;
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(VIEW + fxml));
            Parent view = loader.load();
            shell.setCenter(view);
            return loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Could not open screen: " + fxml);
            return null;
        }
    }
}
