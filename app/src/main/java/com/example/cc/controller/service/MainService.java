package com.example.cc.controller.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.cc.controller.R;
import com.example.cc.controller.activity.MainActivity;
import com.example.cc.controller.service.admin.DeviceAdminTools;
import com.example.cc.controller.service.command.ServiceCommands;
import com.example.cc.controller.service.tools.LocationHandler;
import com.example.cc.controller.service.tools.PlaySound;

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
                    showNotification();
                    registerMessageReceiver();
                    break;

                // Request the server turn message receiver ff
                // the server stops monitoring incoming sms
                case ServiceCommands.MSG_STOP_RECEIVING_SMS:
                    cancelNotification();
                    unregisterMessageReceiver();
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    private Notification notification;
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
                    .setContentTitle("hi")
                    .setContentText("hello")
                    .setSmallIcon(R.mipmap.ic_launcher)
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
        if (isRegisteredReceiver == false) {
            this.registerReceiver(messageReceiver,
                    new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
            isRegisteredReceiver = true;
            Log.i("MessageReceiver", "registering messageReceiver.");
        }
    }

    /**
     * Unregister a message receiver to the service
     * */
    private void unregisterMessageReceiver() {
        if (isRegisteredReceiver == true) {
            this.unregisterReceiver(messageReceiver);
            isRegisteredReceiver = false;
        }
    }

    // A MessageReceiver instance's reference, to register or unregister
    private MessageReceiver messageReceiver = new MessageReceiver();

    // MessageReceiver
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            lastPhoneNumber = messages[0].getOriginatingAddress();
            String sms = messages[0].getMessageBody();
            handleSMS(sms);
            Log.i("MessageReceiver,SMS", sms);
        }
    }

    /**
     * Handle new SMS,
     * when it contains '##LOCKSCREEN', the server locks screen
     * when it contains '##PLAYSOUND', the server plays sound
     * when it contains '##STOPSOUND', the server stop playing sound
     * */
    private void handleSMS(String sms) {
        if (sms.contains("##LOCKSCREEN")) {
            DeviceAdminTools.getInstance(this).lockScreen(this);
        } else if (sms.contains("##PLAYSOUND")) {
            PlaySound.getInstance(getApplicationContext()).play();
        } else if (sms.contains("##STOPSOUND")) {
            PlaySound.getInstance(getApplicationContext()).stop();
        } else if (sms.contains("##LOCATION")) {
            LocationHandler lh = LocationHandler.getInstance(this);
            // avoid register location listener many times
            if (lh.isListenerRegistered() == false) {
                lh.registerLocationListener();
            }
            // checking again, because it is possible
            // that fails to register Location listener
            if (lh.isListenerRegistered()) {
                sendSMSBack();
            }
        }
//        } else if (sms.contains("##STOPLOCATION")) {
//            LocationHandler lh = LocationHandler.getInstance(this);
//            if (lh.isListenerRegistered() == true) {
//                LocationHandler.getInstance(this).unregisterLocationListener();
//            }
        notifyClientNewIncomingSMS(sms);
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

    private boolean tryToSendLocationBack() {
        LocationHandler locationHandler = LocationHandler.getInstance(this);
        Location location = locationHandler.getLocation();
        if (location != null && lastPhoneNumber != null) {
            String latlon = "http://maps.google.com/maps?q=loc:"
                    + String.valueOf(location.getLatitude()) + ","
                    + String.valueOf(location.getLongitude());

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(
                    lastPhoneNumber,
                    null,
                    latlon,
                    null,
                    null);
            Log.i("To " + lastPhoneNumber, latlon);
            return true;
        }
        return false;
    }

    private String lastPhoneNumber;

    private Runnable smsSender = new Runnable() {
        @Override
        public void run() {
            if (!tryToSendLocationBack()) {
                handler.postDelayed(smsSender, 5000);
            } else {
                LocationHandler.getInstance(MainService.this).unregisterLocationListener();
            }
        }
    };

    private Handler handler = new Handler();

    private void sendSMSBack() {
        handler.post(smsSender);
    }

}
