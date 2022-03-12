package server;

import server.User;
import server.UserAndTimeOfLastMessage;

import java.util.ArrayList;

public class Group {
    private String groupName;
    private User owner;
    private static ArrayList<UserAndTimeOfLastMessage> membersAndTimeOfLastMessage = new ArrayList<>();
    private ArrayList<String> historyMessages = new ArrayList<>();

    public Group(String groupName, User owner) {
        this.groupName = groupName;
        this.owner = owner;
    }

    public String getGroupName() {
        return groupName;
    }

    public User getOwner() {
        return owner;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<User> getMembers() {
        ArrayList<User> members = new ArrayList<>();
        if (!membersAndTimeOfLastMessage.isEmpty()) {
            for (UserAndTimeOfLastMessage nextUserAndTimeOfLastMessage: membersAndTimeOfLastMessage) {
                members.add(nextUserAndTimeOfLastMessage.getUser());
            }
        }
        return members;
    }

    public ArrayList<UserAndTimeOfLastMessage> getMembersAndTimeOfLastMessage() {
        return membersAndTimeOfLastMessage;
    }

    public void setMembersAndTimeOfLastMessage(ArrayList<UserAndTimeOfLastMessage> membersAndTimeOfLastMessage) {
        this.membersAndTimeOfLastMessage = membersAndTimeOfLastMessage;
    }

    public void updateTimeOfLastMessage(String username, Long newTimestamp){
        for (UserAndTimeOfLastMessage nextUserAndTimeOfLastMessage: membersAndTimeOfLastMessage) {
            if (nextUserAndTimeOfLastMessage.getUser().getUserName().equals(username)){
                nextUserAndTimeOfLastMessage.setTimestampOfLastMessage(newTimestamp);
            }
        }
    }

    public boolean addMember(User user, Long timestamp){
        if (checkIfUserExist(user.getUserName())){
            return false;
        }else {
            UserAndTimeOfLastMessage userAndTimeOfLastMessage = new UserAndTimeOfLastMessage(user, timestamp);
            membersAndTimeOfLastMessage.add(userAndTimeOfLastMessage);
            return true;
        }
    }

    public void deleteMemberByName(String name){
        membersAndTimeOfLastMessage.removeIf(userAndTimeOfLastMessage -> userAndTimeOfLastMessage.getUser().getUserName().equals(name));
    }

    public User getMemberByName(String name){
        for (UserAndTimeOfLastMessage userAndTimeOfLastMessage: membersAndTimeOfLastMessage){
            if (userAndTimeOfLastMessage.getUser().getUserName().equals(name)){
                return userAndTimeOfLastMessage.getUser();
            }
        }
        return null;
    }

    public boolean checkIfUserExist(String name){
        for (UserAndTimeOfLastMessage userAndTimeOfLastMessage: membersAndTimeOfLastMessage){
            if (userAndTimeOfLastMessage.getUser().getUserName().equals(name)){
                return true;
            }
        }
        return false;
    }

    public void addHistoryMessage(String historyMessage) {
        historyMessages.add(historyMessage);
    }

    public String getHistoryMessages() {
        String historyMessagesOfGroup = "";
        if (historyMessages.size() == 0) {
            historyMessagesOfGroup = "*The group has no history message";
        } else {
            historyMessagesOfGroup = "*Following are the history messages of group:";
            for (String historyMessage: historyMessages){
                historyMessagesOfGroup += "\n";
                historyMessagesOfGroup += historyMessage;
            }
            historyMessagesOfGroup += "\n";
            historyMessagesOfGroup += "*End.";
        }
        return historyMessagesOfGroup;
    }
}
