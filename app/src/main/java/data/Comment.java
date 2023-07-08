package data;

public class Comment{
    private int id;
    private String content;
    private String username;
    private int eventId;

    public Comment(int id, String content, String username, int eventId){
        this.id = id;
        this.content = content;
        this.username = username;
        this.eventId = eventId;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public int getEventId() {
        return eventId;
    }
    public int getId() {
        return id;
    }
}