package com.example.cc.controller.service.admin;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Created by cc on 16-10-30.
 */

public class DeviceAdminTools {
    private static DeviceAdminTools instance;
    private DevicePolicyManager DPM;

    public static final int REQUEST_ADMIN_PERMISSION_CODE = 0;

    public synchronized static DeviceAdminTools getInstance(Context context) {
        if (instance == null) {
            instance = new DeviceAdminTools(context);
        }
        return instance;
    }

    private DeviceAdminTools(Context context) {
        DPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    /*
    * Request device admin permission
    * Note: the instances of context and activity have to be from same activity
    * */
    public synchronized void requestAdminPermission(Context context, Activity activity) {
        if (context.equals(activity)) {
            ComponentName componentName = new ComponentName(context,
                    DeviceAdminHandler.class);

            Intent intent = new Intent(DevicePolicyManager
                    .ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "We need the admin permission to lock screen.");
            activity.startActivityForResult(intent, REQUEST_ADMIN_PERMISSION_CODE);
        }
    }

    /*
    * Check if App has device admin permission
    * */
    public synchronized boolean isDeviceAdminActive(Context context) {
        return DPM.isAdminActive(new ComponentName(context,
                DeviceAdminHandler.class));
    }

    /*
    * !!!DANGER!!!
    * Source:   http://stackoverflow.com/questions/13145544/lock-screen-password-lock-android
    *           https://developer.android.com/guide/topics/admin/device-admin.html#sample
    *           https://developer.android.com/reference/android/app/admin/DevicePolicyManager.html#resetPassword(java.lang.String, int)
    * */
    private synchronized boolean resetDevicePassword(Context context, String newPassword) {
        ComponentName componentName = new ComponentName(context,
                DeviceAdminHandler.class);

        if (DPM.isAdminActive(componentName)) {
            return DPM.resetPassword(newPassword, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        }
        return false;
    }

    /*
    * Source:   http://stackoverflow.com/questions/14352648/how-to-lock-unlock-screen-programmatically
    *           https://developer.android.com/guide/topics/admin/device-admin.html
    * */
    public synchronized boolean lockScreen(Context context) {
        ComponentName componentName = new ComponentName(context,
                DeviceAdminHandler.class);

        if (DPM.isAdminActive(componentName)) {
            DPM.lockNow();
            return true;
        }
        return false;
    }
}
