package com.example.cc.controller.service.tools;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by chendehua on 16/11/4.
 */

public class StateManager {
    private static StateManager instance;
    private SharedPreferences sharedPreferences;
    private static final String FILE_NAME = "com.example.myapp.PREFERENCE_FILE_KEY";
    private static final String LAST_STATE = "last_state";

    public static final int STATE_EMPTY = -1;
    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;


    private StateManager(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public synchronized static StateManager getInstance(Context context) {
        if (instance == null) {
            instance = new StateManager(context);
        }
        return instance;
    }

    public synchronized int getLastState() {
        return sharedPreferences.getInt(LAST_STATE, -1);
    }

    public synchronized boolean setLastState(int state) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LAST_STATE, state);
        return editor.commit();
    }
}
