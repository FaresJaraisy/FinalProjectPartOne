package data;

public class UserEventCount {
    private String username;
    private int eventCount;

    public UserEventCount(String username, int eventCount) {
        this.username = username;
        this.eventCount = eventCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public int getEventCount() {
        return eventCount;
    }
}