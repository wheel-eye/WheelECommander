package com.example.wheele_commander.backend_thread;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.HandlerThread;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.example.wheele_commander.deserializer.Deserializer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public final class NetworkClient extends Service implements INetworkClient{

    /*
    Y.S.- It should be List<ViewModel> right? otherwise the subscribe method should take
          IMessageSubscriber as inputs.
     */
    private List<ViewModel> subscribedViewModels = new ArrayList<ViewModel>();

    private static final int PORT_NUMBER = 6969;

    private static final HandlerThread senderHT =
            new HandlerThread("SenderHandlerThread", Process.THREAD_PRIORITY_BACKGROUND){
        @Override
        protected void onLooperPrepared(){
            this.Socket socket = new Socket(PORT_NUMBER);
        }
    };

    private static Handler senderHandler;

    /*
    private static final HandlerThread receiverHT =
            new HandlerThread("ReceiverHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
    */

    //private static Handler receiverHandler;

    private static volatile boolean runningReceiverThread = true; // K.P - used to exit the receiverThread when onDestroy() is called

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

        /*
        receiverHT.start();
        receiverHandler = new Handler(receiverHT.getLooper()) {
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
         */
    }

    @Override
    public void onDestroy() {
        runningReceiverThread = false;
        senderHT.quitSafely();
        //receiverHT.quitSafely();
        super.onDestroy();
    }

    public void sendMessage(Message msg){
        senderHandler.sendMessage(msg);
    }

    /*
    Y.S.- See subscribedViewModels
     */
    @Override
    public void subscribe(ViewModel viewModel) {
        subscribedViewModels.add(viewModel);
    }

    private static final class receiverThread implements Runnable {
        Socket s;
        ServerSocket ss;
        InputStreamReader isr;
        BufferedReader br;
        byte[] message;
        @Override
        public void run() {
            try {
                ss = new ServerSocket(PORT_NUMBER);
                while (runningReceiverThread) {
                    s = ss.accept();
                    InputStream stream = s.getInputStream();
                    byte[] data = readToByteArray(stream);
                    /*
                    Deserialize data and send to viewmodel
                     */
                }
                ss.close();
                }
            catch (IOException e) {
                        throw new RuntimeException(e);
            }
        }

        private byte[] readToByteArray(InputStream stream) throws IOException {
            int nRead;
            byte[] data = new byte[4];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            while ((nRead = stream.read(data, 0, data.length)) != 0) {
                System.out.println("here " + nRead);
                baos.write(data, 0, nRead);
            }

            baos.flush();
            return baos.toByteArray();
        }

    }
}
