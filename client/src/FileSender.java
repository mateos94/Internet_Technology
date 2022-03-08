import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class FileSender implements Runnable{
    private Socket server;
    private String filePath;
    private FileInputStream fileInputStream;
    private OutputStream ostream;
    public FileSender(Socket socket, String filePath) throws IOException {
        this.server = socket;
        this.filePath = filePath;
        this.fileInputStream = new FileInputStream(filePath);
        this.ostream = server.getOutputStream();
    }

    @Override
    public void run() {
        byte b[] = new byte[2024];
        try {
            fileInputStream.read(b,0,b.length);
            ostream.write(b,0,b.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
