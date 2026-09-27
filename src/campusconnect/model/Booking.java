package campusconnect.model;

import java.time.LocalDate;

public class Booking {

    private Resource resource;
    private Activity activity;
    private LocalDate date;
    private String timeSlot;

    public Booking() {}

    public Booking(Resource resource, Activity activity, LocalDate date, String timeSlot) {
        this.resource = resource;
        this.activity = activity;
        this.date = date;
        this.timeSlot = timeSlot;
    }

    // Check if this booking clashes with another date + slot + resource combo
    public boolean conflictsWith(Resource res, LocalDate d, String slot) {
        return this.resource.getName().equals(res.getName())
                && this.date.equals(d)
                && this.timeSlot.equals(slot);
    }

    // --- Getters and Setters ---

    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
}
