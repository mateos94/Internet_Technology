package client;


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Client
{


    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 1337;
    private static final String PATH = "client/client.txt";


    public static void main(String[] args) throws Exception
    {
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        // reading from keyboard (keyRead object)
        BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
        // sending to client (pwrite object)
        OutputStream ostream = socket.getOutputStream();
        PrintWriter pwrite = new PrintWriter(ostream, true);
        ServerConnection serverConn = new ServerConnection(socket);
        System.out.println("Start the chitchat, type and press Enter key");

        String sendMessage;


        new Thread(serverConn).start();
        while(true)
        {
            sendMessage = keyRead.readLine();  // keyboard reading   clientName is at the beginning of every message
            if (containsIgnoreCase(sendMessage,"Send")){
                System.out.println(sendMessage);
                try {
                    FileInputStream fis = new FileInputStream(PATH);
                    byte b[] = new byte[2002];
                    fis.read(b, 0, b.length);
                    OutputStream os = socket.getOutputStream();
                    os.write(b, 0, b.length);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                pwrite.println(sendMessage);       // sending to server
                pwrite.flush();                    // flush the data

            } else {
                String encryptedMessage = sendMessage;

                pwrite.println(encryptedMessage);       // sending to server
                pwrite.flush();                    // flush the data
            }
        }
    }

    public static boolean containsIgnoreCase(String str, String searchStr)     {
        if(str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }

}   