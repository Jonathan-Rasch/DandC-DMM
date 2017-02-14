package com.example.work.dmm;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

/**
 * Created by Work on 13/02/2017.
 */

public class BaseApplication extends Application {
    //Bluetooth connection
    private clientBluetoothConnection connection;
    private BluetoothAdapter blueAdapter;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null) {
                switch (action){
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        Intent startConnectionScreen = new Intent(getApplicationContext(),ConnectionScreenActivity.class);
                        startConnectionScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startConnectionScreen);
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        Intent startMainScreen = new Intent(getApplicationContext(),MainActivity.class);
                        startMainScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMainScreen);
                        break;
                    default:
                        break;
                }
            }

        }
    };

    public Boolean getConnection_active() {
        if (connection != null) {
            return connection.isConnectionActive();
        }else {
            return false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(receiver,filter);
    }

    public void setBluetoothAdapter(BluetoothAdapter adapter){
        this.blueAdapter = adapter;
    }

    public Boolean start_connection(BluetoothDevice device){
        if (blueAdapter != null && blueAdapter.isEnabled()) {
            connection = new clientBluetoothConnection(device,blueAdapter,getApplicationContext());
            connection.start();
            while(connection.get_connection_state() == 0){}//TODO fix this ugly mess
            if (connection.get_connection_state() == 1){
                return true;//connection made
            }else{
                connection.close_connection();
            }
        }
        return false;
    }

    public void drop_connection(){
        if (connection != null){
            connection.close_connection();
        }
    }

    public void t(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }
    public void ts(String text){
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }
}
