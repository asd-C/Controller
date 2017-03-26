package com.example.cc.controller.service.tools;

import android.content.Context;
import android.location.Location;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.cc.controller.R;
import com.example.cc.controller.service.admin.DeviceAdminTools;

/**
 * Created by chendehua on 2017/3/21.
 */

public class SMSHandler {

    private Context context;
    private boolean isSessionStarted;

    public SMSHandler(Context context, boolean playground) {
        this.context = context;
        isSessionStarted = playground;
    }

    public SMSHandler(Context context) {
        this.context = context;
    }

    /**
     * Handle new SMS,
     * when it contains '##LOCKSCREEN', the server locks screen
     * when it contains '##PLAYSOUND', the server plays sound
     * when it contains '##STOPSOUND', the server stop playing sound
     * */
    public String handleSMS_play(String sms) {
        if (sms.contains(context.getString(R.string.LOCK_SCREEN))) {
            DeviceAdminTools.getInstance(context).lockScreen(context);
            return "Screen locked.";
        } else if (sms.contains(context.getString(R.string.PLAY_SOUND))) {
            PlaySound.getInstance(context.getApplicationContext()).play();
            return "Started sound.";
        } else if (sms.contains(context.getString(R.string.STOP_SOUND))) {
            PlaySound.getInstance(context.getApplicationContext()).stop();
            return "Stopped sound.";
        } else if (sms.contains(context.getString(R.string.LOCATION))) {
            String latlon = null;
            LocationHandler lh = LocationHandler.getInstance(context);
            lh.registerLocationListener();

            // checking again, because it is possible
            // that fails to register Location listener
            if (lh.isListenerRegistered()) {
                Location location = lh.getLocation();

                if (location == null)
                    return "Please, try again.";

                latlon = "http://maps.google.com/maps?q=loc:"
                        + String.valueOf(location.getLatitude()) + ","
                        + String.valueOf(location.getLongitude());

                lh.unregisterLocationListener();
            }
            return latlon;
        } else {
            return "Wrong command!";
        }
    }

    /**
     * Handle new SMS,
     * when it contains '##LOCKSCREEN', the server locks screen
     * when it contains '##PLAYSOUND', the server plays sound
     * when it contains '##STOPSOUND', the server stop playing sound
     * */
    public void handleSMS(String sms) {
        if (sms.contains(context.getString(R.string.LOCK_SCREEN))) {
            DeviceAdminTools.getInstance(context).lockScreen(context);
        } else if (sms.contains(context.getString(R.string.PLAY_SOUND))) {
            PlaySound.getInstance(context.getApplicationContext()).play();
        } else if (sms.contains(context.getString(R.string.STOP_SOUND))) {
            PlaySound.getInstance(context.getApplicationContext()).stop();
        } else if (sms.contains(context.getString(R.string.LOCATION))) {
            LocationHandler lh = LocationHandler.getInstance(context);
            lh.registerLocationListener();

            // checking again, because it is possible
            // that fails to register Location listener
            if (lh.isListenerRegistered()) {
                SMSSender.getInstance(context).sendLocationBack();
            }
        }
    }

    /**
     * Start session to receive commands from requester
     * */
    public boolean startSession(SmsMessage sms) {
        if (isSessionStarted) return true;

        if (sms.getMessageBody().toLowerCase().contains("#controller")) {
            Log.i("Session", "Session opened.");
            SMSSender smsSender = SMSSender.getInstance(context);
            smsSender.setLastPhoneNumber(sms.getOriginatingAddress());
            smsSender.sendHi();
            isSessionStarted = true;
            return true;
        }

        return false;
    }

    /**
     * Called when requester send "#BYE" or when unregister the receiver.
     * Close session.
     * */
    public boolean stopSession(SmsMessage sms) {
        if (!isSessionStarted) return true;

        // sms = null, when unregister the receiver
        if (sms == null || sms.getMessageBody().contains("#BYE")) {
            Log.i("Session", "Session closed.");
            isSessionStarted = false;
            SMSSender.getInstance(context).sendBye();
            SMSSender.getInstance(context).setLastPhoneNumber(null);
            return true;
        }
        return false;
    }
}
