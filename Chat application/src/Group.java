import java.util.ArrayList;

public class Group {
    private String groupName;
    private User owner;
    private ArrayList<User> members = new ArrayList<>();

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
        return members;
    }

    public void setMembers(ArrayList<User> members) {
        this.members = members;
    }

    public boolean addMember(User user){
        if (checkIfUserExist(user.getUserName())){
            return false;
        }else {
            members.add(user);
            return true;
        }
    }

    public void deleteMemberByName(String name){
        for (User user: members){
            if (user.getUserName().equals(name)){
                members.remove(user);
                return;
            }
        }
    }

    public User getMemberByName(String name){
        for (User user: members){
            if (user.getUserName().equals(name)){
                return user;
            }
        }
        return null;
    }

    public boolean checkIfUserExist(String name){
        for (User user: members){
            if (user.getUserName().equals(name)){
                return true;
            }
        }
        return false;
    }
}
