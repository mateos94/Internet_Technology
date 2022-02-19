public class User {
    private String userName;
    private boolean loggedIn = true;

    public User(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void changeLoginStatus() {
        if (loggedIn){
            loggedIn = false;
        }else {
            loggedIn = true;
        }
    }
}
