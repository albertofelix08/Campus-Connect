package campusconnect.ui;

import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Single place that attaches the app stylesheet and holds small styling helpers.
 * (Polished edition — written with AI assistance, see README.md.)
 */
public final class Theme {

    private static final String STYLESHEET =
            Theme.class.getResource("/campusconnect/view/theme.css").toExternalForm();

    /** Shown wherever the app discloses how the polished branch was produced. */
    public static final String AI_NOTE = "Polished with AI assistance";

    private Theme() {}

    /** Attach the theme to a scene (safe to call more than once). */
    public static void apply(Scene scene) {
        if (!scene.getStylesheets().contains(STYLESHEET)) {
            scene.getStylesheets().add(STYLESHEET);
        }
    }

    /** Dialogs and alerts live in their own window, so they need the stylesheet too. */
    public static void style(Dialog<?> dialog) {
        var sheets = dialog.getDialogPane().getStylesheets();
        if (!sheets.contains(STYLESHEET)) sheets.add(STYLESHEET);
    }

    /** CSS class for a status pill / tinted cell, e.g. "Approved" -> "s-approved". */
    public static String statusClass(String status) {
        return status == null ? "s-proposed" : "s-" + status.toLowerCase();
    }

    /** CSS class for a resource type chip, e.g. "Hall" -> "rc-hall". */
    public static String resourceClass(String subType) {
        return subType == null ? "rc-other" : "rc-" + subType.toLowerCase();
    }

    /** Friendly placeholder for empty lists. */
    public static VBox emptyState(String title, String subtitle) {
        Label t = new Label(title);
        t.getStyleClass().add("empty-title");
        Label s = new Label(subtitle);
        s.getStyleClass().add("empty-sub");
        VBox box = new VBox(t, s);
        box.getStyleClass().add("empty-state");
        return box;
    }
}
