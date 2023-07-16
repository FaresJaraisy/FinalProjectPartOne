package data;

public class EventToUserConfirmation {
    private int _ID;
    private int eventId;
    private int userId;

    public EventToUserConfirmation(int _ID, int eventId, int userId) {
        this._ID = _ID;
        this.eventId = eventId;
        this.userId = userId;
    }

    public int get_ID() {
        return _ID;
    }

    public int getEventId() {
        return eventId;
    }

    public int getUserId() {
        return userId;
    }
}
