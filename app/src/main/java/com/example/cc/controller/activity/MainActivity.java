package com.example.cc.controller.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.cc.controller.R;
import com.example.cc.controller.activity.intro.IntroActivity;
import com.example.cc.controller.activity.intro.IntroHelper;
import com.example.cc.controller.service.MainService;
import com.example.cc.controller.service.admin.DeviceAdminTools;
import com.example.cc.controller.service.command.ServiceCommands;
import com.example.cc.controller.service.tools.LocationHandler;
import com.skyfishjy.library.RippleBackground;

/*
* Feature available:
*       read new incoming sms,
*       #(SMSCommand)play sound,
*       app with admin permission,
*       #(SMSCommand)lock screen,
*       #(SMSCommand)retrieve geolocation,
*       notification when is active,
*       open/close session to send commands
*       auto start,
*       save/recover last state,
*       layout,
*       presentation,
*       check if gps is on
*
* Features to implement:
*       finish command class,
*       finish SMSParser
*       show how to work
*
* Warning:
*       resetPassword is not in use
* */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Messenger serviceMessenger;
    private Messenger clientMessenger = new Messenger(new Handler(){
        /**
         * Override the function to handle incoming messages from service.
         * */
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;

            // find out what kind of msg it is.
            switch (what) {
                case ServiceCommands.MSG_NEW_SMS:
                    sms_main.setText(((Bundle)msg.obj).getString("sms"));
                    break;
                case ServiceCommands.MSG_START_RECEIVING_SMS:
                    setBtnActive();
                    break;
                case ServiceCommands.MSG_STOP_RECEIVING_SMS:
                    setBtnInactive();
                    break;
            }
            super.handleMessage(msg);
        }
    });

    // Connection to service,
    // handles when service is connected
    // and when is disconnected
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // obtain the messenger of service,
            // which is used to communicate to service
            serviceMessenger = new Messenger(service);

            // notify the service the client is on
            Message msg = Message.obtain();
            msg.what = ServiceCommands.MSG_CONNECT;
            // passing the messenger of client to service
            // for getting notifications back from service
            msg.replyTo = clientMessenger;
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
        }
    };

    RippleBackground rippleBackground;

    private void setBtnActive() {
        if (rippleBackground == null) rippleBackground = (RippleBackground)findViewById(R.id.content);
        enable_btn.setText(BTN_ACTIVE);
        enable_btn.setTextColor(Color.WHITE);
        rippleBackground.startRippleAnimation();
    }

    private void setBtnInactive() {
        if (rippleBackground == null) rippleBackground = (RippleBackground)findViewById(R.id.content);
        enable_btn.setText(BTN_INACTIVE);
        enable_btn.setTextColor(Color.BLACK);
        rippleBackground.stopRippleAnimation();
    }

    private boolean isBtnActive() {
        return enable_btn.isChecked();
    }

    private Button reset_btn, help_btn;
    private ToggleButton enable_btn;
    private TextView sms_main;

    private static final String BTN_ACTIVE = "Active";
    private static final String BTN_INACTIVE = "Inactive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, MainService.class));
//        requestSMSAndAdminPermission();

        reset_btn = (Button) findViewById(R.id.button);
        reset_btn.setOnClickListener(this);
//        reset_btn.setVisibility(View.GONE);

        // textview which shows the received SMSs
        sms_main = (TextView) findViewById(R.id.sms_main);
        sms_main.setVisibility(View.GONE);

        enable_btn = (ToggleButton) findViewById(R.id.enable_btn_main);
        enable_btn.setOnClickListener(this);
        setBtnInactive();

        help_btn = (Button) findViewById(R.id.help_btn_main);
        help_btn.setOnClickListener(this);

        IntroHelper helper = new IntroHelper(this);
        if (helper.isFirstAccess()) {
            startActivity(new Intent(this, IntroActivity.class));
        }
    }

    /**
     * Called in onCreated()
     * Check admin, sms and location permission,
     * request if app does not have yet
     * */
    private void requestSMSAndAdminPermission() {
        requestAdminPermission();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED ||
                    this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(
                        new String[]{Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }
    }

    /**
     * Request admin permission if app does not have yet
     * */
    private void requestAdminPermission() {
        if (!haveAdminPermission()) {
            DeviceAdminTools.getInstance(this).requestAdminPermission(this,this);
        }
    }

    /**
     * Check if app has admin permissions.
     * */
    private boolean haveAdminPermission() {
        return DeviceAdminTools.getInstance(this).isDeviceAdminActive(this);
    }

    /**
     * Check if app has all permissions, SMS and location.
     * */
    private boolean haveAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return !(this.checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED ||
                    this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED);
        } else {
            return true;
        }
    }

    private void showInfoAboutPermissions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions not granted.")
                .setCancelable(false)
                .setMessage("We need all these permissions to provide services to you." +
                        " Please, grant them!")
                .setNegativeButton("NO!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        MainActivity.this.startActivity(intent);
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        doBindService();
//        requestSMSAndAdminPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
    }

    private void doBindService() {
        bindService(new Intent(this, MainService.class),
                connection, Context.BIND_AUTO_CREATE);
    }

    private void doUnbindService() {
        // notify the service that the client is off
        if (serviceMessenger != null) {
            Message msg = Message.obtain();
            msg.what = ServiceCommands.MSG_DISCONNECT;
            try {
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(connection);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                (new IntroHelper(this)).set(true);
                break;

            case R.id.enable_btn_main:
                enableBtnClicked();
                break;

            case R.id.help_btn_main:
                showHelpDialog();
                break;

            default:
                break;
        }
    }

    private void showHelpDialog() {
        String ops = "To start control your cell phone remotely, you need to open session.\n" +
                "To open session: #contoller\n";
        String[] commands = getResources().getStringArray(R.array.Commands);
        ops += "To close session: #BYE\n";
        ops += "When the session is opened, you have options:\n";
        for (String s: commands) {
            ops += s + "\n";
        }
        (new AlertDialog.Builder(this)).setTitle("Help").setMessage(ops).show();
    }

    /**
     * Check if it is ready to start, if not request permissions.
     * Change the reset_btn's text, ENABLE/DISABLE.
     * Notify the MainService to start monitoring incoming sms.
     * */
    private void enableBtnClicked() {
        if (!isBtnActive()) {
            // if do not have admin permission,
            // can not start the service.
            if (!haveAdminPermission()) {
                requestAdminPermission();
                return;
            }
            // if do not have all permission,
            // can not start the service.
            if (!haveAllPermissions()) {
                showInfoAboutPermissions();
                return;
            }
            // if GPS if off, request to turn on and return
            if (!LocationHandler.getInstance(this).isGPSOn()) {
                showDialogRequestingGPS();
                return;
            }
            if (serviceMessenger != null) {
                Message msg = Message.obtain();
                msg.what = ServiceCommands.MSG_START_RECEIVING_SMS;
                try {
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                setBtnActive();
            } else {
                startService(new Intent(this, MainService.class));
                doBindService();
            }
        } else {
            if (serviceMessenger != null) {
                Message msg = Message.obtain();
                msg.what = ServiceCommands.MSG_STOP_RECEIVING_SMS;
                try {
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                startService(new Intent(this, MainService.class));
                doBindService();
            }
            setBtnInactive();
        }
    }

    /**
     * When GPS is off, show the dialog explaining why GPS is needed
     * */
    private void showDialogRequestingGPS() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("GPS is off!")
                .setMessage("Please, turn on GPS! \nWe need GPS to locate your phone if you lost it.")
                .create();
        dialog.show();
    }
}
