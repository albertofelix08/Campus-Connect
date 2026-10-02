package campusconnect.ui;

import campusconnect.model.Activity;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Card-style row for an {@link Activity}: title + details on the left, status pill on the right. */
public final class ActivityCell extends ListCell<Activity> {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final Label title = new Label();
    private final Label meta  = new Label();
    private final Label pill  = new Label();
    private final HBox  card  = new HBox(14);

    public ActivityCell() {
        title.getStyleClass().add("activity-title");
        meta.getStyleClass().add("activity-meta");
        pill.getStyleClass().add("pill");

        VBox text = new VBox(4, title, meta);
        HBox.setHgrow(text, Priority.ALWAYS);

        card.getChildren().addAll(text, pill);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("activity-card");
        setPrefWidth(0); // let the list drive the width instead of the content
    }

    @Override
    protected void updateItem(Activity a, boolean empty) {
        super.updateItem(a, empty);
        if (empty || a == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        title.setText(a.getTitle());

        List<String> parts = new ArrayList<>();
        if (a.getCategory() != null) parts.add(a.getCategory());
        if (a.getVenue() != null)    parts.add(a.getVenue());
        if (a.getDate() != null)     parts.add(a.getDate().format(DATE));
        if (a.getTimeSlot() != null) parts.add(a.getTimeSlot());
        meta.setText(String.join("  ·  ", parts));

        pill.setText(a.getStatus());
        pill.getStyleClass().removeIf(c -> c.startsWith("s-"));
        pill.getStyleClass().add(Theme.statusClass(a.getStatus()));

        setText(null);
        setGraphic(card);
    }
}
