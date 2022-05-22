package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A thread class that is used for sending Ping message
 *
 * @author Mateos and Haoshuang
 */
public class TimerThread extends Thread{

    private static final int PING_GAP_IN_MILLISECONDS = 5000;

    private static Timer timer;
    protected Socket socket;
    private PrintWriter out;
    private ClientHandler clientThread;

    public TimerThread(Socket client, ClientHandler clientThread) throws IOException {
        this.clientThread = clientThread;
        this.socket = client;
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {

        //create timer task to increment counter
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                clientThread.setCounter();
            }
        };

        //create thread to print counter value
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                while (true) {
                        if (clientThread.getCounter() == 3) {
                            out.println("Ping");
                            clientThread.setCounter(); //increase counter by 1 so it doesn't ping multiple times
                        }
                            else if (clientThread.getCounter() == 10 && clientThread.getResponseMessage().equals("")) { //if no response within 10 seconds terminate connection
                                out.println("Timer has reached 10 connection will now be terminated");
                                clientThread.getUserOfClientHandler().changeLoginStatus();
                                socket.close();
                                timer.cancel();//end the timer
                                break;//end this loop
                            }

                        Thread.sleep(1000);
                    }
                } catch (InterruptedException | IOException ex) {
                    ex.printStackTrace();
                }finally {
                    out.close();
                }
            }
        });

        timer = new Timer("MyTimer");//create a new timer
        timer.scheduleAtFixedRate(timerTask, 50, PING_GAP_IN_MILLISECONDS);//start timer in 50ms to increment counter

        t.start();//start thread to display counter
    }
}