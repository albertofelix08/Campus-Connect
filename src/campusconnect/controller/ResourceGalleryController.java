package campusconnect.controller;

import campusconnect.model.Activity;
import campusconnect.model.Booking;
import campusconnect.model.Resource;
import campusconnect.store.DataStore;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ResourceGalleryController implements Initializable {

    @FXML private FlowPane resourceFlowPane;
    @FXML private StackPane conflictOverlay;
    @FXML private Label     conflictDetailLabel;
    @FXML private ListView<Activity> dropTargetList;

    private DataStore store = DataStore.getInstance();
    private Resource  draggedResource = null;  // track what's being dragged

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        conflictOverlay.setVisible(false);
        buildResourceCards();
        populateDropTarget();
    }

    // ── Build resource cards in the FlowPane ──────────────────────────────────

    private void buildResourceCards() {
        resourceFlowPane.getChildren().clear();

        for (Resource res : store.getResources()) {
            VBox card = createCard(res);
            resourceFlowPane.getChildren().add(card);
        }
    }

    private VBox createCard(Resource res) {
        VBox card = new VBox(6);
        card.setStyle(
            "-fx-background-color: " + cardColor(res.getSubType()) + ";" +
            "-fx-padding: 12;" +
            "-fx-border-color: #BDC3C7;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-min-width: 150;" +
            "-fx-max-width: 150;"
        );

        Label nameLabel    = new Label(res.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-wrap-text: true;");

        Label subTypeLabel = new Label(res.getSubType());
        subTypeLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11;");

        Label statusLabel  = new Label(res.isAvailable() ? "✓ Available" : "✗ Booked");
        statusLabel.setStyle("-fx-text-fill: " + (res.isAvailable() ? "#27AE60" : "#E74C3C") + ";");

        card.getChildren().addAll(nameLabel, subTypeLabel, statusLabel);

        // ── Drag source (MouseEvent: drag detected) ────────────────────────
        card.setOnDragDetected((MouseEvent e) -> {
            draggedResource = res;
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(res.getName()); // put resource name as drag payload
            db.setContent(content);
            e.consume();
        });

        card.setOnDragDone((DragEvent e) -> {
            draggedResource = null;
            e.consume();
        });

        return card;
    }

    // ── Drop target (the activity list) ───────────────────────────────────────

    private void populateDropTarget() {
        dropTargetList.setItems(store.getActivities());

        // Accept drag over the list
        dropTargetList.setOnDragOver((DragEvent e) -> {
            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });

        // Handle the drop
        dropTargetList.setOnDragDropped((DragEvent e) -> {
            Activity target = dropTargetList.getSelectionModel().getSelectedItem();
            if (target == null || draggedResource == null) {
                e.setDropCompleted(false);
                e.consume();
                return;
            }

            // Check for conflict before allocating
            if (store.hasConflict(draggedResource, target.getDate(), target.getTimeSlot())) {
                conflictDetailLabel.setText(
                    "\"" + draggedResource.getName() + "\" is already booked for " +
                    target.getDate() + " at " + target.getTimeSlot() + "."
                );
                conflictOverlay.setVisible(true);
                e.setDropCompleted(false);
            } else {
                // All clear — create the booking
                Booking booking = new Booking(draggedResource, target, target.getDate(), target.getTimeSlot());
                store.getBookings().add(booking);
                draggedResource.setAvailable(false);
                buildResourceCards(); // refresh cards to show new status
                e.setDropCompleted(true);
            }
            e.consume();
        });
    }

    // ── Conflict overlay buttons ───────────────────────────────────────────────

    @FXML
    private void handleConflictCancel() {
        conflictOverlay.setVisible(false);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String cardColor(String subType) {
        switch (subType) {
            case "Hall":       return "#EBF5FB";
            case "Equipment":  return "#FEF9E7";
            case "Coordinator": return "#EAFAF1";
            case "Volunteer":  return "#FDEDEC";
            default:           return "#F2F3F4";
        }
    }
}
