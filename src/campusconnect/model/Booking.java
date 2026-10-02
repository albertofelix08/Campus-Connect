package campusconnect.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

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

    // A booking stops counting once its activity has been cancelled
    public boolean isActive() {
        return activity == null || !"Cancelled".equals(activity.getStatus());
    }

    // Check if this booking clashes with another date + slot + resource combo.
    // Null-safe, ignores cancelled activities, and treats overlapping slots
    // (e.g. "09:00 - 13:00" vs "10:00 - 11:00") as a clash, not just identical ones.
    public boolean conflictsWith(Resource res, LocalDate d, String slot) {
        if (!isActive() || resource == null || res == null || date == null || d == null) return false;
        return Objects.equals(resource.getName(), res.getName())
                && date.equals(d)
                && slotsOverlap(timeSlot, slot);
    }

    // Slots look like "HH:mm - HH:mm". Overlap = each starts before the other ends.
    public static boolean slotsOverlap(String a, String b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;
        try {
            LocalTime[] x = parseSlot(a);
            LocalTime[] y = parseSlot(b);
            return x[0].isBefore(y[1]) && y[0].isBefore(x[1]);
        } catch (RuntimeException e) {
            return false; // unparseable slot text: fall back to "no overlap" (equal strings handled above)
        }
    }

    private static LocalTime[] parseSlot(String s) {
        String[] p = s.split("-");
        if (p.length != 2) throw new IllegalArgumentException("Bad slot: " + s);
        return new LocalTime[] { LocalTime.parse(p[0].trim()), LocalTime.parse(p[1].trim()) };
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
