package com.example.wheele_commander.viewmodel;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.wheele_commander.R;
import com.example.wheele_commander.backend.NetworkClient;
import com.example.wheele_commander.model.WarningType;

public class WarningViewModel extends AbstractViewModel {
    private static final String TAG = "WarningViewModel";
    private static final String CHANNEL_ID = "WarningChannel";
    private final NotificationManager notificationManager;

    public WarningViewModel(@NonNull Application application) {
        super(application);
        notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Warning", NotificationManager.IMPORTANCE_HIGH);

        if (notificationManager != null)
            notificationManager.createNotificationChannel(channel);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: Connected to service");
                NetworkClient.NetworkClientBinder binder = (NetworkClient.NetworkClientBinder) iBinder;
                networkClient = binder.getService();
                networkClient.setWarningViewModel(WarningViewModel.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected: Service disconnected");
            }
        };
    }

    @Override
    public void handleMessage(Message msg) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        WarningType warningType = WarningType.getWarningTypeFromCode(msg.arg1);

        if (warningType == null)
            return;

        switch (warningType) {
            case EMERGENCY_STOP:
                builder.setContentTitle("Emergency Stop")
                        .setContentText("The system has detected an emergency stop situation.");
                break;
            case DISABLE_COMPONENT:
                builder.setContentTitle("Component Disabled")
                        .setContentText("A component has been disabled.");
                break;
            case COMPONENT_CONNECTION_LOST:
                builder.setContentTitle("Component Connection Lost")
                        .setContentText("A connection to a component has been lost.");
                break;
            case COLLISION_WARNING:
                builder.setContentTitle("Collision Warning")
                        .setContentText("A collision is imminent. Take action to avoid it.");
                break;
            case BATTERY_LOW:
                builder.setContentTitle("Battery Low")
                        .setContentText("The battery level is low. Connect to a power source.");
                break;
            case BATTERY_CRITICAL:
                builder.setContentTitle("Battery Critical")
                        .setContentText("The battery level is critical. Connect to a power source immediately.");
                break;
            case SPEED_WARNING:
                builder.setContentTitle("Speed Warning")
                        .setContentText("The speed limit has been exceeded. Slow down.");
                break;
        }

        notificationManager.notify(msg.arg1, builder.build());
    }
}