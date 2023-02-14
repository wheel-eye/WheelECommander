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
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wheele_commander.deserializer.Data;
import com.example.wheele_commander.deserializer.Warning;
import com.example.wheele_commander.viewmodel.MessageType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class NetworkClient extends Service implements INetworkClient {
    private static final String TAG = "NetworkClient";
    private static final String HARDWARE_IP = "172.20.118.23";
    //    private static final String HARDWARE_IP = "100.90.35.131";
    private static final int HARDWARE_PORT_NUMBER = 5000;
    private final IBinder networkClientBinder = new NetworkClientBinder();
    private HandlerThread senderHandlerThread;
    private ReceiverThread receiverThread;
    private Handler senderHandler;
    private Handler receiverHandler;
    private Socket socket;
    private MutableLiveData<Message> movementMessageData;
    private MutableLiveData<Message> batteryMessageData;
    private MutableLiveData<Message> warningMessageData;

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
        movementMessageData = new MutableLiveData<>();
        batteryMessageData = new MutableLiveData<>();
        warningMessageData = new MutableLiveData<>();
        establishConnection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO: Close socket
		closeSocket();
        senderHandlerThread.quitSafely();
        receiverThread.stopThread();
    }

    public void sendMessage(Message msg) {
        if (socket != null && !socket.isConnected())
			new Thread(this::connectSocket).start();		
		else
            senderHandler.sendMessage(msg);
    }

    @Override
    public void establishConnection() {
        // essential as Android doesn't allow socket set-up in main thread -> NetworkOnMainThreadException
        Thread connectionThread = new Thread(() -> {
			connectSocket();
			
			senderHandlerThread = new HandlerThread("SenderHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
			senderHandlerThread.start();
			senderHandler = new SenderHandler(senderHandlerThread.getLooper(), socket);
			
			receiverHandler = new ReceiverHandler(Looper.getMainLooper());
            receiverThread = new ReceiverThread(receiverHandler, socket);
            receiverThread.start();
        });
        connectionThread.start();
    }
	
	private void connectSocket() {
		boolean connected = false;
		while (!connected) {
        	try {
            	socket = new Socket();
				socket.connect(new InetSocketAddress(HARDWARE_IP, HARDWARE_PORT_NUMBER), 2000);
                socket.setKeepAlive(true);
                connected = true;
                Log.d(TAG, "establishConnection: Connected to " + HARDWARE_IP + ":" + HARDWARE_PORT_NUMBER);
			} catch (IOException e) {
            	Log.d(TAG, "establishConnection: Connection failed, reconnecting in 2 sec...");
				SystemClock.sleep(2000);
			}
		}
	}
	
	private void closeSocket() {
		if (socket.isConnected()) {
			try {
				socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

    private class ReceiverHandler extends Handler {
        public ReceiverHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0) { // DATA
                Data data = (Data) msg.obj;

                if (data.getSpeed() != null) {
                    Message velocityMessage = new Message();
                    velocityMessage.what = MessageType.VELOCITY_UPDATE.ordinal();
                    velocityMessage.arg1 = data.getSpeed();
                    movementMessageData.postValue(velocityMessage);
                }
                if (data.getBattery() != null) {
                    Message batteryMessage = new Message();
                    batteryMessage.what = MessageType.BATTERY_UPDATE.ordinal();
                    batteryMessage.arg1 = data.getBattery();
                    batteryMessageData.postValue(batteryMessage);
                }
            } else if (msg.what == 1) { // WARNING
                Message warningMessage = new Message();
                warningMessage.what = MessageType.WARNING_MESSAGE.ordinal();
                warningMessage.arg1 = ((Warning) msg.obj).getCode();
                warningMessageData.postValue(warningMessage);
            }
        }
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
