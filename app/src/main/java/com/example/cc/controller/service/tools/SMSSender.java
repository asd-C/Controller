package com.example.cc.controller.service.tools;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Created by chendehua on 16/11/3.
 */

public class SMSSender {

    private static SMSSender instance;

    private SMSSender(Context context) {
        this.context = context;
    }

    // TODO avoid use context in static instante
    private Context context;
    private String lastPhoneNumber;

    public synchronized static SMSSender getInstance(Context context) {
        if (instance == null) {
            instance = new SMSSender(context);
        }
        return instance;
    }

    public synchronized String getLastPhoneNumber() {
        return lastPhoneNumber;
    }

    public synchronized void setLastPhoneNumber(String lastPhoneNumber) {
        this.lastPhoneNumber = lastPhoneNumber;
    }

    /**
     * Try to send sms that contains Location back to requester.
     * If it fails, try later and until timeout.
     * */
    private boolean tryToSendLocationBack() {
        LocationHandler locationHandler = LocationHandler.getInstance(context);
        Location location = locationHandler.getLocation();

        // Create new local instance of phone number
        // when the session is closed, the pending action will still be completed
        String lastPhoneNumber = this.lastPhoneNumber;

        // if gps is not on, stop sending sms
        // if (LocationHandler.getInstance(context).isGPSOn() == false) return true;

        // if there is no phone number set, stop to try to send sms.
        if (lastPhoneNumber == null || lastPhoneNumber.isEmpty()) return true;

        if (location != null) {
            String latlon = "http://maps.google.com/maps?q=loc:"
                    + String.valueOf(location.getLatitude()) + ","
                    + String.valueOf(location.getLongitude());

            sendSMS(lastPhoneNumber, latlon);
            return true;
        }
        return false;
    }

    // TODO Create a instance to handle sms sending, which contains a constant phone number(immutable)
    private Runnable smsSender = new Runnable() {
        @Override
        public void run() {
            // When there is not timeout,
            // then try to send sms
            if (!timeout()) {
                if (!tryToSendLocationBack()) {
                    // When it failed to send sms, try it later and decrease the timer
                    tryAgain();
                } else {
                    // When it succeed, reset the timer, and unregister location listener
                    finishAndReset();
                }
            } else {
                // Timeout, stop trying to send, reset the timer and unregister location listener
                finishAndReset();
            }
        }
    };

    /**
     * Check if it timeouts.
     * */
    private boolean timeout() {
        return !(timer > 0);
    }

    /**
     * Try to send again and decrease the timer.
     * */
    private void tryAgain() {
        handler.postDelayed(smsSender, WAIT_TIME);
        timer--;
    }

    /**
     * Unregister location listener and reset timer.
     * */
    private void finishAndReset() {
        timer = MAX_ATTEMPT;
        LocationHandler.getInstance(context).unregisterLocationListener();
    }

    private void sendSMS(String desaddr,String sms) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(
                desaddr,
                null,
                sms,
                null,
                null);
        Log.i("To " + desaddr, sms);
    }

    // Try 60 times to send sms back,
    // if it still fails, stop trying.
    private static final int MAX_ATTEMPT = 60;
    private static final int WAIT_TIME = 5000;
    private int timer = MAX_ATTEMPT;

    private Handler handler = new Handler();

    public synchronized void sendLocationBack() {
        handler.post(smsSender);
    }

    public synchronized void sendOption() {
        String ops = "You have options: ";
                  //  + "##LOCATION, ";
                   // + "##PLAYSOUND, "
                   // + "##STOPSOUND, "
                   // + "##LOCKSCREEN. "
                  //  + "Or #BYE to end the session.";
        sendSMS(lastPhoneNumber, ops);
    }

    public synchronized void sendHi() {
        sendSMS(lastPhoneNumber, "Hi, you just opened the session.");
        sendOption();
    }

    public synchronized void sendBye() {
        sendSMS(lastPhoneNumber, "The session is closed now.");
    }
}
