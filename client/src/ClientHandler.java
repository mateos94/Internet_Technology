import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

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
            listOfGroupsAsString = "*There is no group.";
        } else {
            listOfGroupsAsString = "*Here are the list of groups:";
            listOfGroupsAsString += "\n";
            for (Group nextGroup: groups) {
                listOfGroupsAsString += nextGroup.getGroupName();
                listOfGroupsAsString += "\n";
            }
            listOfGroupsAsString += "*End.";
        }
        return listOfGroupsAsString;
    }

    public void disbandGroup(String groupName) {
        groups.removeIf(nextGroup -> nextGroup.getGroupName().equals(groupName));
    }

    public String parseMessage (String string) {
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
                    "Login <username>: a command used to login to the chat server or create a new user if <username> isn’t registered before. \n" +
                    "Request users: a command to request all users from the server that are currently online. \n" +
                    "Broadcast <message>: a command to broadcast a message to all connected(online) users. \n" +
                    "Quit server: a command to log out from the server. \n" +
                    "Join <group name>: a command to join a group that exists. \n" +
                    "Leave <group name>: a command to leave a group you are in. \n" +
                    "Create <group name>: a command to create a new group. \n" +
                    "Kick <username to be kicked> <group name>: a command to kick a user from your group (you have to \n" +
                    "be the admin/creator of that group) \n" +
                    "Private <username to message> <message>: a command to send a private message to another user. \n" +
                    "Group <group name> <message>: a command to send a message to all members of your group. \n" +
                    "Send < receiver’s username> <file name>: a command to send a file to another user. \n" +
                    "Pong: extend the duration of your connection";
            return responseMessage;
        } else if (restOfMessage.equalsIgnoreCase("Groups")) {
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else {
                responseMessage = getGroups();
            }
            return responseMessage;
        }
        int x = restOfMessage.indexOf(' ');
        //if the restOfMessage was not defined ask used to enter the full command
        if(x<0) {
            responseMessage = "Please enter the full command";
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
                            responseMessage = "*Logging in failed, this user already exists.";
                            userExist = true;
                        }
                    }
                }
                if (!userExist) {
                    User user = new User(contentOfMessage);
                    users.add(user);
                    responseMessage = "*You are logged in with username " + contentOfMessage + ".";
                    setClientName(contentOfMessage);
                }
            } else {
                responseMessage = "*You are already logged in.";
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
                responseMessage = "*There is no user logged in so far.";
            } else {
                responseMessage = "List of users: \n";
                for (User nextUser : users) {
                    if (nextUser.isLoggedIn()) {
                        responseMessage += (nextUser.getUserName() + " \n");
                    }
                }
            }
        }
        else if (typeOfMessage.equalsIgnoreCase("Broadcast")) {
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else {
                responseMessage = "*<" + senderName + "> " + restOfMessage;
                outToAllLoggedIn(responseMessage);
                responseMessage = "Your message has been broadcasted";
            }
        } else if (typeOfMessage.equalsIgnoreCase("Quit")) {
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else {
                responseMessage = "*You are logged out.";
                changeLoginStatus(senderName);
            }
        } else if (typeOfMessage.equalsIgnoreCase("Join")) {
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else if (getGroupByName(contentOfMessage) == null){
                responseMessage = "*Such group does not exist.";
            } else if(getGroupByName(contentOfMessage).addMember(getUserByName(senderName))){
                outToAllLoggedIn("*User " + senderName + " joint group " + contentOfMessage + ".");
                responseMessage = "*You joined group " + contentOfMessage;
            } else{
               responseMessage = "Failed to join the group";
            }
        } else if (typeOfMessage.equalsIgnoreCase("Leave")) {
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else {
                if (getGroupByName(contentOfMessage) == null) {
                    responseMessage = "*Such group does not exist.";
                } else if (getGroupByName(contentOfMessage).checkIfUserExist(senderName)) {
                    getGroupByName(contentOfMessage).deleteMemberByName(senderName);
                    responseMessage = "*You left group " + contentOfMessage;
                    if (getGroupByName(contentOfMessage).getOwner().getUserName().equals(senderName)) {
                        disbandGroup(contentOfMessage);
                    }
                } else {
                    responseMessage = "*You cannot leave a group that you have not joint yet.";
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("Create")) {
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else {
                if (checkIfGroupExist(contentOfMessage)) {
                    responseMessage = "*This group already exist.";
                } else {
                    Group group = new Group(contentOfMessage, getUserByName(senderName));
                    group.addMember(getUserByName(senderName));
                    groups.add(group);
                    outToAllLoggedIn("*User " + senderName + " created group " + contentOfMessage + ".");
                    responseMessage = "*Group created.";
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("Kick")) {
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String groupName = contentOfMessage.substring(0, j);
                String kickedUsername = contentOfMessage.substring(j + 1);
                if (!checkIfGroupExist(groupName)) {
                    responseMessage = "*This group does not exist.";
                } else {
                    if (!getGroupByName(groupName).getOwner().getUserName().equals(senderName)) {
                        responseMessage = "*You are not the owner of group.";
                    } else {
                        if (!getGroupByName(groupName).checkIfUserExist(kickedUsername)) {
                            responseMessage = "*The user does not exist in the group.";
                        } else {
                            getGroupByName(groupName).deleteMemberByName(kickedUsername);
                            responseMessage = "The user " + kickedUsername +" was kicked from the group";
                        }
                    }
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("Private")) {
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String receiverName = contentOfMessage.substring(0, j);
                String messageToReceiver = contentOfMessage.substring(j + 1);
                if (!checkIfUserExist(receiverName)) {
                    responseMessage = "*User does not exist.";
                } else {
                    responseMessage = "*<" + senderName + ">" + messageToReceiver;
                    outToPrivate(responseMessage,receiverName);
                    responseMessage = "Your message has been sent to " + receiverName + ".";
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("Group")) {
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String groupName = contentOfMessage.substring(0, j);
                String messageToGroup = contentOfMessage.substring(j + 1);
                if (!checkIfGroupExist(groupName)) {
                    responseMessage = "*This group does not exist.";
                } else if (getGroupByName(groupName).checkIfUserExist(senderName)) {
                    responseMessage = "*<" + groupName + ">" + "<" + senderName + "> " + messageToGroup;
                    outToGroup(responseMessage, groupName);
                    getGroupByName(groupName).addHistoryMessage(responseMessage);
                    responseMessage = "Your message has been sent to the group.";
                } else {
                    responseMessage = "*You cannot send message to a group that you have not joint.";
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("History")) {
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else {
                if (!checkIfGroupExist(contentOfMessage)) {
                    responseMessage = "*This group does not exist.";
                } else if (getGroupByName(contentOfMessage).checkIfUserExist(senderName)) {
                    responseMessage = getGroupByName(contentOfMessage).getHistoryMessages();
                } else {
                    responseMessage = "*You cannot get chatting history of a group that you did not join.";
                }
            }
        } else if (typeOfMessage.equalsIgnoreCase("Send")){
            if (senderName.equals("0")) {
                responseMessage = "*You need to login first.";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String receiverName = contentOfMessage.substring(0, j);
                if (!checkIfUserExist(receiverName)) {
                    responseMessage = "*User does not exist.";
                } else {
                    try {
                        receiveFile(receiverName);
                        responseMessage = "*Your file has been sent successfully";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        else {
            responseMessage = "*It does not match any function.";
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

}

