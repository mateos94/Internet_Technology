package server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
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
    private String typeOfMessage;

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

    public static ClientHandler getByUserName(String clientName) {
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
               // removeUserByName(user.getUserName());
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
            listOfGroupsAsString = "ER18 There is no group";
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
            listOfOnlineUsersAsString = "ER19 There is no user logged in so far";
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

    static void disbandGroup(String groupName) {
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
                    "CONN <username>: Login to the chat server as a guest if <username> isn’t registered before. \n" +
                    "SIGNUP <username> <password>: Register at server if <username> isn't registered before. \n" +
                    "SIGNIN <username> <password>: Sign in using already existing authenticated user. \n" +
                    "USERS: Request all users from the server that are currently online. \n" +
                    "BCST <message>: Broadcast a message to all connected(online) users. \n" +
                    "QUIT: Log out from the server. \n" +
                    "# -----server.Group related-----: \n" +
                    "GROUPS: Get a list of all groups. \n" +
                    "HISTORY <group name>: Get chat history of a group. \n" +
                    "JOIN <group name>: Join a group that exists. \n" +
                    "LEAVE <group name>: Leave a group that you are in. \n" +
                    "CREATE <group name>: Create a new group. \n" +
                    "KICK <username to be kicked> <group name>: Kick a user from your group (you have to be the admin/creator of that group) \n" +
                    "GROUP <group name> <message>: Send a message to all members of your group. \n" +
                    "# -----Send related-----: \n" +
                    "PRIVATE <username to message> <message>: Send a private message to another user. \n" +
                    "SEND < receiver’s username> <file name>: Send a file to another user. \n" +
                    "Pong: extend the duration of your connection";
            return responseMessage;
        } else if (string.equalsIgnoreCase("Groups")) {
            if (user == null){
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
                responseMessage = "# " + currentTimeAsDateString + " You are logged out";
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
        } else if (typeOfMessage.equalsIgnoreCase("SEND")){
            responseMessage = send(contentOfMessage);
        } else if (typeOfMessage.equalsIgnoreCase("FILE")){
            responseMessage = askToSendFile(contentOfMessage);
        }

        else {
            responseMessage = "ER00 Unknown command";
        }
        counter = 0;
        return responseMessage;

    }

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

    private String signin(String contentOfMessage) throws IOException {
        if (user == null) {
            if (!contentOfMessage.contains(" ")) {
                responseMessage = "ER08 You need a password";
            } else {
                int j = contentOfMessage.indexOf(' ');
                String username = contentOfMessage.substring(0, j);
                String password = contentOfMessage.substring(j + 1);
                if (usernameAndPasswordCorrect(username, password, "client/authenticatedUsers.txt")) {
                    user = new User(contentOfMessage);
                    users.add(user);
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

    private String signup(String contentOfMessage) throws IOException {
        if (user == null) {
            if (!contentOfMessage.contains(" ")) {
                responseMessage = "ER08 You need a password";
            } else {
                boolean userExist = false;
                int j = contentOfMessage.indexOf(' ');
                String username = contentOfMessage.substring(0, j);
                String password = contentOfMessage.substring(j + 1);
                if (usernameAlreadyExists(username)) {
                    responseMessage = "ERO1 User already logged in";
                    userExist = true;
                }
                if (!userExist) {
                    user = new User(username);
                    user.setPassword(password);
                    users.add(user);
                    storeUsernamePasswordInFile(username, password, "client/authenticatedUsers.txt");
                    responseMessage = "You are registered and logged in with authenticated user, with username of " + username;
                }
            }
        } else {
            responseMessage = "ER07 You are already logged in";
        }
        return responseMessage;
    }

    private String broadcastMessage(String string){
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {
            responseMessage = "ER03 Please log in first";
        } else {
            responseMessage = "<" + getUserOfClientHandler().getUserName() + "> " + string;
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
        } else if(getGroupByName(contentOfMessage).addMember(getUserByName(user.getUserName()), System.currentTimeMillis())){
            outToAllLoggedIn("User " + user.getUserName() + " joint group " + contentOfMessage);
            responseMessage = "You joined group " + contentOfMessage;
        } else{
            responseMessage = "ER11 Failed to join group";
        }
        return responseMessage;
    }

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
                    } else {
                        getGroupByName(groupName).deleteMemberByName(kickedUsername);
                        responseMessage = "The user " + kickedUsername +" was kicked from the group";
                    }
                }
            }
        }
        return responseMessage;
    }

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
                    responseMessage = "<";
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
            }
        }
        return responseMessage;
    }

    private String askToSendFile(String contentOfMessage) throws IOException {
        if (user == null){
            responseMessage = "ER03 Please log in first";
        } else if (!user.isLoggedIn()) {
            responseMessage = "ER03 Please log in first";
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
                responseMessage = "ER17 File path is wrong";
            } else if (!checkIfUserExist(receiverName)) {
                responseMessage = "ER04 Such user doesn't exist";
            } else {
                outToPrivate("<" + user.getUserName() + "> want to send you a file, do you allow? type file to save it", receiverName);
                responseMessage = "Your file has been sent successfully, if the receiver allows, he/she will receive";
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

    static void outToPrivate(String responseMessage, String name){
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
                //new Thread(fileSender).start();
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
                        message += "# Because of " + nextUserAndTimeOfLastMessage.getUser().getUserName() + "did not talk for more than 2 mins in group " + nextGroup.getGroupName() + ", got kicked out of group";
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

    public static boolean usernameAlreadyExists(String username) throws IOException {
        return usernameAlreadyExistsAsGuest(username) || usernameAlreadyExistsAsAuthenticatedUser(username, "client/authenticatedUsers.txt");
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

    public static boolean usernameAlreadyExistsAsGuest(String username) {
        boolean usernameAlreadyExist = false;
        for (User user : users) {
            if (username.equals(user.getUserName())) {
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

