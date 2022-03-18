package client;

import java.io.*;
import java.net.Socket;

public class Client
{


    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 1337;
    private static final String PATH = "client/client/client.txt";


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
            if (startsWithIgnoreCase("Send ", sendMessage)){
                String fileLocation = sendMessage.substring(sendMessage.lastIndexOf(" ")+1);
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
            } else if (startsWithIgnoreCase("Private ", sendMessage)) {
                if (sendMessage.length() > 8) {
                    String restOfMessage = sendMessage.substring(8);
                    if (restOfMessage.contains(" ")) {
                        String receiverName = restOfMessage.split(" ", 2)[0];
                        String message = restOfMessage.split(" ", 2)[1];
                        String encryptedMessage = message;
                        sendMessage = "PRIVATE " + receiverName + " " + Crypto.encrypt(encryptedMessage);
                    }
                }
                pwrite.println(sendMessage);       // sending to server
                pwrite.flush();                    // flush the data
            } else {
                pwrite.println(sendMessage);       // sending to server
                pwrite.flush();                    // flush the data
            }

        }
    }

    public static boolean startsWithIgnoreCase(String targetOfStartWith, String string) {
        String lowerCasedTargetOfStartWith = targetOfStartWith.toLowerCase();
        String lowerCasedString = string.toLowerCase();
        return lowerCasedString.startsWith(lowerCasedTargetOfStartWith);
    }

}   