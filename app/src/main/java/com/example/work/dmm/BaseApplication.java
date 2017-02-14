package com.example.work.dmm;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import java.util.ArrayList;

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
                    case MessageCode.CUSTOM_ACTION_SERIAL:
                        byte[] buffer = new byte[1024];
                        buffer = intent.getByteArrayExtra(MessageCode.MSG_READ_DATA);
                        parse_read_data(buffer);
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
        filter.addAction(MessageCode.CUSTOM_ACTION_SERIAL);
        this.registerReceiver(receiver,filter);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
//connection related methods
////////////////////////////////////////////////////////////////////////////////////////////////////
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
            t("Dropping connection...");
            connection.close_connection();
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
// Data processing
////////////////////////////////////////////////////////////////////////////////////////////////////
    private StringBuilder current_message;//holds the message that is currently being put together

    private void parse_read_data(byte[] buffer) {
        String message = new String(buffer);
        if (buffer != null) {
            if (message.contains("<")){
                current_message = new StringBuilder(message);//clearing the current message string so the next one can be put together
            }else{
                current_message.append(message);
            }
            //test if the message contains a closing tag (meaning message is complete)
            if (message.contains(">")){
                Intent data_read = new Intent(MessageCode.PARSED_DATA_VOLTAGE);
                data_read.putExtra(MessageCode.PARSED_DATA_VOLTAGE, current_message.toString());
                //TODO do actual parsing before
                sendBroadcast(data_read);
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
//getters and setters
////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getDeviceName(){
        if (getConnection_active()){
            return connection.getBluetoothDevice().getName();
        }
        return "";
    }

    public String getDeviceAddress(){
        if (getConnection_active()){
            return connection.getBluetoothDevice().getAddress();
        }
        return "";
    }

    public void t(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }
    public void ts(String text){
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }
}
