package tk.roberthramirez.chisteboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReciber extends BroadcastReceiver {

    private final String ON_BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ON_BOOT)){

        }
    }
}
