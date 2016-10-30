package com.example.cc.controller.service;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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

import com.example.cc.controller.service.admin.DeviceAdminHandler;
import com.example.cc.controller.service.admin.DeviceAdminTools;
import com.example.cc.controller.service.command.ServiceCommands;
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
                    break;

                // Notifying the client is off
                case ServiceCommands.MSG_DISCONNECT:
                    clientMessenger = null;
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerMessageReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterMessageReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    /**
     * Register a message receiver to the service
     * */
    private void registerMessageReceiver() {
        this.registerReceiver(messageReceiver,
                new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));

        Log.i("MessageReceiver", "registering messageReceiver.");
    }

    /**
     * Unregister a message receiver to the service
     * */
    private void unregisterMessageReceiver() {
        this.unregisterReceiver(messageReceiver);
    }

    // A MessageReceiver instance's reference, to register or unregister
    private MessageReceiver messageReceiver = new MessageReceiver();

    // MessageReceiver
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            String sms = messages[0].getMessageBody();
            handleSMS(sms);
            Log.i("MessageReceiver,SMS", sms);
        }
    }

    private void handleSMS(String sms) {
        if (sms.contains("##LOCKSCREEN")) {
            DeviceAdminTools.getInstance(this).lockScreen(this);
        } else if (sms.contains("##PLAYSOUND")) {
            PlaySound.getInstance(getApplicationContext()).play();
        } else if (sms.contains("##STOPSOUND")) {
            PlaySound.getInstance(getApplicationContext()).stop();
        }
        notifyClientNewIncomingSMS(sms);
    }

    /**
     * Send the content of sms to the client if the client is on
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
