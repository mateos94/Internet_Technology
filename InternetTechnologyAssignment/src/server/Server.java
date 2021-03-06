package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Mateos and Haoshuang
 */
public class Server {

    private static final int LIMIT_OF_CLIENTS = 20;
    private static final int PORT = 1337;
    private static final int MILLISECONDS_OF_KICKING_MEMBER_WHO_DID_NOT_TALK = 120000;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService poolOfClientThread = Executors.newFixedThreadPool(LIMIT_OF_CLIENTS);
    private static ExecutorService poolOfTimerThread = Executors.newFixedThreadPool(LIMIT_OF_CLIENTS);
    private static ExecutorService poolOfFileReceiverServer = Executors.newFixedThreadPool(LIMIT_OF_CLIENTS);
    private static ExecutorService poolOfFileSenderServer = Executors.newFixedThreadPool(LIMIT_OF_CLIENTS);

    public static void main(String args[]) throws IOException {


        ServerSocket serverSocket = new ServerSocket(PORT);

        Timer timer = new Timer();
        int begin = 0;
        timer.schedule(new TimerTask() {
            @Override
            public void run() throws ConcurrentModificationException {
                try {
                    kickPeopleWhoAreNotChattingMoreThanTwoMinutesInGroups();
                } catch (ConcurrentModificationException concurrentModificationException){
                    System.out.println("ConcurrentModificationException happened");
                }
            }
        }, begin, MILLISECONDS_OF_KICKING_MEMBER_WHO_DID_NOT_TALK);

        while (true) {
            Socket client = serverSocket.accept();
            ClientHandler clientThread = new ClientHandler(client, clients);
            TimerThread timerThread = new TimerThread(client,clientThread);

            clients.add(clientThread);
            poolOfClientThread.execute(clientThread);
            poolOfTimerThread.execute(timerThread); // a timer thread that check for activity each 10 secs disable it for demoing freely

            if(clientThread.getResponseMessage().equals("Your file has been sent successfully")){
                FileReceiverServer fileReceiverServer = new FileReceiverServer(client);
                poolOfFileReceiverServer.execute(fileReceiverServer);
            }
            if(clientThread.getResponseMessage().equals("You received a new file")){
                FileSenderServer fileSenderServer = new FileSenderServer(client);
                poolOfFileSenderServer.execute(fileSenderServer);
            }



            System.out.println("a new client has been connected");
        }

    }

    /**
     * Kicking people who do not talk in a group for more than 2 minutes
     * @throws ConcurrentModificationException
     */
    private static void kickPeopleWhoAreNotChattingMoreThanTwoMinutesInGroups() throws ConcurrentModificationException {
        if (!ClientHandler.getGroups().isEmpty()){
            for (Group nextGroup: ClientHandler.getGroups()){
                for (UserAndTimeOfLastMessage nextUserAndTimeOfLastMessage: nextGroup.getMembersAndTimeOfLastMessage()){
                    if (System.currentTimeMillis() - nextUserAndTimeOfLastMessage.getTimestampOfLastMessage() > 10000){
                        nextGroup.deleteMemberByName(nextUserAndTimeOfLastMessage.getUser().getUserName());
                        if (nextGroup.getOwner().getUserName().equals(nextUserAndTimeOfLastMessage.getUser().getUserName())) {
                            ClientHandler.disbandGroup(nextGroup.getGroupName());
                        }
                        String message = "";
                        message += "Because of " + nextUserAndTimeOfLastMessage.getUser().getUserName() + " did not talk for more than 2 mins in group " + nextGroup.getGroupName() + ", got kicked out of group.";
                        ClientHandler.outToPrivate(message, nextUserAndTimeOfLastMessage.getUser().getUserName());
                    }
                }
            }
        }
    }



}
