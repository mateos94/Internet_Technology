package server;

public class User {
    private String userName;
    private String password = null;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public boolean userIsAuthenticated() {
        return password != null;
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
