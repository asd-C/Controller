package com.example.cc.controller.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cc.controller.R;
import com.example.cc.controller.service.MainService;
import com.example.cc.controller.service.admin.DeviceAdminTools;
import com.example.cc.controller.service.command.ServiceCommands;
import com.example.cc.controller.service.tools.PlaySound;

/*
* Feature available:
*       read new incoming sms,
*       play sound,
*       app with admin permission,
*       lock screen
*
* Features to implement:
*       retrieve geolocation,
*       finish command class,
*       finish SMSParser
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

    private Button button;
    private TextView textView;
    private TextView sms_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, MainService.class));

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        textView = (TextView) findViewById(R.id.textView);

        // textview which shows the received SMSs
        sms_main = (TextView) findViewById(R.id.sms_main);
    }

    private void requestSMSAndAdminPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(
                        new String[]{Manifest.permission.RECEIVE_SMS}, 0);
            }
        }

        if (!DeviceAdminTools.getInstance(this).isDeviceAdminActive(this)) {
            DeviceAdminTools.getInstance(this).requestAdminPermission(this,this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        doBindService();
        requestSMSAndAdminPermission();
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
                PlaySound p = PlaySound.getInstance(getApplicationContext());

                if (p.isPlaying()) {
                    p.stop();
                } else {
                    p.play();
                }
                break;
            default:
                break;
        }
    }
}
