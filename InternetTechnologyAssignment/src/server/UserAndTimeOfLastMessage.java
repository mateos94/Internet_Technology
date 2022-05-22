package server;

/**
 * This class is an object-class, it represents an object that contains user and last time of user speaking in a chatting group
 *
 * @author Mateos and Haoshuang
 */
public class UserAndTimeOfLastMessage {
    private User user;
    private Long timestampOfLastMessage;

    public UserAndTimeOfLastMessage(User user, Long timestampOfLastMessage) {
        this.user = user;
        this.timestampOfLastMessage = timestampOfLastMessage;
    }

    /**
     * Getter of user
     * @return user
     */
    public User getUser() {
        return user;
    }

    /**
     * Getter of timestampOfLastMessage
     * @return timestamp of last activity of user
     */
    public Long getTimestampOfLastMessage() {
        return timestampOfLastMessage;
    }

    /**
     * Setter of timestampOfLastMessage
     * @param timestampOfLastMessage timestamp of last activity of user
     */
    public void setTimestampOfLastMessage(Long timestampOfLastMessage) {
        this.timestampOfLastMessage = timestampOfLastMessage;
    }
}
