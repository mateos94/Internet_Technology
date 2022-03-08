import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public ClientHandler getByUserName(String clientName) {
        for (ClientHandler nextClient : clients) {
            if (nextClient.getUserOfClientHandler().getUserName().equals(clientName)) {
                return nextClient;
            }
        }
        return null;
    }

    public User getUserOfClientHandler() {
        return user;
    }

    public void removeUserByName(String userName){
        users.removeIf(user -> user.getUserName().equals(userName));
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
            receiveMessage = in.readLine();
            out.println(parseMessage(receiveMessage));
            out.flush();
            responseMessage = "";
    }
        } catch (IOException e) {
            try {
                removeNotOnlineGuestFromFile("client/users.txt");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            System.err.println("IO exception in client handler");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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

    public static ArrayList<Group> getGroups() {
        return groups;
    }

    public String getGroupsAsString() {
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
            listOfOnlineUsersAsString = "#There is no user logged in so far.";
            listOfOnlineUsersAsString += "\n";
        } else {
            listOfOnlineUsersAsString = "#List of users:";
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

    public void disbandGroup(String groupName) {
        groups.removeIf(nextGroup -> nextGroup.getGroupName().equals(groupName));
    }

    public String parseMessage (String string) throws Exception {
        String currentTimeAsDateString = convertTimestampToDate(System.currentTimeMillis());
        //if user responds with Pong extend session.
        if (string.contains("Pong")){
            responseMessage = "Connection duration extended";
            counter = 0;
            return responseMessage;
        }
        int i = string.indexOf(' ');
        if (string.equals("?")) {
            responseMessage =
                    "# Help list: \n" +
                    "Everything starts with # means message from system. \n" +
                    "Login <username>: Login to the chat server as a guest if <username> isn’t registered before. \n" +
                    "Signup <username> <password>: Register at server if <username> isn't registered before. \n" +
                    "Signin <username> <password>: Sign in using already existing authenticated user. \n" +
                    "Users: Request all users from the server that are currently online. \n" +
                    "Broadcast <message>: Broadcast a message to all connected(online) users. \n" +
                    "Quit: Log out from the server. \n" +
                    "# -----Group related-----: \n" +
                    "Groups: Get a list of all groups. \n" +
                    "History <group name>: Get chat history of a group. \n" +
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
        } else if (string.equalsIgnoreCase("Groups")) {
            if (user == null){
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else if (!user.isLoggedIn()) {
                responseMessage = "#You need to login first.";
            } else {
                responseMessage = getGroupsAsString();
            }
            return responseMessage;
        } else if (string.equalsIgnoreCase("Users")) {
            if (user == null){
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else if (!user.isLoggedIn()) {
                responseMessage = "#You need to login first.";
            } else {
                responseMessage = getOnlineUsers();
            }
            return responseMessage;
        } else if (string.equalsIgnoreCase("Quit")) {
            if (user == null){
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else if (!user.isLoggedIn()) {
                responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
            } else {
                if (!user.userIsAuthenticated()) {
                    removeUsernameInFile(user.getUserName(),"client/users.txt");
                }
                responseMessage = "# " + currentTimeAsDateString + " You are logged out.";
                removeUsernameInFile(user.getUserName(), "client/users.txt");
                removeUserByName(user.getUserName());
                user = null;
            }
            return responseMessage;
        }
        int x = string.indexOf(' ');
        if(x<0) {
            responseMessage = "#Please enter the full command";
            return responseMessage;
        }
        String typeOfMessage = string.substring(0, x);
        String contentOfMessage = string.substring(x + 1);
        if (typeOfMessage.equalsIgnoreCase("Login")) {
            responseMessage = login(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("Signin")) {
            responseMessage = signin(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("Signup")) {
            responseMessage = signup(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("Broadcast")) {
            responseMessage = broadcastMessage(currentTimeAsDateString, string);
        } else if (typeOfMessage.equalsIgnoreCase("Join")) {
            responseMessage = joinGroup(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("Leave")) {
            responseMessage = leaveGroup(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("Create")) {
            responseMessage = createGroup(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("Kick")) {
            responseMessage = kickPersonOutOfGroup(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("Private")) {
            responseMessage = sendPrivateMessage(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("Group")) {
            responseMessage = sendGroupMessage(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("History")) {
            responseMessage = checkHistoryOfGroup(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("Send")){
            responseMessage = send(currentTimeAsDateString, contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("File")){
            responseMessage = askToSendFile(currentTimeAsDateString, contentOfMessage);
        }

        else {
            responseMessage = "# " + currentTimeAsDateString + " It does not match any function.";
        }
        counter = 0;
        return responseMessage;

    }

    private String login(String currentTimeAsDateString, String contentOfMessage) throws IOException {
        if (user == null) {
            if (!validUsernameFormat(contentOfMessage)){
                responseMessage = "# " + currentTimeAsDateString + " This username has an invalid format (only characters, numbers and underscores are allowed).";
            } else if(ClientHandler.usernameAlreadyExists(contentOfMessage)){
                responseMessage = "# " + currentTimeAsDateString + " This name is already in use please try a different name.";
            } else {
                user = new User(contentOfMessage);
                users.add(user);
                storeUsernameInFile(user.getUserName(), "client/users.txt");
                responseMessage = "#In: " + currentTimeAsDateString + " You are logged in with username " + contentOfMessage + ".";
            }
        } else {
            responseMessage = "# " + currentTimeAsDateString + " You are already logged in.";
        }
        return responseMessage;
    }

    private String signin(String currentTimeAsDateString, String contentOfMessage) throws IOException {
        if (user == null) {
            if (!contentOfMessage.contains(" ")) {
                responseMessage = "# " + currentTimeAsDateString + " You need a password.";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String username = contentOfMessage.substring(0, j);
                String password = contentOfMessage.substring(j + 1);
                if (usernameAndPasswordCorrect(username, password, "client/authenticatedUsers.txt")) {
                    user = new User(contentOfMessage);
                    users.add(user);
                    responseMessage = "#In: " + currentTimeAsDateString + " You are logged in as authenticated user " + contentOfMessage + ".";
                } else {
                    responseMessage = "# " + currentTimeAsDateString + " Username/Password wrong.";
                }

            }
        } else {
            responseMessage = "# " + currentTimeAsDateString + " You are already logged in.";
        }
        return responseMessage;
    }

    private String signup(String currentTimeAsDateString, String contentOfMessage) throws IOException {
        if (user == null) {
            if (!contentOfMessage.contains(" ")) {
                responseMessage = "# " + currentTimeAsDateString + " You need a password.";
            } else {
                boolean userExist = false;
                int j = contentOfMessage.indexOf(' ');
                String username = contentOfMessage.substring(0, j);
                String password = contentOfMessage.substring(j + 1);
                if (usernameAlreadyExists(username)) {
                    responseMessage = "# " + currentTimeAsDateString + " Cannot sign up, this user already exists.";
                    userExist = true;
                }
                if (!userExist) {
                    user = new User(username);
                    user.setPassword(password);
                    users.add(user);
                    storeUsernamePasswordInFile(username, password, "client/authenticatedUsers.txt");
                    responseMessage = "# " + currentTimeAsDateString + " You are registered and logged in with authenticated user, with username of " + username + ".";
                }
            }
        } else {
            responseMessage = "# " + currentTimeAsDateString + " You are already logged in.";
        }
        return responseMessage;
    }

    private String broadcastMessage(String currentTimeAsDateString, String string){
        if (user == null){
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (!user.isLoggedIn()) {
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else {
            responseMessage = "# " + currentTimeAsDateString + " <" + getUserOfClientHandler().getUserName() + "> " + string;
            outToAllLoggedIn(responseMessage);
            responseMessage = "# " + currentTimeAsDateString + "Your message has been broadcasted";
        }
        return responseMessage;
    }

    private String joinGroup(String currentTimeAsDateString, String contentOfMessage){
        if (user == null){
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (!user.isLoggedIn()) {

            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (getGroupByName(contentOfMessage) == null){
            responseMessage = "# " + currentTimeAsDateString + " Such group does not exist.";
        } else if(getGroupByName(contentOfMessage).addMember(getUserByName(user.getUserName()), System.currentTimeMillis())){
            outToAllLoggedIn("# " + currentTimeAsDateString + " User " + user.getUserName() + " joint group " + contentOfMessage + ".");
            responseMessage = "# " + currentTimeAsDateString + " You joined group " + contentOfMessage;
        } else{
            responseMessage = "# " + currentTimeAsDateString + " Failed to join the group";
        }
        return responseMessage;
    }

    private String leaveGroup(String currentTimeAsDateString, String contentOfMessage){
        if (user == null){
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (!user.isLoggedIn()) {
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else {
            if (getGroupByName(contentOfMessage) == null) {
                responseMessage = "# " + currentTimeAsDateString + " Such group does not exist.";
            } else if (getGroupByName(contentOfMessage).checkIfUserExist(user.getUserName())) {
                getGroupByName(contentOfMessage).deleteMemberByName(user.getUserName());
                responseMessage = "# " + currentTimeAsDateString + " You left group " + contentOfMessage;
                if (getGroupByName(contentOfMessage).getOwner().getUserName().equals(user.getUserName())) {
                    disbandGroup(contentOfMessage);
                }
            } else {
                responseMessage = "# " + currentTimeAsDateString + " You cannot leave a group that you have not joint yet.";
            }
        }
        return responseMessage;
    }

    private String createGroup(String currentTimeAsDateString, String contentOfMessage){
        if (user == null){
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (!user.isLoggedIn()) {
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else {
            if (checkIfGroupExist(contentOfMessage)) {
                responseMessage = "# " + currentTimeAsDateString + " This group already exist.";
            } else {
                Group group = new Group(contentOfMessage, getUserByName(user.getUserName()));
                group.addMember(getUserByName(user.getUserName()), System.currentTimeMillis());
                groups.add(group);
                outToAllLoggedIn("# " + currentTimeAsDateString + " User " + user.getUserName() + " created group " + contentOfMessage + ".");
                responseMessage = "# " + currentTimeAsDateString +" Group created.";
            }
        }
        return responseMessage;
    }

    private String kickPersonOutOfGroup(String currentTimeAsDateString, String contentOfMessage){
        if (user == null){
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (!user.isLoggedIn()) {
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else {
            int j = contentOfMessage.indexOf(' ');
            String groupName = contentOfMessage.substring(0, j);
            String kickedUsername = contentOfMessage.substring(j + 1);
            if (!checkIfGroupExist(groupName)) {
                responseMessage = "# " + currentTimeAsDateString + " This group does not exist.";
            } else {
                if (!getGroupByName(groupName).getOwner().getUserName().equals(user.getUserName())) {
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
        return responseMessage;
    }

    private String sendPrivateMessage(String currentTimeAsDateString, String contentOfMessage){
        if (user == null){
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (!user.isLoggedIn()) {
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else {
            int j = contentOfMessage.indexOf(' ');
            String receiverName = contentOfMessage.substring(0, j);
            String messageToReceiver = contentOfMessage.substring(j + 1);
            if (user.getUserName().equals(receiverName)) {
                responseMessage = "# " + currentTimeAsDateString + " You cannot send message to yourself.";
            } else if (!checkIfUserExist(receiverName)) {
                responseMessage = "#" + currentTimeAsDateString + " User does not exist.";
            } else {
                if (!getUserByName(receiverName).isLoggedIn()) {
                    responseMessage = "# " + currentTimeAsDateString + " Target user is not online now.";
                } else {
                    responseMessage = "# " + currentTimeAsDateString + " <";
                    if (getUserByName(user.getUserName()).userIsAuthenticated()) {
                        responseMessage += "*";
                    }
                    responseMessage += (user.getUserName() + ">: " + messageToReceiver);
                    outToPrivate(responseMessage,receiverName);
                    responseMessage = "# " + currentTimeAsDateString + " Your message has been sent to " + receiverName + ".";
                }
            }
        }
        return responseMessage;
    }

    private String sendGroupMessage(String currentTimeAsDateString, String contentOfMessage){
        if (user == null){
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (!user.isLoggedIn()) {
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else {
            int j = contentOfMessage.indexOf(' ');
            String groupName = contentOfMessage.substring(0, j);
            String messageToGroup = contentOfMessage.substring(j + 1);
            if (!checkIfGroupExist(groupName)) {
                responseMessage = "# " + currentTimeAsDateString + " This group does not exist.";
            } else if (getGroupByName(groupName).checkIfUserExist(user.getUserName())) {
                responseMessage = "# " + currentTimeAsDateString + " <" + groupName + ">" + "<";
                if (getUserByName(user.getUserName()).userIsAuthenticated()) {
                    responseMessage += "*";
                }
                responseMessage += (user.getUserName() + "> " + messageToGroup);
                outToGroup(responseMessage, groupName);
                getGroupByName(groupName).addHistoryMessage(responseMessage);
                getGroupByName(groupName).updateTimeOfLastMessage(user.getUserName(), System.currentTimeMillis());
                responseMessage = "# " + currentTimeAsDateString + " Your message has been sent to the group.";
            } else {
                responseMessage = "# " + currentTimeAsDateString + " You cannot send message to a group that you have not joint.";
            }
        }
        return responseMessage;
    }

    private String checkHistoryOfGroup(String currentTimeAsDateString, String contentOfMessage){
        if (user == null){
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (!user.isLoggedIn()) {
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else {
            if (!checkIfGroupExist(contentOfMessage)) {
                responseMessage = "# " + currentTimeAsDateString + " This group does not exist.";
            } else if (getGroupByName(contentOfMessage).checkIfUserExist(user.getUserName())) {
                responseMessage = getGroupByName(contentOfMessage).getHistoryMessages();
            } else {
                responseMessage = "# " + currentTimeAsDateString + " You cannot get chatting history of a group that you did not join.";
            }
        }
        return responseMessage;
    }

    private String send(String currentTimeAsDateString, String contentOfMessage){
        if (user == null){
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (!user.isLoggedIn()) {
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
        return responseMessage;
    }

    private String askToSendFile(String currentTimeAsDateString, String contentOfMessage) throws IOException {
        if (user == null){
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else if (!user.isLoggedIn()) {
            responseMessage = "# " + currentTimeAsDateString + " You need to login first.";
        } else {
            int j = contentOfMessage.indexOf(' ');
            String receiverName = contentOfMessage.substring(0, j);
            String filePath = contentOfMessage.substring(j + 1);
            File file = new File(filePath);

            FileInputStream fis;
            DataOutputStream dos;

            if (file.exists()) {
                try {
                        fis = new FileInputStream(file);
                        dos = new DataOutputStream(client.getOutputStream());

                        dos.writeUTF(file.getName());
                        dos.flush();
                        dos.writeLong(file.length());
                        dos.flush();

                        byte[] bytes = new byte[1024];
                        int length = 0;
                        long progress = 0;
                        while((length = fis.read(bytes, 0, bytes.length)) != -1) {
                            dos.write(bytes, 0, length);
                            dos.flush();
                            progress += length;
                            System.out.print("| " + (100*progress/file.length()) + "% |");
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    client.close();
                }
            }

            if (!file.isFile()) {
                responseMessage = "# " + currentTimeAsDateString + " File path is wrong.";
            } else if (!checkIfUserExist(receiverName)) {
                responseMessage = "# " + currentTimeAsDateString + " User does not exist.";
            } else {
                outToPrivate("# <" + user.getUserName() + "> want to send you a file, do you allow? type file to save it.", receiverName);
                responseMessage = "# " + currentTimeAsDateString + " Your file has been sent successfully, if the receiver allows, he/she will receive";
            }
        }
        return responseMessage;
    }

    static void outToAll(String responseMessage) {
        for( ClientHandler clientHandlers : clients){
            clientHandlers.out.println(responseMessage);
        }
    }

    private void outToAllLoggedIn(String responseMessage) {
        for( ClientHandler clientHandlers : clients){
            if (clientHandlers.getUserOfClientHandler().getUserName() != null){
                clientHandlers.out.println(responseMessage);
            }
        }
    }

    private void outToPrivate(String responseMessage, String name){
        for( ClientHandler clientHandlers : clients){
            if (getByUserName(name).equals(clientHandlers)) {
                clientHandlers.out.println(responseMessage);
            }
        }
    }

    private void outToGroup(String responseMessage, String groupName){
        for( ClientHandler clientHandlers : clients){
            for (Group group : groups){
                if(group.checkIfUserExist(clientHandlers.getUserOfClientHandler().getUserName()) && groupName.equals(group.getGroupName())){
                    clientHandlers.out.println(responseMessage);
                }
            }
        }
    }

    public void changeLoginStatus(){
        for (User nextUser : users) {
            if (nextUser.getUserName().equals(user.getUserName())) {
                nextUser.changeLoginStatus();
            }
        }
    }

    private void receiveFile(String name) throws IOException {
        byte b[] = new byte[1024];
        InputStream inputStream = client.getInputStream();
        FileOutputStream fileOutputStream = new FileOutputStream("client/server.txt");
        inputStream.read(b,0,b.length);
        fileOutputStream.write(b,0,b.length);
        for( ClientHandler clientHandlers : clients){
            if (getByUserName(name).equals(clientHandlers)) {
                clientHandlers.out.println("You received a new file");
                FileSender fileSender = new FileSender(clientHandlers.getClient(),"client/server.txt");
                new Thread(fileSender).start();
            }
        }
    }

    private String convertTimestampToDate(long timestamp){

        Date date = new Date(timestamp);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }

    public void kickPeopleWhoAreNotChattingMoreThanTwoMinutesInGroups() {
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

    public void storeUsernamePasswordInFile(String username, String password, String file) throws IOException {
        String passwordAfterEncryption = AES.encrypt(password);
        BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
        out.append(username).append(" ").append(passwordAfterEncryption);
        out.newLine();
        out.close();
    }


    public void storeUsernameInFile(String username, String file) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
        out.append(username);
        out.newLine();
        out.close();
    }

    public static boolean usernameAlreadyExists(String username) throws IOException {
        return usernameAlreadyExistsAsGuest(username, "client/users.txt") || usernameAlreadyExistsAsAuthenticatedUser(username, "client/authenticatedUsers.txt");
    }

    public static boolean usernameAlreadyExistsAsAuthenticatedUser(String username, String file) throws IOException {
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

    public static boolean usernameAlreadyExistsAsGuest(String username, String file) throws IOException {
        boolean usernameAlreadyExist = false;
        for (String usernameAndPassword : getAllUsernameAndPasswordFromFileAsArray(file)) {
            if (username.equals(usernameAndPassword)) {
                usernameAlreadyExist = true;
            }
        }
        return usernameAlreadyExist;
    }

    public void removeUsernameInFile(String username, String file) throws Exception {
        File targetFile = new File(file);
        List<String> out = Files.lines(targetFile.toPath())
                .filter(line -> !line.contains(username))
                .collect(Collectors.toList());
        Files.write(targetFile.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void removeNotOnlineGuestFromFile(String file) throws Exception {
        ArrayList<String> onlineUsers = new ArrayList<>();
        for (User nextUser: users) {
            if (nextUser.getUserName().equals("") && nextUser.getUserName() != null) {
                onlineUsers.add(nextUser.getUserName());
            }
        }

        for (String nextUsernameInFile: getAllUsernameAndPasswordFromFileAsArray(file)){
            if (!onlineUsers.contains(nextUsernameInFile)) {
                removeUsernameInFile(nextUsernameInFile, file);
            }
        }
    }


    public static ArrayList<String> getAllUsernameAndPasswordFromFileAsArray(String file) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(file)));
        return new ArrayList<>(Arrays.asList(content.split("\\r?\\n")));
    }

    public static boolean validUsernameFormat(String username) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{3,14}$");
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

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

