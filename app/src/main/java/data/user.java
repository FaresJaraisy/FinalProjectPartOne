package data;

public class user {
    private int id;
    private String userName;
    String userPassword;
    private int confirmations;

    public user(String userId, String userName, String userPassword, String confirmations, String rejections)
    {
        this.id = Integer.parseInt(userId);
        this.userName = userName;
        this.userPassword = userPassword;
        this.confirmations = Integer.parseInt(confirmations);
        this.rejections = Integer.parseInt(rejections);
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

    private int rejections;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


}
