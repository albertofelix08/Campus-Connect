package campusconnect.controller;

import campusconnect.model.Activity;
import campusconnect.model.Booking;
import campusconnect.model.Resource;
import campusconnect.store.DataStore;
import campusconnect.ui.ActivityCell;
import campusconnect.ui.Theme;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ResourceGalleryController implements Initializable {

    @FXML private FlowPane resourceFlowPane;
    @FXML private StackPane conflictOverlay;
    @FXML private Label     conflictTitleLabel;
    @FXML private Label     conflictDetailLabel;
    @FXML private ListView<Activity> dropTargetList;

    private final DataStore store = DataStore.getInstance();
    private Resource draggedResource = null;  // track what's being dragged

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hideOverlay();
        buildResourceCards();
        populateDropTarget();
    }

    // ── Build resource cards in the FlowPane ──────────────────────────────────

    private void buildResourceCards() {
        resourceFlowPane.getChildren().clear();

        for (Resource res : store.getResources()) {
            resourceFlowPane.getChildren().add(createCard(res));
        }
    }

    private VBox createCard(Resource res) {
        VBox card = new VBox(6);
        card.getStyleClass().add("resource-card");

        Label nameLabel    = new Label(res.getName());
        nameLabel.getStyleClass().add("resource-name");
        nameLabel.setWrapText(true);

        Label subTypeLabel = new Label(res.getSubType());
        subTypeLabel.getStyleClass().addAll("chip", Theme.resourceClass(res.getSubType()));

        // A resource can be booked many times for different slots, so "Booked" is derived
        // from its live bookings instead of being a one-way flag that never resets.
        long active = store.getBookings().stream()
                .filter(b -> b.isActive() && b.getResource() == res).count();
        Label statusLabel = new Label(active == 0 ? "✓ Available" : "● Booked × " + active);
        statusLabel.getStyleClass().add(active == 0 ? "avail-ok" : "avail-busy");

        card.getChildren().addAll(nameLabel, subTypeLabel, statusLabel);

        // ── Drag source ────────────────────────────────────────────────────
        card.setOnDragDetected((MouseEvent e) -> {
            draggedResource = res;
            card.getStyleClass().add("dragging");
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(res.getName()); // put resource name as drag payload
            db.setContent(content);
            e.consume();
        });

        card.setOnDragDone((DragEvent e) -> {
            draggedResource = null;
            card.getStyleClass().remove("dragging");
            e.consume();
        });

        return card;
    }

    // ── Drop target (the activity list) ───────────────────────────────────────

    private void populateDropTarget() {
        dropTargetList.setItems(store.getActivities());
        dropTargetList.setCellFactory(lv -> new ActivityCell());
        dropTargetList.setPlaceholder(Theme.emptyState("No activities yet", "Create an activity first, then assign resources to it."));

        // Accept drag over the list
        dropTargetList.setOnDragOver((DragEvent e) -> {
            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                if (!dropTargetList.getStyleClass().contains("drop-active")) dropTargetList.getStyleClass().add("drop-active");
            }
            e.consume();
        });

        dropTargetList.setOnDragExited((DragEvent e) -> {
            dropTargetList.getStyleClass().remove("drop-active");
            e.consume();
        });

        // Handle the drop
        dropTargetList.setOnDragDropped((DragEvent e) -> {
            dropTargetList.getStyleClass().remove("drop-active");
            Activity target = dropTargetList.getSelectionModel().getSelectedItem();

            if (draggedResource == null) {
                e.setDropCompleted(false);
            } else if (target == null) {
                showOverlay("Select an activity first",
                        "Click an activity in the list, then drag the resource card onto it.");
                e.setDropCompleted(false);
            } else if ("Cancelled".equals(target.getStatus())) {
                showOverlay("Activity is cancelled", "Resources can't be assigned to a cancelled activity.");
                e.setDropCompleted(false);
            } else if (target.getDate() == null || target.getTimeSlot() == null) {
                // Without a date + slot there is nothing to check for conflicts against
                showOverlay("Missing date or time slot",
                        "\"" + target.getTitle() + "\" needs a date and a time slot before resources can be assigned. Edit the activity first.");
                e.setDropCompleted(false);
            } else if (store.hasConflict(draggedResource, target.getDate(), target.getTimeSlot())) {
                showOverlay("⚠  Scheduling Conflict Detected",
                    "\"" + draggedResource.getName() + "\" is already booked for " +
                    target.getDate() + " at a time overlapping " + target.getTimeSlot() + ".");
                e.setDropCompleted(false);
            } else {
                // All clear — create the booking
                store.getBookings().add(new Booking(draggedResource, target, target.getDate(), target.getTimeSlot()));
                buildResourceCards(); // refresh cards to show new status
                e.setDropCompleted(true);
            }
            e.consume();
        });
    }

    // ── Overlay ───────────────────────────────────────────────────────────────

    private void showOverlay(String title, String detail) {
        conflictTitleLabel.setText(title);
        conflictDetailLabel.setText(detail);
        conflictOverlay.setVisible(true);
        conflictOverlay.setManaged(true);
    }

    private void hideOverlay() {
        conflictOverlay.setVisible(false);
        conflictOverlay.setManaged(false);
    }

    @FXML
    private void handleConflictCancel() {
        hideOverlay();
    }
}
