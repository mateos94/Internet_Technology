import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    static final int LIMIT_OF_CLIENTS = 20;
    static final int PORT = 3000;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(LIMIT_OF_CLIENTS);
    private static ExecutorService pool2 = Executors.newFixedThreadPool(LIMIT_OF_CLIENTS);

    public static void main(String args[]) throws InterruptedException, IOException {


        ServerSocket  serverSocket = new ServerSocket(PORT);
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
}
