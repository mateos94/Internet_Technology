package client;

import server.AES;

import java.io.*;
import java.net.Socket;


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
                if (receiveMessage == null) {
                    break;
                }
                if (Client.startsWithIgnoreCase("###", receiveMessage)) {
                    String head = receiveMessage.split(" ", 2)[0];
                    head = head.substring(3);
                    String encryptedMessage = receiveMessage.split(" ", 2)[1];
                    receiveMessage = head + " " + AES.decrypt(encryptedMessage);
                } else if (receiveMessage.contains("*You are logged in with username ")){
                     loggedIn = true;
                } else if (receiveMessage.contains("*Logging in failed, this user already exists.")){
                    server.close(); // cut connection if user exists already
                }
                 if (receiveMessage.contains("You received a new file")){
                     byte [] b = new byte[2002];
                     //InputStream is = server.getInputStream();
                     FileInputStream fis = new FileInputStream("client/receive.txt");
                     FileOutputStream fos = new FileOutputStream("client/receivedFromServer.txt");
                     //is.read(b,0,b.length);
                     fis.read(b,0,b.length);
                     fos.write(b,0,b.length);
                 }
                System.out.println(receiveMessage); // displaying at DOS prompt
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
