package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileReceiverServer implements Runnable {
    private Socket socket;
    public FileReceiverServer(Socket socket) {
        this.socket = socket;
    }

    public void receive() {
        try {
            byte [] b = new byte[20002];
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream("client/receive.txt");
            is.read(b,0,b.length);
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
