package data;

public class EventFirebase {
    public EventFirebase(int id, String type, String description, String location, String district, String severity, String date, int confirmations, int rejections, String user, int userId, String imageUrl) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.location = location;
        this.district = district;
        this.severity = severity;
        this.date = date;
        this.confirmations = confirmations;
        this.rejections = rejections;
        this.user = user;
        this.userId = userId;
        this.imageUrl = imageUrl;
    }
    public EventFirebase() {
        // Default constructor required for Firebase Firestore deserialization
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public int getRejections() {
        return rejections;
    }

    public void setRejections(int rejections) {
        this.rejections = rejections;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private int id;
    private String type;
    private String description;
    private String location;
    private String district;
    private String severity;
    private String date;
    private int confirmations;
    private int rejections;
    private String user;
    private int userId;
    private String imageUrl; // Added field for the image URL

    // Constructors, getters, and setters...
}
