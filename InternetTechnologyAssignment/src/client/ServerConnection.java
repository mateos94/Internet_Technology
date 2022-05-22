package client;

import server.AES;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;

/**
 * This class handles the message from server
 *
 * @author Mateos and Haoshuang
 */
public class ServerConnection implements Runnable{
    private Socket server;
    private BufferedReader in;
    private boolean loggedIn = false;

    public ServerConnection(Socket socket) throws IOException {
        server = socket;
        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
    }

    @Override
    public void run() {
        try {
            while(true){
                String receiveMessage = in.readLine();
                if (!receiveMessage.equals("Connection duration extended")) {
                    if (receiveMessage.equals("Ping")) {
                        Client.printAndFlush("Pong");
                    } else {
                        if (receiveMessage == null) {
                            break;
                        }
                        if (Client.startsWithIgnoreCase("###", receiveMessage)) {
                            String head = receiveMessage.split(" ", 2)[0];
                            head = head.substring(3);
                            String encryptedMessage = receiveMessage.split(" ", 2)[1];
                            receiveMessage = head + " " + AES.decrypt(encryptedMessage);
                        } if (receiveMessage.startsWith("You received a new file")) {
                            String senderNameAndFileNameAndBase64String = receiveMessage.substring(24);
                            String senderName = senderNameAndFileNameAndBase64String.substring(0, senderNameAndFileNameAndBase64String.indexOf(" "));
                            String fileNameAndBase64String = senderNameAndFileNameAndBase64String.substring(senderNameAndFileNameAndBase64String.indexOf(" ") + 1);
                            int indexOfFirstBlank = fileNameAndBase64String.indexOf(" ");
                            String fileName = fileNameAndBase64String.substring(0, indexOfFirstBlank);
                            String base64String = fileNameAndBase64String.substring(indexOfFirstBlank + 1);

                            int lastDotOfFileName = fileName.lastIndexOf(".");
                            String dotWithFileType = fileName.substring(lastDotOfFileName);

                            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                            Base64EncoderAndDecoder.decodeBase64ToFile(base64String,"receivedFile" + timestamp.getTime() + dotWithFileType);

                            receiveMessage = "You received a new file " + fileName + " from " + senderName + ", now named as " + "receivedFile" + timestamp.getTime() + dotWithFileType;
                        }
                        System.out.println(receiveMessage); // displaying at DOS prompt
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
