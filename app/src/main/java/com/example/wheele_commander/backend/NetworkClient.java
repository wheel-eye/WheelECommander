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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.net.Socket;

public final class NetworkClient extends Service implements INetworkClient {
    private static final String TAG = "NetworkClient";

    private static final String HARDWARE_IP = "172.20.118.23";

    // private static final String HARDWARE_IP = "100.90.35.131";
    private static final int HARDWARE_PORT_NUMBER = 5000;
    private final IBinder networkClientBinder = new NetworkClientBinder();
    private HandlerThread senderHandlerThread;
    private ReceiverThread receiverThread;
    private Handler senderHandler;
    private Handler receiverHandler;
    private Socket socket;
    private final MutableLiveData<Message> movementMessageData = new MutableLiveData<>();
    private final MutableLiveData<Message> batteryMessageData = new MutableLiveData<>();
    private final MutableLiveData<Message> warningMessageData = new MutableLiveData<>();

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

        // essential as Android doesn't allow socket set-up in main thread -> NetworkOnMainThreadException
        Thread initializationThread = new Thread(() -> {
            try {
                socket = new Socket(HARDWARE_IP, HARDWARE_PORT_NUMBER);

                senderHandlerThread = new HandlerThread("SenderHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
                senderHandlerThread.start();
                senderHandler = new SenderHandler(senderHandlerThread.getLooper(), socket);

                receiverHandler = new ReceiverHandler(
                        Looper.getMainLooper(),
                        movementMessageData,
                        batteryMessageData,
                        warningMessageData);
                receiverThread = new ReceiverThread(receiverHandler, socket);
                receiverThread.start();
            } catch (IOException e) {
//                throw new RuntimeException(e);
                System.out.println("Cannot connect to socket!");
            }
        });
        initializationThread.start();
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

    public LiveData<Message> getMovementMessage() {
        return movementMessageData;
    }

    public LiveData<Message> getBatteryMessage() {
        return batteryMessageData;
    }

    public LiveData<Message> getWarningMessage() {
        return warningMessageData;
    }
}
