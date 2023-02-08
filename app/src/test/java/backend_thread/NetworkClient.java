package backend_thread;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.HandlerThread;
import android.os.Process;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public static final class NetworkClient extends Service implements INetworkClient{
    protected static final int PORT_NUMBER = 1000;

    protected static final Socket socket = new Socket("ip", PORT_NUMBER);
    
    private static final HandlerThread senderThread = new HandlerThread("senderThread", Process.THREAD_PRIORITY_MORE_FAVORABLE);
    private static final Handler senderHandler;

    private static volatile boolean runningReceiverThread = true; // K.P - used to exit the receiverThread when onDestroy() is called 

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected final void onCreate() {
        super.onCreate();

        Thread receiverThread = new Thread(new receiverThread());
        receiverThread.start();

        senderThread.start();
        senderHandler = new Handler(senderThread.get_looper());
    }

    @Override
    protected final void onDestroy() {
        runningReceiverThread = false;
        senderThread.quitSafely();
        super.onDestroy();
    }

    /* 
    K.P. - Removed executors and replaced as Runnable for a persistant thread,
    we may need to replace the standard looper queue with a fifo circular queue
    in the persistant thread.

    Note: using anonymous Runnable may cause persistance issues
    
    Y.S. -
    Replaces SenderHandler and SenderThread, instead of having a looper
    to keep thread running, a new thread is created each time a message
    needs to be sent
     */
    public static final void sendMessage(Message msg){
        senderHandler.post(new Runnable() {
            @Override
            public run() {
                try {
                    PrintWriter pw = new PrintWriter(NetworkClient.socket.getOutputStream());
                    pw.write("Interprets msg to the correct string");
                    pw.flush();
                    pw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };
        });
    }
    /*
    Y.S. -
    Replaces SenderHandler and SenderThread, instead of having a looper
    to keep thread running, a new thread is created each time a message
    needs to be sent
     */

    /*
    Y.S. - No need for ReceiverHandler when the whole thread always and only listen
     */
    private static final class receiverThread implements Runnable {
        Socket s;
        ServerSocket ss;
        InputStreamReader isr;
        BufferedReader br;
        String message;
        @Override
        public final void run() {
            try {
                ss = new ServerSocket(PORT_NUMBER);
                // Y.S. - Automatically exit when NetworkClient is destroyed
                // K.P. - Wouldn't it just keep running until socket error?
                // included runningReceiverThread to close
                while (runningReceiverThread) {
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
                s.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
    Y.S. - I need some further explanation for subscribing, currently I don't see the point
    of having them as we are simply using the Observer pattern.
     */
    @Override
    public void subscribe(ViewModel viewModel) {

    }

    @Override
    public void unsubscribe(ViewModel viewModel) {

    }
}
