import java.io.*;
import java.net.Socket;

public class ServerConnection implements Runnable{
    private Socket server;
    private BufferedReader in;
    private String clientName = "0 ";
    private boolean loggedIn = false;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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
                receiveMessage = AES.decrypt(receiveMessage);
                if(receiveMessage.contains("*You are logged in with username ")){
                     loggedIn = true;
                 } else if (receiveMessage.contains("*Logging in failed, this user already exists.")){
                    server.close(); // cut connection if user exists already
                }
                 if (receiveMessage.contains("You received a new file")){
                    byte b[] = new byte[2024];
                    InputStream inputStream = server.getInputStream();
                    FileOutputStream fileOutputStream = new FileOutputStream("client/receive.txt");
                    inputStream.read(b,0,b.length);
                    fileOutputStream.write(b,0,b.length);
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
