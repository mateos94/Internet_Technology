package server;


import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles the messages from client
 *
 * @author Mateos and Haoshuang
 */
public class ClientHandler implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String responseMessage = "";
    public static String receiveMessage;
    public static ArrayList<User> users = new ArrayList<>();
    private static ArrayList<Group> groups = new ArrayList<>();
    private static ArrayList<ClientHandler> clients;
    private int counter;
    private User user;
    private String typeOfMessage;

    /**
     * Getter of pingpong counter
     * @return counter
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Increase pingpong counter by 1
     */
    public void setCounter() {
        counter++;
    }

    /**
     * Getter of responseMessage
     * @return response message to the sender
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Get a clientHandler by user name
     * @param clientName username of client
     * @return clientHandler
     */
    public static ClientHandler getByUserName(String clientName) {
        for (ClientHandler nextClient : clients) {
            if (nextClient.getUserOfClientHandler().getUserName().equals(clientName)) {
                return nextClient;
            }
        }
        return null;
    }

    /**
     * Getter of user
     * @return User
     */
    public User getUserOfClientHandler() {
        return user;
    }

    /**
     * Remove a user in users
     * @param userName username of user that needs to be removed
     * @throws ConcurrentModificationException
     */
    public void removeUserByName(String userName) throws ConcurrentModificationException{
        users.removeIf(user -> user.getUserName().equals(userName));
    }

    /**
     *
     * @param clientSocket Socket of the client
     * @param clients All clients of system
     * @throws IOException Because constructor is using getInputStream and getOutputStream, it needs to use IOException
     */
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
                receiveMessage = in.readLine();
                out.println(parseMessage(receiveMessage));
                out.flush();
                responseMessage = "";
            }
        } catch (Exception e) {
            removeUserByName(user.getUserName());
            System.err.println("IO exception in client handler");
        } finally {
            out.close();
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}

    /**
     * Get a user by name
     * @param name name of user
     * @return User
     */
    User getUserByName(String name) {
        for (User nextUser : users) {
            if (nextUser.getUserName().equals(name)) {
                return nextUser;
            }
        }
        return null;
    }

    /**
     * Get a group by name
     * @param name Name of group
     * @return Group
     */
    private Group getGroupByName(String name) {
        for (Group nextGroup : groups) {
            if (nextGroup.getGroupName().equals(name)) {
                return nextGroup;
            }
        }
        return null;
    }

    /**
     * Check if user exists in the arraylist
     * @param name Name of user
     * @return Boolean of weather the user is in arraylist
     */
    boolean checkIfUserExist(String name) {
        for (User nextUser : users) {
            if (nextUser.getUserName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if group exists by group name
     * @param name Name of group
     * @return Boolean of weather the group exists
     */
    boolean checkIfGroupExist(String name) {
        for (Group nextGroup : groups) {
            if (nextGroup.getGroupName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Getter of arraylist of groups
     * @return Groups
     */
    public static ArrayList<Group> getGroups() {
        return groups;
    }

    /**
     * Get the whole chatting history of the group as a string
     * @return String that represents chatting history of group
     */
    public String getGroupsAsString() {
        String listOfGroupsAsString = "";
        if (groups.size() == 0) {
            listOfGroupsAsString = "ER18 There is no group";
        } else {
            listOfGroupsAsString = "Here are the list of groups:";
            listOfGroupsAsString += "\n";
            for (Group nextGroup: groups) {
                listOfGroupsAsString += nextGroup.getGroupName();
                listOfGroupsAsString += "\n";
            }
            listOfGroupsAsString += "End.";
        }
        return listOfGroupsAsString;
    }

    /**
     * Get list of online users as string
     * @return list of online users or error
     */
    public String getOnlineUsers() {
        String listOfOnlineUsersAsString = "";
        boolean anyUserLoggedIn = false;
        if (!users.isEmpty()) {
            for (User nextUser : users) {
                if (nextUser.isLoggedIn()) {
                    anyUserLoggedIn = true;
                }
            }
        }
        if (!anyUserLoggedIn) {
            listOfOnlineUsersAsString = "ER19 There is no user logged in so far";
            listOfOnlineUsersAsString += "\n";
        } else {
            listOfOnlineUsersAsString = "List of users:";
            listOfOnlineUsersAsString += "\n";
            for (User nextUser : users) {
                if (nextUser.isLoggedIn()) {
                    if (nextUser.userIsAuthenticated()){
                        listOfOnlineUsersAsString += "*";
                    }
                    listOfOnlineUsersAsString += nextUser.getUserName();
                    listOfOnlineUsersAsString += "\n";
                }
            }
        }
        return listOfOnlineUsersAsString;
    }

    /**
     * Disband a group by name
     * @param groupName Name of group
     */
    static void disbandGroup(String groupName) {
        int indexOfGroup = 0;
        for (Group group : groups) {
            if (group.getGroupName().equals(groupName)) {
                indexOfGroup = groups.indexOf(group);
            }
        }
        outToGroup("The group " + groupName + " got disbanded, because the owner quits.",groupName);
        groups.remove(indexOfGroup);
    }

    /**
     * Parse command that received from client
     * @param string command
     * @return Response to the client who sent the command, might be error that warns client
     * @throws Exception
     */
    public String parseMessage (String string) throws Exception {
        //if user responds with Pong extend session.
        if (string.toLowerCase(Locale.ROOT).equals("pong")){
            responseMessage = "Connection duration extended";
            counter = 0;
            return responseMessage;
        }
        if (string.equals("?")){
            responseMessage = "Help list: \n" +
                    "Everything starts with means message from system. \n" +
                    "CONN <username>: Login to the chat server as a guest if <username> isn’t registered before. \n" +
                    "SIGNUP <username> <password>: Register at server if <username> isn't registered before. \n" +
                    "SIGNIN <username> <password>: Sign in using already existing authenticated user. \n" +
                    "USERS: Request all users from the server that are currently online. \n" +
                    "BCST <message>: Broadcast a message to all connected(online) users. \n" +
                    "QUIT: Log out from the server. \n" +
                    "-----server.Group related-----: \n" +
                    "GROUPS: Get a list of all groups. \n" +
                    "HISTORY <group name>: Get chat history of a group. \n" +
                    "JOIN <group name>: Join a group that exists. \n" +
                    "LEAVE <group name>: Leave a group that you are in. \n" +
                    "CREATE <group name>: Create a new group. \n" +
                    "KICK <username to be kicked> <group name>: Kick a user from your group (you have to be the admin/creator of that group) \n" +
                    "GROUP <group name> <message>: Send a message to all members of your group. \n" +
                    "-----Send related-----: \n" +
                    "PRIVATE <username to message> <message>: Send a private message to another user. \n" +
                    "SEND < receiver’s username> <file name>: Send a file to another user.";
            return responseMessage;
        } else if (string.equalsIgnoreCase("Groups")) {
            if (user == null) {
                responseMessage = "ER03 Please log in first";
            } else if (!user.isLoggedIn()) {
                responseMessage = "ER03 Please log in first";
            } else {
                responseMessage = getGroupsAsString();
            }
            return responseMessage;
        } else if (string.equalsIgnoreCase("Users")) {
            if (user == null){
                responseMessage = "ER03 Please log in first";
            } else if (!user.isLoggedIn()) {
                responseMessage = "ER03 Please log in first";
            } else {
                responseMessage = getOnlineUsers();
            }
            return responseMessage;
        } else if (string.equalsIgnoreCase("Quit")) {
            if (user == null){
                responseMessage = "ER03 Please log in first";
            } else if (!user.isLoggedIn()) {
                responseMessage = "ER03 Please log in first";
            } else {
                responseMessage = "You are logged out";
                removeUserByName(user.getUserName());
                user = null;
            }
            return responseMessage;
        }

        int x = string.indexOf(' ');
        if(x<0) {
            responseMessage = "ER21 Please enter the full command";
            return responseMessage;
        }
        typeOfMessage = string.substring(0, x);
        String contentOfMessage = string.substring(x + 1);

        if (typeOfMessage.equalsIgnoreCase("CONN")) {
            responseMessage = login(contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("SIGNIN")) {
            responseMessage = signin(contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("SIGNUP")) {
            responseMessage = signup(contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("BCST")) {
            responseMessage = broadcastMessage(string);
        } else if (typeOfMessage.equalsIgnoreCase("JOIN")) {
            responseMessage = joinGroup(contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("LEAVE")) {
            responseMessage = leaveGroup(contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("CREATE")) {
            responseMessage = createGroup(contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("KICK")) {
            responseMessage = kickPersonOutOfGroup(contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("PRIVATE")) {
            responseMessage = sendPrivateMessage(contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("GROUP")) {
            responseMessage = sendGroupMessage(contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("HISTORY")) {
            responseMessage = checkHistoryOfGroup(contentOfMessage);

        } else if (typeOfMessage.equalsIgnoreCase("IGNORE")){
            responseMessage = "ER17 File path is wrong";
        } else if (typeOfMessage.equalsIgnoreCase("SEND")){
            responseMessage = send(contentOfMessage);
        } else {
            responseMessage = "ER00 Unknown command";
        }
        counter = 0;
        return responseMessage;
    }

    /**
     * Client logs in as guest
     * @param contentOfMessage content of message
     * @return Response to the client who sent the command, might be error that warns client
     * @throws IOException The function is using file, so it needs to throw IOException
     */
    private String login(String contentOfMessage) throws IOException {
        if (user == null) {
            if (!validUsernameFormat(contentOfMessage)){
                responseMessage = "ER02 This username has an invalid format (only characters, numbers and underscores are allowed)";
            } else if(ClientHandler.usernameAlreadyExists(contentOfMessage)){
                responseMessage = "ERO1 User already logged in";
            } else {
                user = new User(contentOfMessage);
                users.add(user);
                responseMessage = "You are logged in with username " + contentOfMessage;
            }
        } else {
            responseMessage = "ER07 You are already logged in";
        }
        return responseMessage;
    }

    /**
     * Client signs in as authenticated user
     * @param contentOfMessage content of message
     * @return Response to the client who sent the command, might be error that warns client
     * @throws IOException The function is using file, so it needs to throw IOException
     */
    private String signin(String contentOfMessage) throws IOException {
        if (user == null) {
            if (!contentOfMessage.contains(" ")) {
                responseMessage = "ER08 You need a password";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String username = contentOfMessage.substring(0, j);
                String password = contentOfMessage.substring(j + 1);
                if (usernameAndPasswordCorrect(username, password, "Internet technology assignment/relatedTextFiles/authenticatedUsers.txt")) {
                    user = new User(username);
                    users.add(user);
                    user.setPassword(password);
                    responseMessage = "You are logged in as authenticated user " + contentOfMessage;
                } else {
                    responseMessage = "ER09 Username/Password wrong";
                }

            }
        } else {
            responseMessage = "ER07 You are already logged in";
        }
        return responseMessage;
    }

    /**
     * Client signs up as authenticated user and sign in with that newly created authenticated user
     * @param contentOfMessage content of message
     * @return Response to the client who sent the command, might be error that warns client
     * @throws IOException The function is using file, so it needs to throw IOException
     */
    private String signup(String contentOfMessage) throws IOException {
        if (user == null) {
            if (!contentOfMessage.contains(" ")) {
                responseMessage = "ER08 You need a password";
            } else {
                boolean userExist = false;
                boolean usernameFormatIsBad = false;
                int j = contentOfMessage.indexOf(' ');
                String username = contentOfMessage.substring(0, j);
                String password = contentOfMessage.substring(j + 1);
                if (!validUsernameFormat(username)){
                    responseMessage = "ER02 This username has an invalid format (only characters, numbers and underscores are allowed)";
                    usernameFormatIsBad = true;
                }
                if (usernameAlreadyExists(username)) {
                    responseMessage = "ERO1 User already logged in";
                    userExist = true;
                }
                if (!userExist && !usernameFormatIsBad) {
                    user = new User(username);
                    user.setPassword(password);
                    users.add(user);
                    storeUsernamePasswordInFile(username, password, "Internet technology assignment/relatedTextFiles/authenticatedUsers.txt");
                    responseMessage = "You are registered and logged in with authenticated user, with username of " + username;
                }
            }
        } else {
            responseMessage = "ER07 You are already logged in";
        }
        return responseMessage;
    }

    /**
     * Client broadcast message to all online people
     * @param string content of message
     * @return Response to the client who sent the command, might be error that warns client
     */
    private String broadcastMessage(String string){
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {
            responseMessage = "ER03 Please log in first";
        } else {
            responseMessage = "<";
            if (getUserByName(user.getUserName()).userIsAuthenticated()) {
                responseMessage += "*";
            }
            responseMessage += getUserOfClientHandler().getUserName() + "> " + string;
            outToAllLoggedIn(responseMessage);
            responseMessage = "Your message has been broadcasted";
        }
        return responseMessage;
    }

    private String joinGroup(String contentOfMessage){
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {

            responseMessage = "ER03 Please log in first";
        } else if (getGroupByName(contentOfMessage) == null){
            responseMessage = "ER10 Such group doesn't exist";
        } else if(getGroupByName(contentOfMessage).checkIfUserExist(user.getUserName())){
            responseMessage = "ER11 You are already part of the group";
        } else if(getGroupByName(contentOfMessage).addMember(getUserByName(user.getUserName()), System.currentTimeMillis())){
            outToAllLoggedIn("User " + user.getUserName() + " joint group " + contentOfMessage);
            responseMessage = "You joined group " + contentOfMessage;
        }
        return responseMessage;
    }

    /**
     * Client leave group
     * @param contentOfMessage content of message
     * @return Response to the client who sent the command, might be error that warns client
     */
    private String leaveGroup(String contentOfMessage){
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {
            responseMessage = "ER03 Please log in first";
        } else {
            if (getGroupByName(contentOfMessage) == null) {
                responseMessage = "ER10 Such group doesn't exist";
            } else if (getGroupByName(contentOfMessage).checkIfUserExist(user.getUserName())) {
                getGroupByName(contentOfMessage).deleteMemberByName(user.getUserName());
                responseMessage = "You left group " + contentOfMessage;
                if (getGroupByName(contentOfMessage).getOwner().getUserName().equals(user.getUserName())) {
                    disbandGroup(contentOfMessage);
                }
            } else {
                responseMessage = "ER12 You cannot leave a group that you have not joint yet";
            }
        }
        return responseMessage;
    }

    /**
     * Client create group
     * @param contentOfMessage content of message
     * @return Response to the client who sent the command, might be error that warns client
     */
    private String createGroup(String contentOfMessage){
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {
            responseMessage = "ER03 Please log in first";
        } else {
            if (checkIfGroupExist(contentOfMessage)) {
                responseMessage = "ER13 Group name already exist";
            } else {
                Group group = new Group(contentOfMessage, getUserByName(user.getUserName()));
                group.addMember(getUserByName(user.getUserName()), System.currentTimeMillis());
                groups.add(group);
                outToAllLoggedIn("User " + user.getUserName() + " created group " + contentOfMessage);
                responseMessage = "Group created";
            }
        }
        return responseMessage;
    }

    /**
     * Client kick someone out of group, only owner group can kick other people
     * @param contentOfMessage content of message
     * @return Response to the client who sent the command, might be error that warns client
     */
    private String kickPersonOutOfGroup(String contentOfMessage){
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {
            responseMessage = "ER03 Please log in first";
        } else {
            int j = contentOfMessage.indexOf(' ');
            String groupName = contentOfMessage.substring(0, j);
            String kickedUsername = contentOfMessage.substring(j + 1);
            if (!checkIfGroupExist(groupName)) {
                responseMessage = "ER10 Such group doesn't exist";
            } else {
                if (!getGroupByName(groupName).getOwner().getUserName().equals(user.getUserName())) {
                    responseMessage = "ER14 You are not the owner of group";
                } else {
                    if (!getGroupByName(groupName).checkIfUserExist(kickedUsername)) {
                        responseMessage = "ER15 The user does not exist in the group";
                    } else if (kickedUsername.equals(user.getUserName())) {
                        responseMessage = "ER20 Cannot kick yourself";
                    } else {
                        getGroupByName(groupName).deleteMemberByName(kickedUsername);
                        responseMessage = "The user " + kickedUsername +" was kicked from the group";
                    }
                }
            }
        }
        return responseMessage;
    }

    /**
     * A client send private message to another client
     * @param contentOfMessage content of message
     * @return Response to the client who sent the command, might be error that warns client
     */
    private String sendPrivateMessage(String contentOfMessage){
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {
            responseMessage = "ER03 Please log in first";
        } else {
            int j = contentOfMessage.indexOf(' ');
            String receiverName = contentOfMessage.substring(0, j);
            String messageToReceiver = contentOfMessage.substring(j + 1);
            if (user.getUserName().equals(receiverName)) {
                responseMessage = "ER06 Cannot send message to yourself";
            } else if (!checkIfUserExist(receiverName)) {
                responseMessage = "ER04 Such user doesn't exist";
            } else {
                if (!getUserByName(receiverName).isLoggedIn()) {
                    responseMessage = "ER05 Target user is not online";
                } else {
                    responseMessage = "###<";
                    if (getUserByName(user.getUserName()).userIsAuthenticated()) {
                        responseMessage += "*";
                    }
                    responseMessage += (user.getUserName() + ">: " + messageToReceiver);
                    outToPrivate(responseMessage,receiverName);
                    responseMessage = "Your message has been sent to " + receiverName;
                }
            }
        }
        return responseMessage;
    }

    /**
     * Client send message to all clients of a group
     * @param contentOfMessage content of message
     * @return Response to the client who sent the command, might be error that warns client
     */
    private String sendGroupMessage(String contentOfMessage){
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {
            responseMessage = "ER03 Please log in first";
        } else {
            int j = contentOfMessage.indexOf(' ');
            String groupName = contentOfMessage.substring(0, j);
            String messageToGroup = contentOfMessage.substring(j + 1);
            if (!checkIfGroupExist(groupName)) {
                responseMessage = "ER10 Such group doesn't exist";
            } else if (getGroupByName(groupName).checkIfUserExist(user.getUserName())) {
                responseMessage = "<" + groupName + ">" + "<";
                if (getUserByName(user.getUserName()).userIsAuthenticated()) {
                    responseMessage += "*";
                }
                responseMessage += (user.getUserName() + "> " + messageToGroup);
                outToGroup(responseMessage, groupName);
                getGroupByName(groupName).addHistoryMessage(responseMessage);
                getGroupByName(groupName).updateTimeOfLastMessage(user.getUserName(), System.currentTimeMillis());
                responseMessage = "Your message has been sent to the group";
            } else {
                responseMessage = "You cannot send message to a group that you have not joint";
            }
        }
        return responseMessage;
    }

    /**
     * Client check chatting history of a group
     * @param contentOfMessage content of message
     * @return Response to the client who sent the command, might be error that warns client
     */
    private String checkHistoryOfGroup(String contentOfMessage){
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {
            responseMessage = "ER03 Please log in first";
        } else {
            if (!checkIfGroupExist(contentOfMessage)) {
                responseMessage = "ER10 Such group doesn't exist";
            } else if (getGroupByName(contentOfMessage).checkIfUserExist(user.getUserName())) {
                responseMessage = getGroupByName(contentOfMessage).getHistoryMessages();
            } else {
                responseMessage = "ER16 You cannot get chatting history of a group that you did not join";
            }
        }
        return responseMessage;
    }

    /**
     * Client send a file
     * @param contentOfMessage content of message
     * @return Response to the client who sent the command, might be error that warns client
     */
    private String send(String contentOfMessage){
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {
            responseMessage = "ER03 Please log in first";
        } else {
            int j = contentOfMessage.indexOf(' ');
            String receiverName = contentOfMessage.substring(0, j);
            if (!checkIfUserExist(receiverName)) {
                responseMessage = "ER04 Such user doesn't exist";
            } else {
                responseMessage = "Your file has been sent successfully";
                receiveFile(receiverName);
            }
        }
        return responseMessage;
    }

    /**
     * Send message to all logged in clients
     * @param responseMessage Message that will be sent
     */
    private void outToAllLoggedIn(String responseMessage) {
        for( ClientHandler clientHandlers : clients){
            if (clientHandlers.getUserOfClientHandler().getUserName() != null){
                clientHandlers.out.println(responseMessage);
            }
        }
    }

    /**
     * Send message to a client
     * @param responseMessage Message that will be sent
     * @param name Name of receiver client
     */
    static void outToPrivate(String responseMessage, String name){
        for( ClientHandler clientHandlers : clients){
            if (getByUserName(name).equals(clientHandlers)) {
                clientHandlers.out.println(responseMessage);
            }
        }
    }

    /**
     * Send message to all clients of a group
     * @param responseMessage Message that will be sent
     * @param groupName Name of group
     */
    private static void outToGroup(String responseMessage, String groupName){
        for( ClientHandler clientHandlers : clients){
            for (Group group : groups){
                if(group.checkIfUserExist(clientHandlers.getUserOfClientHandler().getUserName()) && groupName.equals(group.getGroupName())){
                    clientHandlers.out.println(responseMessage);
                }
            }
        }
    }

    /**
     * Client receive file
     * @param name Name of receiver client
     */
    private void receiveFile(String name) {
        for( ClientHandler clientHandlers : clients){
            if (getByUserName(name).equals(clientHandlers)) {
                clientHandlers.out.println("You received a new file");
            }
        }
    }

    /**
     * Store username and password to txt file for authenticated user
     * @param username Username of authenticated user
     * @param password Password of authenticated user
     * @param file File that stores usernames and passwords of authenticated users
     * @throws IOException The function is using file, so it needs to throw IOException
     */
    private void storeUsernamePasswordInFile(String username, String password, String file) throws IOException {
        String passwordAfterEncryption = AES.encrypt(password);
        BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
        out.append(username).append(" ").append(passwordAfterEncryption);
        out.newLine();
        out.close();
    }

    /**
     * Check if user name already existed as guest or authenticated user
     * @param username Username that will be checked
     * @return Boolean weather the username is already existing
     * @throws IOException The function is using file, so it needs to throw IOException
     */
    private static boolean usernameAlreadyExists(String username) throws IOException {
        return usernameAlreadyExistsAsGuest(username) || usernameAlreadyExistsAsAuthenticatedUser(username, "Internet technology assignment/relatedTextFiles/authenticatedUsers.txt");
    }

    /**
     * Check if user name already existed as authenticated user
     * @param username Username that will be checked
     * @param file File that stores usernames and passwords of authenticated users
     * @return Boolean weather the username is already existing as authenticated user
     * @throws IOException The function is using file, so it needs to throw IOException
     */
    private static boolean usernameAlreadyExistsAsAuthenticatedUser(String username, String file) throws IOException {
        boolean usernameAlreadyExist = false;
        for (String usernameAndPassword : getAllUsernameAndPasswordFromFileAsArray(file)) {
            String[] parts = usernameAndPassword.split(" ");
            String usernameOfFile = parts[0];
            if (username.equals(usernameOfFile)) {
                usernameAlreadyExist = true;
            }
        }
        return usernameAlreadyExist;
    }

    /**
     * Check if user name already existed as guest
     * @param username Username that will be checked
     * @return Boolean weather the username is already existing as guest
     */
    private static boolean usernameAlreadyExistsAsGuest(String username) {
        boolean usernameAlreadyExist = false;
        for (User user : users) {
            if (username.equals(user.getUserName())) {
                usernameAlreadyExist = true;
            }
        }
        return usernameAlreadyExist;
    }

    /**
     * Get all usernames and passwords of authenticated users as an arraylist, every item of arraylist is a username and a password
     * @param file File that stores usernames and passwords of authenticated users
     * @return Arraylist of usernames and passwords of authenticated users
     * @throws IOException The function is using file, so it needs to throw IOException
     */
    private static ArrayList<String> getAllUsernameAndPasswordFromFileAsArray(String file) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(file)));
        return new ArrayList<>(Arrays.asList(content.split("\\r?\\n")));
    }

    /**
     * Check if string matches rule of username
     * @param username Username that will be checked
     * @return Boolean weather the username is matching the rule
     */
    private static boolean validUsernameFormat(String username) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{3,14}$");
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    /**
     * Check if username and password are correct as authenticated user
     * @param username Username that will be checked
     * @param password Password that will be checked
     * @param file File that stores usernames and passwords of authenticated users
     * @return Boolean of weather the username and password are correct as existing authenticated user
     * @throws IOException The function is using file, so it needs to throw IOException
     */
    public static boolean usernameAndPasswordCorrect(String username, String password, String file) throws IOException {
        boolean usernameExistsAndMatchPassword = false;
        for (String usernameAndPassword : getAllUsernameAndPasswordFromFileAsArray(file)) {
            String[] parts = usernameAndPassword.split(" ");
            if (!parts[0].equals("")) {
                String usernameOfFile = parts[0];
                String passwordOfFile = parts[1];
                String passwordOfFileAfterDecryption = AES.decrypt(passwordOfFile);
                if (username.equals(usernameOfFile)) {
                    assert passwordOfFileAfterDecryption != null;
                    if (passwordOfFileAfterDecryption.equals(password)) {
                        usernameExistsAndMatchPassword = true;
                    }
                }
            }
        }
        return usernameExistsAndMatchPassword;
    }

}
