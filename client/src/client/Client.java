package client;

import server.AES;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class Client
{


    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 1337;


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
            sendMessage = keyRead.readLine();
            if (startsWithIgnoreCase("Send ", sendMessage)){
                String fileLocation = sendMessage.substring(sendMessage.lastIndexOf(" ")+1);
                File file = new File(fileLocation);
                if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(fileLocation);

                        byte b[] = new byte[2002];
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        try (InputStream is = Files.newInputStream(Paths.get(fileLocation));
                             DigestInputStream dis = new DigestInputStream(is, md))
                        {
                            dis.read(b, 0, b.length);
                        }
                        byte[] d = md.digest();
                        fis.read(d, 0, b.length);
                        OutputStream os = socket.getOutputStream();
                        os.write(d, 0, b.length);
                    }
                catch (Exception e) {
                    e.printStackTrace();
                }
                }
                else {
                    pwrite.println("Ignore message");       // sending to server
                    pwrite.flush();
                }
                pwrite.println(sendMessage);       // sending to server
                pwrite.flush();                    // flush the data
            } else if (startsWithIgnoreCase("Private ", sendMessage)) {
                if (sendMessage.length() > 8) {
                    //rest of message is message after private
                    String restOfMessage = sendMessage.substring(8);
                    if (restOfMessage.contains(" ")) {
                        //first part after split is name of receiver name, second part is content of message
                        String receiverName = restOfMessage.split(" ", 2)[0];
                        String message = restOfMessage.split(" ", 2)[1];
                        sendMessage = "PRIVATE " + receiverName + " " + AES.encrypt(message);
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

    /**
     * Check if string is starts with another string
     * @param targetOfStartWith a string that being used as ruler of checking if whole string starts with it
     * @param string whole string
     * @return boolean of weather whole string is started with another another string
     */
    public static boolean startsWithIgnoreCase(String targetOfStartWith, String string) {
        String lowerCasedTargetOfStartWith = targetOfStartWith.toLowerCase();
        String lowerCasedString = string.toLowerCase();
        return lowerCasedString.startsWith(lowerCasedTargetOfStartWith);
    }

}   