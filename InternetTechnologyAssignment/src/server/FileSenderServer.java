package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author Mateos and Haoshuang
 */
public class FileSenderServer implements Runnable{

    private Socket socket;
    private static final String PATH = "Internet technology assignment/relatedTextFiles/client.txt";
    FileSenderServer(Socket socket) throws IOException {
        this.socket = socket;
//        this.socket = new Socket("127.0.0.1", 1338);
    }
    @Override
    public void run() {
        sendFile();
    }

    /**
     * Send a file
     */
    public void sendFile(){
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
