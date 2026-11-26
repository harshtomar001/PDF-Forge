package com.harsh.pdfstudio.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class Vibrate {

    public void vibrate( Context context, long duration) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            VibratorManager vibratorManager =
                    (VibratorManager) context
                            .getSystemService(Context.VIBRATOR_MANAGER_SERVICE);

            if (vibratorManager != null) {

                Vibrator vibrator =
                        vibratorManager.getDefaultVibrator();

                vibrator.vibrate(
                        VibrationEffect.createOneShot(
                                duration,
                                VibrationEffect.DEFAULT_AMPLITUDE
                        )
                );
            }

        } else {

            Vibrator vibrator =
                    (Vibrator) context
                            .getSystemService(Context.VIBRATOR_SERVICE);

            if (vibrator != null) {

                vibrator.vibrate(
                        VibrationEffect.createOneShot(
                                duration,
                                VibrationEffect.DEFAULT_AMPLITUDE
                        )
                );
            }
        }
    }
}
