import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Client
{

    public static void main(String[] args) throws Exception
    {
        Socket sock = new Socket("127.0.0.1", 3000);
        // reading from keyboard (keyRead object)
        BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
        // sending to client (pwrite object)
        OutputStream ostream = sock.getOutputStream();
        PrintWriter pwrite = new PrintWriter(ostream, true);
        ServerConnection serverConn = new ServerConnection(sock);
        System.out.println("Start the chitchat, type and press Enter key");

        String sendMessage;

        new Thread(serverConn).start();

        while(true)
        {
            sendMessage = keyRead.readLine();  // keyboard reading   clientName is at the beginning of every message
            if (sendMessage.contains("Send")){
                pwrite.println(sendMessage);       // sending to server
                pwrite.flush();                    // flush the data
                FileSender fileSender = new FileSender(sock,"client.txt");
                new Thread(fileSender).start();
            } else {
                String encryptedMessage = sendMessage;

                pwrite.println(encryptedMessage);       // sending to server
                pwrite.flush();                    // flush the data
            }
        }
    }

    public static List<String> readFileIntoList(String file) {
        List<String> lines = new ArrayList<String>();
        try {
            lines = Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8);
            }
        catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

}   