package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class FileSenderServer implements Runnable{

    private Socket socket;
    private static final String PATH = "client/receive.txt";
    public FileSenderServer(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        sendFile();
    }

    private void sendFile(){
        try {
            FileInputStream fis = new FileInputStream(PATH);
            byte b[] = new byte[2002];
            fis.read(b, 0, b.length);
            OutputStream os = socket.getOutputStream();
            os.write(b, 0, b.length);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
