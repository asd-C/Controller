package com.example.cc.controller.service.tools;

import android.content.Context;

/**
 * Created by chendehua on 16/11/4.
 */

public class StateManager {
    private static StateManager instance;

    private StateManager(Context context) {

    }

    public synchronized static StateManager getInstance(Context context) {
        if (instance == null) {
            instance = new StateManager(context);
        }
        return instance;
    }
}
