package com.example.wheele_commander.backend_thread;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public final class NetworkClient extends Service implements INetworkClient {
    private List<IMessageSubscriber> subscribedViewModels;

    private Socket socket;

    private int HARDWARE_PORT_NUMBER;

    private HandlerThread senderHT;

    private Handler senderHandler;

    private Thread receiverThread;

    private Handler receiverHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            socket = new Socket("IP", 2000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HARDWARE_PORT_NUMBER = 6969;

        senderHT = new HandlerThread("SenderHandlerThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        senderHandler = new SenderHandler(senderHT.getLooper(), socket);
        senderHT.start();

        receiverHandler = new ReceiverHandler(Looper.getMainLooper(), subscribedViewModels);
        receiverThread = new Thread(new ReceiverThread(socket, receiverHandler));
        receiverThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        senderHT.quitSafely();
    }

    public void sendMessage(Message msg) {
        senderHandler.sendMessage(msg);
    }

    @Override
    public void subscribe(IMessageSubscriber viewModel) {
        subscribedViewModels.add(viewModel);
    }
}
