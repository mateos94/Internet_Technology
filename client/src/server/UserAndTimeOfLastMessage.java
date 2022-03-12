package server;

public class UserAndTimeOfLastMessage {
    private User user;
    private Long timestampOfLastMessage;

    public UserAndTimeOfLastMessage(User user, Long timestampOfLastMessage) {
        this.user = user;
        this.timestampOfLastMessage = timestampOfLastMessage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getTimestampOfLastMessage() {
        return timestampOfLastMessage;
    }

    public void setTimestampOfLastMessage(Long timestampOfLastMessage) {
        this.timestampOfLastMessage = timestampOfLastMessage;
    }
}
