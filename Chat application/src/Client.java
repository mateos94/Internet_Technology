import java.io.*;
import java.net.Socket;

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
            sendMessage = (serverConn.getClientName() +keyRead.readLine());  // keyboard reading   clientName is at the beginning of every message
            if (sendMessage.contains("Send")){
                sendMessage = AES.encrypt(sendMessage);
                pwrite.println(sendMessage);       // sending to server
                pwrite.flush();                    // flush the data
                FileSender fileSender = new FileSender(sock,"client.txt");
                new Thread(fileSender).start();
            } else {
                String encryptedMessage = AES.encrypt(sendMessage);
                if (sendMessage.toLowerCase().contains("login") && serverConn.getClientName().equals("0 ")) {
                    serverConn.setClientName(sendMessage.substring(8) + " ");
                }else if(sendMessage.contains("Quit")){
                    serverConn.setClientName("0 ");
                }
                pwrite.println(encryptedMessage);       // sending to server
                pwrite.flush();                    // flush the data
            }
        }
    }
}   