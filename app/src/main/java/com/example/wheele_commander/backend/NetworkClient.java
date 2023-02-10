package com.example.wheele_commander.backend;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;

import androidx.annotation.NonNull;

import com.example.wheele_commander.viewmodel.IMessageSubscriber;
import com.example.wheele_commander.viewmodel.MessageType;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NetworkClient extends Service implements INetworkClient {
    private static final String TAG = "NetworkClient";

    //    private static final String HARDWARE_IP = "172.20.118.23";
    private static final String HARDWARE_IP = "100.90.35.131";
    private static final int HARDWARE_PORT_NUMBER = 12345;
    private final IBinder networkClientBinder = new NetworkClientBinder();
    private HashMap<MessageType, List<IMessageSubscriber>> subscribedViewModels;
    private HandlerThread senderHandlerThread;
    private ReceiverThread receiverThread;
    private Handler senderHandler;
    private Handler receiverHandler;
    private Socket socket;

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return networkClientBinder;
    }

    public class NetworkClientBinder extends Binder {
        public INetworkClient getService() {
            return NetworkClient.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // essential as Android doesn't allow socket set-up in main thread -> NetworkOnMainThreadException
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                socket = new Socket(HARDWARE_IP, HARDWARE_PORT_NUMBER);

                senderHandlerThread = new HandlerThread("SenderHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
                senderHandlerThread.start();
                senderHandler = new SenderHandler(senderHandlerThread.getLooper(), socket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

//        receiverHandler = new ReceiverHandler(Looper.getMainLooper(), subscribedViewModels);
//        receiverThread = new ReceiverThread(receiverHandler, socket);
//        receiverThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        senderHandlerThread.quitSafely();
//        receiverThread.stopThread();
    }

    public void sendMessage(Message msg) {
        senderHandler.sendMessage(msg);
    }

    @Override
    public void subscribe(IMessageSubscriber viewModel, List<MessageType> subscribedTypes) {
        for (MessageType type : subscribedTypes) {
            if (type == null)
                continue;
            if (!subscribedViewModels.containsKey(type)) {
                subscribedViewModels.putIfAbsent(type, new ArrayList<>());
            }
            Objects.requireNonNull(subscribedViewModels.get(type)).add(viewModel);
        }
    }
}
