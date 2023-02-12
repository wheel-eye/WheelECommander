package com.example.wheele_commander.backend;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wheele_commander.viewmodel.AbstractViewModel;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NetworkClient extends Service implements INetworkClient {
    private static final String TAG = "NetworkClient";

    //    private static final String HARDWARE_IP = "172.20.118.23";
    private static final String HARDWARE_IP = "100.90.35.131";
    private static final int HARDWARE_PORT_NUMBER = 5000;
    private final IBinder networkClientBinder = new NetworkClientBinder();
    private HandlerThread senderHandlerThread;
    private ReceiverThread receiverThread;
    private Handler senderHandler;
    private Handler receiverHandler;
    private Socket socket;
    private AbstractViewModel movementStatisticsViewModel;
    private AbstractViewModel batteryViewModel;
    private AbstractViewModel warningViewModel;

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return networkClientBinder;
    }

    public class NetworkClientBinder extends Binder {
        public NetworkClient getService() {
            return NetworkClient.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Network Client Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Network Client Service started");
        // essential as Android doesn't allow socket set-up in main thread -> NetworkOnMainThreadException
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                socket = new Socket(HARDWARE_IP, HARDWARE_PORT_NUMBER);

                senderHandlerThread = new HandlerThread("SenderHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
                senderHandlerThread.start();
                senderHandler = new SenderHandler(senderHandlerThread.getLooper(), socket);

                /* TODO: This is a short term fix! Service is started sooner,
                    than the view models get to bind, then these references will be null
                 */
                while (movementStatisticsViewModel == null || batteryViewModel == null || warningViewModel == null) {
                }

                System.out.println(warningViewModel);
                receiverHandler = new ReceiverHandler(
                        Looper.getMainLooper(),
                        movementStatisticsViewModel,
                        batteryViewModel,
                        warningViewModel);
                receiverThread = new ReceiverThread(receiverHandler, socket);
                receiverThread.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        senderHandlerThread.quitSafely();
        receiverThread.stopThread();
    }

    public void sendMessage(Message msg) {
        senderHandler.sendMessage(msg);
    }

    public void setMovementStatisticsViewModel(AbstractViewModel movementStatisticsViewModel) {
        this.movementStatisticsViewModel = movementStatisticsViewModel;
    }

    public void setBatteryViewModel(AbstractViewModel batteryViewModel) {
        this.batteryViewModel = batteryViewModel;
    }

    public void setWarningViewModel(AbstractViewModel warningViewModel) {
        System.out.println("Setting warningViewModel to: " + warningViewModel);
        this.warningViewModel = warningViewModel;
    }
}
