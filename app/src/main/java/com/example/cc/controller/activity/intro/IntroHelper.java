package com.example.cc.controller.activity.intro;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by chendehua on 16/11/6.
 */

public class IntroHelper {
    private SharedPreferences sharedPreferences;
    private static final String FILE_NAME = "com.example.myapp.FIRST_ACCESS";
    private static final String FIRST_ACCESS = "first_access";

    public IntroHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFirstAccess() {
        return sharedPreferences.getBoolean(FIRST_ACCESS, true);
    }

    public boolean set(boolean isFirstAccess) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_ACCESS, isFirstAccess);
        return editor.commit();
    }
}
