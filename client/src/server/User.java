package server;

public class User {
    private String userName;
    private String password = null;
    private boolean loggedIn = true;

    User(String userName) {
        this.userName = userName;
    }

    /**
     * Getter of username
     * @return Username of user
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Setter of password
     * @param password Password of user
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Check if user has a password
     * @return Boolean of weather user has a password
     */
    public boolean userIsAuthenticated() {
        return password != null;
    }

    /**
     * Check if user is logged in
     * @return Boolean of weather user is logged in
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Change boolean loggedin
     */
    public void changeLoginStatus() {
        if (loggedIn){
            loggedIn = false;
        }else {
            loggedIn = true;
        }
    }
}
