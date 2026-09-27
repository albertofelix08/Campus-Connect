package campusconnect.model;

public class Resource {

    // Could be a hall, projector, mic set, banner, etc.
    private String name;
    private String type;        // Physical or Human
    private String subType;     // Hall / Equipment / Volunteer / Faculty
    private boolean available;

    // If it's a human resource
    private String contactInfo;

    // TODO: add booking history to check conflicts

    public Resource() {
        this.available = true;
    }

    public Resource(String name, String type, String subType) {
        this();
        this.name = name;
        this.type = type;
        this.subType = subType;
    }

    // --- Getters and Setters ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    @Override
    public String toString() {
        return name + " (" + subType + ")";
    }
}
