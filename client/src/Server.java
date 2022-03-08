import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    static final int LIMIT_OF_CLIENTS = 20;
    static final int PORT = 1337;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(LIMIT_OF_CLIENTS);
    private static ExecutorService pool2 = Executors.newFixedThreadPool(LIMIT_OF_CLIENTS);

    public static void main(String args[]) throws InterruptedException, IOException {


        ServerSocket  serverSocket = new ServerSocket(PORT);

        Timer timer = new Timer();
        int begin = 0;
        int timeInterval = 5000;
        timer.schedule(new TimerTask() {
            @Override
            public void run() throws ConcurrentModificationException {
                try {
                    kickPeopleWhoAreNotChattingMoreThanTwoMinutesInGroups();
                } catch (ConcurrentModificationException concurrentModificationException){
                    System.out.println("concurrentModificationException happened");
                }
            }
        }, begin, timeInterval);

        while (true) {
            Socket client = serverSocket.accept();
            ClientHandler clientThread = new ClientHandler(client, clients);
            TimerThread timerThread = new TimerThread(client,clientThread);
            clients.add(clientThread);
            pool.execute(clientThread);
            pool2.execute(timerThread);
            System.out.println("a new client has been connected");
        }
    }

    public static void kickPeopleWhoAreNotChattingMoreThanTwoMinutesInGroups() throws ConcurrentModificationException {
        if (!ClientHandler.getGroups().isEmpty()){
            for (Group nextGroup: ClientHandler.getGroups()){
                for (UserAndTimeOfLastMessage nextUserAndTimeOfLastMessage: nextGroup.getMembersAndTimeOfLastMessage()){
                    if (System.currentTimeMillis() - nextUserAndTimeOfLastMessage.getTimestampOfLastMessage() > 10000){
                        nextGroup.deleteMemberByName(nextUserAndTimeOfLastMessage.getUser().getUserName());
                        String message = "";
                        message += "# Because of " + nextUserAndTimeOfLastMessage.getUser().getUserName() + " did not talk for more than 2 mins in group " + nextGroup.getGroupName() + ", got kicked out of group.";
                        ClientHandler.outToAll(message);
                    }
                }
            }
        }
    }

}
