package campusconnect.store;

import campusconnect.model.Activity;
import campusconnect.model.Booking;
import campusconnect.model.Resource;
import campusconnect.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

// Acts as the in-memory "database" for the whole app
// All controllers pull from here
public class DataStore {

    // Singleton so every controller shares the same data
    private static DataStore instance;

    private ObservableList<Activity> activities;
    private ObservableList<Resource> resources;
    private ObservableList<User> users;
    private ObservableList<Booking> bookings;

    // Track who is logged in right now
    private User currentUser;

    private DataStore() {
        activities = FXCollections.observableArrayList();
        resources  = FXCollections.observableArrayList();
        users      = FXCollections.observableArrayList();
        bookings   = FXCollections.observableArrayList();

        loadSampleData();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // --- Sample data so screens aren't empty on first run ---
    private void loadSampleData() {

        // Users
        users.add(new User("admin",      "admin123",  "Admin"));
        users.add(new User("drpriya",    "pass123",   "Coordinator"));
        users.add(new User("volunteer1", "vol123",    "Volunteer"));

        // Activities
        Activity a1 = new Activity("Annual Technical Conference", "Co-curricular", LocalDate.now().plusDays(10));
        a1.setVenue("Main Auditorium");
        a1.setCoordinator("Dr. Priya");
        a1.setExpectedParticipants(200);
        a1.setStatus("Approved");

        Activity a2 = new Activity("Python Workshop", "Academic", LocalDate.now().plusDays(5));
        a2.setVenue("Seminar Hall B");
        a2.setCoordinator("Prof. Ramesh");
        a2.setExpectedParticipants(60);
        a2.setStatus("Proposed");

        Activity a3 = new Activity("Cultural Fest 2024", "Extra-curricular", LocalDate.now().plusDays(20));
        a3.setVenue("Open Air Theatre");
        a3.setCoordinator("Student Council");
        a3.setExpectedParticipants(500);
        a3.setStatus("Proposed");

        activities.addAll(a1, a2, a3);

        // Resources
        resources.add(new Resource("Main Auditorium",   "Physical", "Hall"));
        resources.add(new Resource("Seminar Hall A",    "Physical", "Hall"));
        resources.add(new Resource("Seminar Hall B",    "Physical", "Hall"));
        resources.add(new Resource("Open Air Theatre",  "Physical", "Hall"));
        resources.add(new Resource("Projector - P01",   "Physical", "Equipment"));
        resources.add(new Resource("Projector - P02",   "Physical", "Equipment"));
        resources.add(new Resource("Mic Set - M01",     "Physical", "Equipment"));
        resources.add(new Resource("Mic Set - M02",     "Physical", "Equipment"));
        resources.add(new Resource("Dr. Priya",         "Human",    "Coordinator"));
        resources.add(new Resource("Prof. Ramesh",      "Human",    "Coordinator"));
        resources.add(new Resource("Ravi (Volunteer)",  "Human",    "Volunteer"));
        resources.add(new Resource("Meena (Volunteer)", "Human",    "Volunteer"));
    }

    // --- Conflict check helper ---
    public boolean hasConflict(Resource res, LocalDate date, String slot) {
        for (Booking b : bookings) {
            if (b.conflictsWith(res, date, slot)) return true;
        }
        return false;
    }

    // --- Getters ---

    public ObservableList<Activity> getActivities() { return activities; }
    public ObservableList<Resource> getResources()  { return resources; }
    public ObservableList<User> getUsers()          { return users; }
    public ObservableList<Booking> getBookings()    { return bookings; }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    // Simple login check
    public User login(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equals(username) && u.checkPassword(password)) {
                currentUser = u;
                return u;
            }
        }
        return null; // login failed
    }
}
