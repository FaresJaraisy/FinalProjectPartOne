package data;

import android.graphics.Bitmap;

import com.example.finalprojectpartone.UserProfile;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class Event {
    private int id;
    private String eventType;
    private Bitmap imgBitmap;
    private String description;
    private String location;
    private String district;
    private String severity;
    private Date eventDate;
    private int confirmations;
    private int rejections;
    private int userId;
    private UserProfile user;

    public Event(String eventType, Bitmap imgBitmap, String description, String location, String district, String severity, UserProfile user) {
        this.eventType = eventType;
        this.imgBitmap = imgBitmap;
        this.description = description;
        this.location = location;
        this.district = district;
        this.severity = severity;
        this.user = user;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte[] getBitmapAsByteArray() {
        if (imgBitmap == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imgBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    public int getRejections() {
        return rejections;
    }

    public void setRejections(int rejections) {
        this.rejections = rejections;
    }
}
