package server;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * This class is an object-class, it represents chatting group in the system
 *
 * @author Mateos and Haoshuang
 */
public class Group {
    private String groupName;
    private User owner;
    private ArrayList<UserAndTimeOfLastMessage> membersAndTimeOfLastMessage = new ArrayList<>();
    private ArrayList<String> historyMessages = new ArrayList<>();

    public Group(String groupName, User owner) {
        this.groupName = groupName;
        this.owner = owner;
    }

    /**
     * Getter of groupName
     * @return name of the group
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Getter of owner
     * @return user who created the group
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Getter of arraylist membersAndTimeOfLastMessage
     * @return A list of users and timestamp of their last activity
     */
    public ArrayList<UserAndTimeOfLastMessage> getMembersAndTimeOfLastMessage() {
        return membersAndTimeOfLastMessage;
    }

    public void setMembersAndTimeOfLastMessage(ArrayList<UserAndTimeOfLastMessage> membersAndTimeOfLastMessage) {
        this.membersAndTimeOfLastMessage = membersAndTimeOfLastMessage;
    }

    /**
     * Update the last activity time of user
     * @param username username of user
     * @param newTimestamp timestamp of newer activity of user
     */
    public void updateTimeOfLastMessage(String username, Long newTimestamp){
        for (UserAndTimeOfLastMessage nextUserAndTimeOfLastMessage: membersAndTimeOfLastMessage) {
            if (nextUserAndTimeOfLastMessage.getUser().getUserName().equals(username)){
                nextUserAndTimeOfLastMessage.setTimestampOfLastMessage(newTimestamp);
            }
        }
    }

    /**
     * Add a user to group
     * @param user new user
     * @param timestamp timestamp of user joining group
     * @return boolean of user added to group or not
     */
    public boolean addMember(User user, Long timestamp){
        if (checkIfUserExist(user.getUserName())){
            return false;
        }else {
            UserAndTimeOfLastMessage userAndTimeOfLastMessage = new UserAndTimeOfLastMessage(user, timestamp);
            membersAndTimeOfLastMessage.add(userAndTimeOfLastMessage);
            return true;
        }
    }

    /**
     * Delete a user in group by name of user
     * @param name name of user
     */

    public void deleteMemberByName(String name) throws ConcurrentModificationException {
        int indexOfTarget = 0;
        for (UserAndTimeOfLastMessage userAndTimeOfLastMessage: membersAndTimeOfLastMessage) {
            if (userAndTimeOfLastMessage.getUser().getUserName().equals(name)){
                indexOfTarget = membersAndTimeOfLastMessage.indexOf(userAndTimeOfLastMessage);
            }
        }
        membersAndTimeOfLastMessage.remove(indexOfTarget);
    }

    public boolean checkIfUserExist(String name){
        for (UserAndTimeOfLastMessage userAndTimeOfLastMessage: membersAndTimeOfLastMessage){
            if (userAndTimeOfLastMessage.getUser().getUserName().equals(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * Add a message to history of messages
     * @param historyMessage message that will be added
     */
    public void addHistoryMessage(String historyMessage) {
        historyMessages.add(historyMessage);
    }

    /**
     * Getter of history arraylist
     * @return historyMessages
     */
    public String getHistoryMessages() {
        String historyMessagesOfGroup = "";
        if (historyMessages.size() == 0) {
            historyMessagesOfGroup = "The group has no history message";
        } else {
            historyMessagesOfGroup = "Following are the history messages of group:";
            for (String historyMessage: historyMessages){
                historyMessagesOfGroup += "\n";
                historyMessagesOfGroup += historyMessage;
            }
            historyMessagesOfGroup += "\n";
            historyMessagesOfGroup += "End.";
        }
        return historyMessagesOfGroup;
    }
}
