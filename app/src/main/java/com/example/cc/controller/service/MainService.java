package com.example.cc.controller.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.cc.controller.R;
import com.example.cc.controller.activity.MainActivity;
import com.example.cc.controller.service.admin.DeviceAdminTools;
import com.example.cc.controller.service.command.ServiceCommands;
import com.example.cc.controller.service.tools.LocationHandler;
import com.example.cc.controller.service.tools.PlaySound;
import com.example.cc.controller.service.tools.SMSHandler;
import com.example.cc.controller.service.tools.SMSSender;
import com.example.cc.controller.service.tools.StateManager;

public class MainService extends Service {

    private Messenger serviceMessenger = new Messenger(new IncomingHandler());
    private Messenger clientMessenger;

    // Handles the incoming command from the client side
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what; // which command
            Message message = new Message();
            switch (what) {
                // Notifying the client is on
                case ServiceCommands.MSG_CONNECT:
                    clientMessenger = msg.replyTo;
                    if (isRegisteredReceiver) {
                        message.what = ServiceCommands.MSG_START_RECEIVING_SMS;
                    } else {
                        message.what = ServiceCommands.MSG_STOP_RECEIVING_SMS;
                    }
                    try {
                        clientMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                // Notifying the client is off
                case ServiceCommands.MSG_DISCONNECT:
                    clientMessenger = null;
                    break;

                // Request the server turn message receiver on
                // the server starts monitoring incoming sms
                case ServiceCommands.MSG_START_RECEIVING_SMS:
                    registerMessageReceiver();
                    break;

                // Request the server turn message receiver ff
                // the server stops monitoring incoming sms
                case ServiceCommands.MSG_STOP_RECEIVING_SMS:
                    unregisterMessageReceiver();
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private boolean saveState(int state) {
        return StateManager.getInstance(this).setLastState(state);
    }

    private int getState() {
        return StateManager.getInstance(this).getLastState();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        smsHandler = new SMSHandler(this);

        int state = getState();
        if (state == StateManager.STATE_ENABLED) {
            registerMessageReceiver();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    private Notification notification;
    private SMSHandler smsHandler;
    private static final int NOTIFICATION_ID = 1;

    /**
     * Show notification and set the service as foreground
     * When the notification is clicked, user will go to MainActivity.
     * */
    private void showNotification() {
        if (notification == null) {
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class),
                    0);
            notification = new Notification.Builder(this)
                    .setContentTitle("Controller")
                    .setContentText("Hey human! Feed me a delicious sms!")
                    .setSmallIcon(R.drawable.ic_stat_hi)
                    .setContentIntent(contentIntent)
                    .build();
        }
        // Set the service as foreground service
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * Cancel the notification and set the service back to background service
     * */
    private void cancelNotification() {
        stopForeground(true);
    }

    private boolean isRegisteredReceiver;

    /**
     * Register a message receiver to the service
     * */
    private void registerMessageReceiver() {
        if (!isRegisteredReceiver) {
            this.registerReceiver(messageReceiver,
                    new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
            isRegisteredReceiver = true;
            showNotification();
            saveState(StateManager.STATE_ENABLED);
            Log.i("MessageReceiver", "registering messageReceiver.");
        }
    }

    /**
     * Unregister a message receiver to the service
     * */
    private void unregisterMessageReceiver() {
        if (isRegisteredReceiver) {
            this.unregisterReceiver(messageReceiver);
            isRegisteredReceiver = false;
            saveState(StateManager.STATE_DISABLED);
            cancelNotification();
            smsHandler.stopSession(null);
            Log.i("MessageReceiver", "unregistering messageReceiver.");
        }
    }

    // A MessageReceiver instance's reference, to register or unregister
    private MessageReceiver messageReceiver = new MessageReceiver();

    // MessageReceiver
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            String sms = messages[0].getMessageBody();
            if (smsHandler.startSession(messages[0])) {
                if (messages[0].getOriginatingAddress()
                        .equalsIgnoreCase(SMSSender.getInstance(context)
                                .getLastPhoneNumber())) {
                    smsHandler.handleSMS(sms);
                    notifyClientNewIncomingSMS(sms);
                    smsHandler.stopSession(messages[0]);
                }
            }
            Log.i("MessageReceiver,SMS", sms);
        }
    }
    
    /**
     * Send the content of sms to the client side if the client is on
     * */
    private void notifyClientNewIncomingSMS(String sms) {
        if (clientMessenger != null) {
            Bundle bundle = new Bundle();
            bundle.putString("sms", sms);

            Message msg = Message.obtain();
            msg.what = ServiceCommands.MSG_NEW_SMS;
            msg.obj = bundle;
            try {
                clientMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
