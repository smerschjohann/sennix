package dev.sennix;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, starting service...");
            Intent serviceIntent = new Intent(context, SensorService.class);
            context.startService(serviceIntent);
        }
    }
}
