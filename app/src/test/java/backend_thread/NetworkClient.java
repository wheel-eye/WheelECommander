package backend_thread;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
<<<<<<< Updated upstream
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
=======

public final class NetworkClient extends Service implements INetworkClient{
    private static final int PORT_NUMBER = 6969;

    private static final HandlerThread senderHT =
            new HandlerThread("SenderHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);

    private static Handler senderHandler;

    private static volatile boolean runningReceiverThread = true; // K.P - used to exit the receiverThread when onDestroy() is called 
>>>>>>> Stashed changes

public class NetworkClient extends Service implements INetworkClient{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread receiverThread = new Thread(new receiverThread());
        receiverThread.start();
<<<<<<< Updated upstream
    }

    /*
    Y.S. -
    Replaces SenderHandler and SenderThread, instead of having a looper
    to keep thread running, a new thread is created each time a message
    needs to be sent
     */
    @Override
    public void sendMessage(Message msg) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Socket s = new Socket("ip", 1000);
                PrintWriter pw = new PrintWriter(s.getOutputStream());
                pw.write("Interprets msg to the correct string");
                pw.flush();
                pw.close();
                s.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
=======

        senderHT.start();
        senderHandler = new Handler(senderHT.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        // Handle message 1
                        break;
                    case 2:
                        // Handle message 2
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        runningReceiverThread = false;
        senderHT.quitSafely();
        super.onDestroy();
    }

    public void sendMessage(Message msg){

    }

    @Override
    public void subscribe(ViewModel viewModel) {

    }

>>>>>>> Stashed changes
    }

    /*
    Y.S. - No need for ReceiverHandler when the whole thread always and only listen
     */
    static class receiverThread implements Runnable {
        Socket s;
        ServerSocket ss;
        InputStreamReader isr;
        BufferedReader br;
        String message;
        @Override
        public void run() {
            try {
                ss = new ServerSocket(1000);
                // Y.S. - Automatically exit when NetworkClient is destroyed
                while (true) {
                    s = ss.accept();
                    isr = new InputStreamReader(s.getInputStream());
                    br = new BufferedReader(isr);
                    message = String.valueOf(br.read());
                    /*
                    Y.S. -
                    The received string needs to be interpreted and sent to
                    the correct viewModel using the JSON header, a JSON
                    de-serializer will be added to the receiverThread inner
                    class
                    */
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
    Y.S. - I do not see the point of connect() and disconnect() when
    NetworkClient always runs on the background until it's destroyed,
    unless it is for actively disconnecting the NetworkClient to the hardware.
    In that case, it should be a runnable and based on a View activity, such as a
    'disconnect' button on the UI, and can be done in the UI thread instead.
     */
    @Override
    public void disconnect() {

    }

    /*
    Y.S. - I need some further explanation for subscribing, currently I don't see the point
    of having them as we are simply using the Observer pattern.
     */
    @Override
    public void subscribe(ViewModel viewModel) {

    }
}
