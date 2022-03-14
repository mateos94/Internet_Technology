package client;

import java.io.*;
import java.net.Socket;

public class ServerConnection implements Runnable{
    private Socket server;
    private BufferedReader in;
    private boolean loggedIn = false;

    public boolean isLoggedIn() {
        return loggedIn;
    }

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
                if(receiveMessage.contains("*You are logged in with username ")){
                     loggedIn = true;
                 } else if (receiveMessage.contains("*Logging in failed, this user already exists.")){
                    server.close(); // cut connection if user exists already
                }
                 if (receiveMessage.contains("You received a new file")){
                     byte [] b = new byte[20002];
                     InputStream is = server.getInputStream();
                     FileOutputStream fos = new FileOutputStream("client/receive.txt");
                     is.read(b,0,b.length);
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