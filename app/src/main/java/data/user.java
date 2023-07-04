package data;

public class user {
    private int id;
    private String userName;
    private int confirmations;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;
}
