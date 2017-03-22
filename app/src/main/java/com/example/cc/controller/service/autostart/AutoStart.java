package com.example.cc.controller.service.autostart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.cc.controller.service.MainService;

/**
 * Created by chendehua on 16/11/4.
 */

public class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MainService.class));
    }
}
