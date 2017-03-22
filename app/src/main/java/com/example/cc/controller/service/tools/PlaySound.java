package com.example.cc.controller.service.tools;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

/**
 * Created by cc on 16-10-30.
 */

public class PlaySound {
    private Ringtone ringtone;

    private static PlaySound instance;

    public static synchronized PlaySound getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new PlaySound(applicationContext);
        }
        return instance;
    }

    /**
     * Source:  https://developer.android.com/reference/android/media/AudioAttributes.Builder.html
     *          https://developer.android.com/reference/android/media/Ringtone.html#setAudioAttributes(android.media.AudioAttributes)
     *          http://stackoverflow.com/questions/15578812/troubles-play-sound-in-silent-mode-on-android
     *          http://stackoverflow.com/questions/2618182/how-to-play-ringtone-alarm-sound-in-android
     * */
    private PlaySound(Context applicationContext) {
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(applicationContext, alarm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ringtone.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM).build());
        } else {
            ringtone.setStreamType(AudioManager.STREAM_ALARM);
        }
    }

    public synchronized void play() {
        ringtone.play();
    }

    public synchronized boolean isPlaying() {
        return ringtone.isPlaying();
    }

    public synchronized void stop() {
        if (ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}
