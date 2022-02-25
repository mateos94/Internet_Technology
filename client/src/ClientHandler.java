import java.io.*;
import java.net.Socket;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClientHandler implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String responseMessage = "";
    public static String receiveMessage;
    public static ArrayList<User> users = new ArrayList<>();
    private static ArrayList<Group> groups = new ArrayList<>();
    private static ArrayList<ClientHandler> clients;
    private String clientName;
    private int counter;

    public int getCounter() {
        return counter;
    }

    public void setCounter() {
        counter++;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public Socket getClient() {
        return client;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public ClientHandler getByClientName(String clientName) {
        for (ClientHandler nextClient : clients) {
            if (nextClient.getClientName().equals(clientName)) {
                return nextClient;
            }
        }
        return null;
    }

    public ClientHandler(Socket clientSocket, ArrayList<ClientHandler> clients) throws IOException {
        this.client = clientSocket;
        this.clients = clients;
        this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out = new PrintWriter(client.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
        while (true) {
            receiveMessage = AES.decrypt(in.readLine());
            out.println(AES.encrypt(parseMessage(receiveMessage)));
            out.flush();
            responseMessage = "";
    }
        } catch (IOException e) {
            if (clientName != null) {
                changeLoginStatus(clientName);
            }
            System.err.println("IO exception in client handler");
            return;
        }finally {
            out.close();
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}

    User getUserByName(String name) {
        for (User nextUser : users) {
            if (nextUser.getUserName().equals(name)) {
                return nextUser;
            }
        }
        return null;
    }

    Group getGroupByName(String name) {
        for (Group nextGroup : groups) {
            if (nextGroup.getGroupName().equals(name)) {
                return nextGroup;
            }
        }
        return null;
    }

    boolean checkIfUserExist(String name) {
        for (User nextUser : users) {
            if (nextUser.getUserName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    boolean checkIfGroupExist(String name) {
        for (Group nextGroup : groups) {
            if (nextGroup.getGroupName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String getGroups() {
        String listOfGroupsAsString = "";
        if (groups.size() == 0) {
            listOfGroupsAsString = "#There is no group.";
        } else {
            listOfGroupsAsString = "#Here are the list of groups:";
            listOfGroupsAsString += "\n";
            for (Group nextGroup: groups) {
                listOfGroupsAsString += nextGroup.getGroupName();
                listOfGroupsAsString += "\n";
            }
            listOfGroupsAsString += "#End.";
        }
        return listOfGroupsAsString;
    }

    public void disbandGroup(String groupName) {
        groups.removeIf(nextGroup -> nextGroup.getGroupName().equals(groupName));
    }

    public String parseMessage (String string) {
        String currentTimeAsDateString = convertTimestampToDate(System.currentTimeMillis());
        //if user responds with Pong extend session.
        if (string.contains("Pong")){
            responseMessage = "Connection duration extended";
            counter = 0;
            return responseMessage;
        }
        int i = string.indexOf(' ');
        String senderName = string.substring(0, i);
        String restOfMessage = string.substring(i + 1);
        if (restOfMessage.equals("?")) {
            responseMessage =
                    "# Help list:\n" +
                    "Everything starts with # means message from system. \n" +
                    "Login <username>: Login to the chat server as a guest if <username> isn’t registered before. \n" +
                    "Signup <username> <password>: Register at server if <username> isn't registered before. \n" +
                    "Request users: Request all users from the server that are currently online. \n" +
                    "Broadcast <message>: Broadcast a message to all connected(online) users. \n" +
                    "Quit server: Log out from the server. \n" +
                    "# -----Group related-----: \n" +
                    "Groups: Get a list of all groups. \n" +
                    "History: Get chat history of a group. \n" +
                    "Join <group name>: Join a group that exists. \n" +
                    "Leave <group name>: Leave a group that you are in. \n" +
                    "Create <group name>: Create a new group. \n" +
                    "Kick <username to be kicked> <group name>: Kick a user from your group (you have to be the admin/creator of that group) \n" +
                    "Group <group name> <message>: Send a message to all members of your group. \n" +
                    "# -----Send related-----: \n" +
                    "Private <username to message> <message>: Send a private message to another user. \n" +
                    "Send < receiver’s username> <file name>: Send a file to another user. \n" +
                    "Pong: extend the duration of your connection";
            return responseMessage;
        } else if (restOfMessage.equalsIgnoreCase("Groups")) {
            if (senderName.equals("0")) {
                responseMessage = "#You need to login first.";
            } else {
                responseMessage = getGroups();
            }
            return responseMessage;
        }
        int x = restOfMessage.indexOf(' ');
        if(x<0) {
            responseMessage = "#Please enter the full command";
            return responseMessage;
        }
        String typeOfMessage = restOfMessage.substring(0, x);
        String contentOfMessage = restOfMessage.substring(x + 1);
        if (typeOfMessage.equalsIgnoreCase("Login")) {
            if (senderName.equals("0")) {
                boolean userExist = false;
                if (!users.isEmpty()) {
                    for (User nextUser : users) {
                        if (nextUser.getUserName().equals(contentOfMessage) && nextUser.isLoggedIn()) {
                            responseMessage = "# " + currentTimeAsDateString + " Logging in failed, this user already exists.";
                            userExist = true;
                        }
                    }
                }
                if (!userExist) {
                    User user = new User(contentOfMessage);
                    users.add(user);
                    responseMessage = "# " + currentTimeAsDateString + " You are logged in with username " + contentOfMessage + ".";
                    setClientName(contentOfMessage);
                }
            } else {
                responseMessage = "# " + currentTimeAsDateString + " You are already logged in.";
            }
        } else if (typeOfMessage.equalsIgnoreCase("Signup")) {
            if (senderName.equals("0")) {
                if (!contentOfMessage.contains(" ")) {
                    responseMessage = "# " + currentTimeAsDateString + " You need a password.";
                } else {
                    boolean userExist = false;
                    int j = contentOfMessage.indexOf(' ');
                    String username = contentOfMessage.substring(0, j);
                    String password = contentOfMessage.substring(j + 1);
                    if (!users.isEmpty()) {
                        for (User nextUser : users) {
                            if (nextUser.getUserName().equals(username) && nextUser.isLoggedIn()) {
                                responseMessage = "# " + currentTimeAsDateString + " Cannot sign up, this user already exists.";
                                userExist = true;
                            }
                        }
                    }
                    if (!userExist) {
                        User user = new User(username);
                        user.setPassword(password);
                        users.add(user);
                        responseMessage = "# " + currentTimeAsDateString + " You are logged in with authenticated user, with username of " + username + ".";
                        setClientName(username);
                    }
                }
            } else {
                responseMessage = "# " + currentTimeAsDateString + " You are already logged in.";
            }
        } else if (typeOfMessage.equalsIgnoreCase("Request")) {
            boolean anyUserLoggedIn = false;
            if (!users.isEmpty()) {
                for (User nextUser : users) {
                    if (nextUser.isLoggedIn()) {
                        anyUserLoggedIn = true;
                    }
                }
            }
            if (!anyUserLoggedIn) {
                responseMessage = "# " + currentTimeAsDateString + " There is no user logged in so far.";
            } else {
                responseMessage = "# " + currentTimeAsDateString + " List of users: \n";
                for (User nextUser : users) {
                    if (nextUser.isLoggedIn()) {
                        responseMessage += (nextUser.getUserName() + " \n");
                    }
                }
            }
        }
        else if (typeOfMessage.equalsIgnoreCase("Broadcast")) {
            if (senderName.equals("0")) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else {
                responseMessage = "# " + currentTimeAsDateString + " <" + senderName + "> " + restOfMessage;
                outToAllLoggedIn(responseMessage);
                responseMessage = "# " + currentTimeAsDateString + "Your message has been broadcasted";
            }
        } else if (typeOfMessage.equalsIgnoreCase("Quit")) {
            if (senderName.equals("0")) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else {
                responseMessage = "# " + currentTimeAsDateString + " You are logged out.";
                changeLoginStatus(senderName);
            }
        } else if (typeOfMessage.equalsIgnoreCase("Join")) {
            if (senderName.equals("0")) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else if (getGroupByName(contentOfMessage) == null){
                responseMessage = "# " + currentTimeAsDateString + " Such group does not exist.";
            } else if(getGroupByName(contentOfMessage).addMember(getUserByName(senderName), System.currentTimeMillis())){
                outToAllLoggedIn("# " + currentTimeAsDateString + " User " + senderName + " joint group " + contentOfMessage + ".");
                responseMessage = "# " + currentTimeAsDateString + " You joined group " + contentOfMessage;
            } else{
               responseMessage = "# " + currentTimeAsDateString + " Failed to join the group";
            }
        } else if (typeOfMessage.equalsIgnoreCase("Leave")) {
            if (senderName.equals("0")) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else {
                if (getGroupByName(contentOfMessage) == null) {
                    responseMessage = "# " + currentTimeAsDateString + " Such group does not exist.";
                } else if (getGroupByName(contentOfMessage).checkIfUserExist(senderName)) {
                    getGroupByName(contentOfMessage).deleteMemberByName(senderName);
                    responseMessage = "# " + currentTimeAsDateString + " You left group " + contentOfMessage;
                    if (getGroupByName(contentOfMessage).getOwner().getUserName().equals(senderName)) {
                        disbandGroup(contentOfMessage);
                    }
                } else {
                    responseMessage = "# " + currentTimeAsDateString + " You cannot leave a group that you have not joint yet.";
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("Create")) {
            if (senderName.equals("0")) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else {
                if (checkIfGroupExist(contentOfMessage)) {
                    responseMessage = "# " + currentTimeAsDateString + " This group already exist.";
                } else {
                    Group group = new Group(contentOfMessage, getUserByName(senderName));
                    group.addMember(getUserByName(senderName), System.currentTimeMillis());
                    groups.add(group);
                    outToAllLoggedIn("# " + currentTimeAsDateString + " User " + senderName + " created group " + contentOfMessage + ".");
                    responseMessage = "# " + currentTimeAsDateString +" Group created.";
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("Kick")) {
            if (senderName.equals("0")) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String groupName = contentOfMessage.substring(0, j);
                String kickedUsername = contentOfMessage.substring(j + 1);
                if (!checkIfGroupExist(groupName)) {
                    responseMessage = "# " + currentTimeAsDateString + " This group does not exist.";
                } else {
                    if (!getGroupByName(groupName).getOwner().getUserName().equals(senderName)) {
                        responseMessage = "# " + currentTimeAsDateString + " You are not the owner of group.";
                    } else {
                        if (!getGroupByName(groupName).checkIfUserExist(kickedUsername)) {
                            responseMessage = "# " + currentTimeAsDateString + " The user does not exist in the group.";
                        } else {
                            getGroupByName(groupName).deleteMemberByName(kickedUsername);
                            responseMessage = "# " + currentTimeAsDateString + " The user " + kickedUsername +" was kicked from the group";
                        }
                    }
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("Private")) {
            if (senderName.equals("0")) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String receiverName = contentOfMessage.substring(0, j);
                String messageToReceiver = contentOfMessage.substring(j + 1);
                if (senderName.equals(receiverName)) {
                    responseMessage = "# " + currentTimeAsDateString + " You cannot send message to yourself.";
                } else if (!checkIfUserExist(receiverName)) {
                    responseMessage = "#" + currentTimeAsDateString + " User does not exist.";
                } else {
                    if (!getUserByName(receiverName).isLoggedIn()) {
                        responseMessage = "# " + currentTimeAsDateString + " Target user is not online now.";
                    } else {
                        responseMessage = "# " + currentTimeAsDateString + " <";
                        if (getUserByName(senderName).userIsAuthenticated()) {
                            responseMessage += "*";
                        }
                        responseMessage += (senderName + ">: " + messageToReceiver);
                        outToPrivate(responseMessage,receiverName);
                        responseMessage = "# " + currentTimeAsDateString + " Your message has been sent to " + receiverName + ".";
                    }
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("Group")) {
            if (senderName.equals("0")) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String groupName = contentOfMessage.substring(0, j);
                String messageToGroup = contentOfMessage.substring(j + 1);
                if (!checkIfGroupExist(groupName)) {
                    responseMessage = "# " + currentTimeAsDateString + " This group does not exist.";
                } else if (getGroupByName(groupName).checkIfUserExist(senderName)) {
                    responseMessage = "# " + currentTimeAsDateString + " <" + groupName + ">" + "<";
                    if (getUserByName(senderName).userIsAuthenticated()) {
                        responseMessage += "*";
                    }
                    responseMessage += (senderName + "> " + messageToGroup);
                    outToGroup(responseMessage, groupName);
                    getGroupByName(groupName).addHistoryMessage(responseMessage);
                    getGroupByName(groupName).updateTimeOfLastMessage(senderName, System.currentTimeMillis());
                    responseMessage = "# " + currentTimeAsDateString + " Your message has been sent to the group.";
                } else {
                    responseMessage = "# " + currentTimeAsDateString + " You cannot send message to a group that you have not joint.";
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("History")) {
            if (senderName.equals("0")) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else {
                if (!checkIfGroupExist(contentOfMessage)) {
                    responseMessage = "# " + currentTimeAsDateString + " This group does not exist.";
                } else if (getGroupByName(contentOfMessage).checkIfUserExist(senderName)) {
                    responseMessage = getGroupByName(contentOfMessage).getHistoryMessages();
                } else {
                    responseMessage = "# " + currentTimeAsDateString + " You cannot get chatting history of a group that you did not join.";
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("Send")){
            if (senderName.equals("0")) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String receiverName = contentOfMessage.substring(0, j);
                if (!checkIfUserExist(receiverName)) {
                    responseMessage = "# " + currentTimeAsDateString + " User does not exist.";
                } else {
                    try {
                        receiveFile(receiverName);
                        responseMessage = "# " + currentTimeAsDateString + " Your file has been sent successfully";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        else {
            responseMessage = "# " + currentTimeAsDateString + " It does not match any function.";
        }
        counter = 0;
        return responseMessage;

    }

    private void outToAll(String responseMessage) {
        for( ClientHandler clientHandlers : clients){
            clientHandlers.out.println(AES.encrypt(responseMessage));
        }
    }

    private void outToAllLoggedIn(String responseMessage) {
        for( ClientHandler clientHandlers : clients){
            if (clientHandlers.getClientName() != null){
                clientHandlers.out.println(AES.encrypt(responseMessage));
            }
        }
    }

    private void outToPrivate(String responseMessage, String name){
        for( ClientHandler clientHandlers : clients){
            if (getByClientName(name).equals(clientHandlers)) {
                clientHandlers.out.println(AES.encrypt(responseMessage));
            }
        }
    }

    private void outToGroup(String responseMessage, String groupName){
        for( ClientHandler clientHandlers : clients){
            for (Group group : groups){
                if(group.checkIfUserExist(clientHandlers.getClientName()) && groupName.equals(group.getGroupName())){
                    clientHandlers.out.println(AES.encrypt(responseMessage));
                }
            }
        }
    }

    public void changeLoginStatus(String senderName){
        for (User nextUser : users) {
            if (nextUser.getUserName().equals(senderName)) {
                nextUser.changeLoginStatus();
            }
        }
    }

    private void receiveFile(String name) throws IOException {
        byte b[] = new byte[2024];
        InputStream inputStream = client.getInputStream();
        FileOutputStream fileOutputStream = new FileOutputStream("server.txt");
        inputStream.read(b,0,b.length);
        fileOutputStream.write(b,0,b.length);
        for( ClientHandler clientHandlers : clients){
            if (getByClientName(name).equals(clientHandlers)) {
                clientHandlers.out.println(AES.encrypt("You received a new file"));
                FileSender fileSender = new FileSender(clientHandlers.getClient(),"server.txt");
                new Thread(fileSender).start();
            }
        }
    }

    private String convertTimestampToDate(long timestamp){

        Date date = new Date(timestamp);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }

    private void kickPeopleWhoAreNotChattingMoreThanTwoMinutesInGroups() {
        if (!groups.isEmpty()){
            for (Group nextGroup: groups){
                for (UserAndTimeOfLastMessage nextUserAndTimeOfLastMessage: nextGroup.getMembersAndTimeOfLastMessage()){
                    if (System.currentTimeMillis() - nextUserAndTimeOfLastMessage.getTimestampOfLastMessage() > 120000){
                        nextGroup.deleteMemberByName(nextUserAndTimeOfLastMessage.getUser().getUserName());
                        String message = "";
                        message += "# Because of " + nextUserAndTimeOfLastMessage.getUser().getUserName() + "did not talk for more than 2 mins in group " + nextGroup.getGroupName() + ", got kicked out of group.";
                        outToAll(message);
                    }
                }
            }
        }
    }

}

