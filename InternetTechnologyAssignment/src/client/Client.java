package client;

import server.AES;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 *
 * @author Mateos and Haoshuang
 */
public class Client
{

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 1337;
    private static Socket socket;
    private static OutputStream ostream;
    private static PrintWriter pwrite;


    public static void main(String[] args) throws Exception
    {
        socket = new Socket(SERVER_IP, SERVER_PORT);
        // reading from keyboard (keyRead object)
        BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
        // sending to client (pwrite object)
        ostream = socket.getOutputStream();
        pwrite = new PrintWriter(ostream, true);
        ServerConnection serverConn = new ServerConnection(socket);
        System.out.println("Start the chitchat, type and press Enter key");

        String sendMessage;


        new Thread(serverConn).start();
        while(true)
        {
            sendMessage = keyRead.readLine();
            if (startsWithIgnoreCase("Send ", sendMessage)){
                String fileLocation = sendMessage.substring(sendMessage.lastIndexOf(" ") + 1);
                File file = new File(fileLocation);
                String textBeforeFileLocation = sendMessage.substring(0, sendMessage.lastIndexOf(" ") + 1);
                if (file.exists()) {
                    File targetFile = new File(fileLocation);
                    String fileName = fileLocation.substring(fileLocation.lastIndexOf("/") + 1);
                    sendMessage = textBeforeFileLocation + fileName + " " + Base64EncoderAndDecoder.encodeFileToBase64(targetFile);
                }
                else {
                    printAndFlush("Ignore message");
                }
                printAndFlush(sendMessage);
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
                printAndFlush(sendMessage);
            } else {
                printAndFlush(sendMessage);
            }

        }
    }

    /**
     * Print and flush a message to server
     * @param sendMessage The message that will be printed and flushed
     */
    public static void printAndFlush(String sendMessage){
        pwrite.println(sendMessage);       // sending to server
        pwrite.flush();                    // flush the data
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