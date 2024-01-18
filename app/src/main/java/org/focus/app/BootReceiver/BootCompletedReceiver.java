package org.focus.app.BootReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

import org.focus.app.Location.LocationService;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, LocationService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
