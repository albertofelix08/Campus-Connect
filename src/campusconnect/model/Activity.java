package campusconnect.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Activity {

    // Basic info
    private String title;
    private String description;
    private String category;       // Academic, Co-curricular, etc.
    private String status;         // Proposed, Approved, Ongoing, Completed, Cancelled
    private LocalDate date;
    private String timeSlot;
    private String venue;
    private String coordinator;
    private int expectedParticipants;
    private int actualParticipants;
    private String remarks;

    // Facilities checked (projector, mic, wifi, etc.)
    private List<String> facilities;

    // TODO: link to assigned resources later

    public Activity() {
        this.status = "Proposed";
        this.facilities = new ArrayList<>();
    }

    public Activity(String title, String category, LocalDate date) {
        this();
        this.title = title;
        this.category = category;
        this.date = date;
    }

    // --- Getters and Setters ---

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getCoordinator() { return coordinator; }
    public void setCoordinator(String coordinator) { this.coordinator = coordinator; }

    public int getExpectedParticipants() { return expectedParticipants; }
    public void setExpectedParticipants(int expectedParticipants) { this.expectedParticipants = expectedParticipants; }

    public int getActualParticipants() { return actualParticipants; }
    public void setActualParticipants(int actualParticipants) { this.actualParticipants = actualParticipants; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public List<String> getFacilities() { return facilities; }
    public void setFacilities(List<String> facilities) { this.facilities = facilities; }

    @Override
    public String toString() {
        return title + " [" + category + "] - " + status;
    }
}
