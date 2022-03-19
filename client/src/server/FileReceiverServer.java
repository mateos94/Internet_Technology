package server;

import java.io.*;
import java.net.Socket;

public class FileReceiverServer implements Runnable {
    private Socket socket;
    public FileReceiverServer(Socket socket) {
        this.socket = socket;
    }

    /**
     * Receive a file
     */
    public void receive(){
        try {
            byte [] b = new byte[2002];
            InputStream is = socket.getInputStream();
            is.read(b,0,b.length);
            FileOutputStream fos = new FileOutputStream("client/client/receive.txt");
            fos.write(b,0,b.length);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
            receive();
    }
}
