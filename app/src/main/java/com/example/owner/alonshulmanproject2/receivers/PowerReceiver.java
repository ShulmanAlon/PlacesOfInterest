package com.example.owner.alonshulmanproject2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.owner.alonshulmanproject2.R;

/**
 * Created by Owner on 28/03/2016.
 */
public class PowerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /** receiver handling two filters, one when power is connected, the other when disconnecting, and displays it to the user */
        switch (intent.getAction()){
            case Intent.ACTION_POWER_CONNECTED:
                Toast.makeText(context, R.string.receiver_power_connected, Toast.LENGTH_SHORT).show();
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                Toast.makeText(context, R.string.receiver_power_disconnected, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
