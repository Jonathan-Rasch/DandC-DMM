package com.example.work.dmm;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
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
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        connection_in_Progress = false;
                        break;
                    case MessageCode.CUSTOM_ACTION_SERIAL:
                        parse_read_data(intent.getByteArrayExtra(MessageCode.MSG_READ_DATA));
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
    //to prevent trying to start two connections (sometimes device discovery finds device twice)
    private boolean connection_in_Progress = false;
    public void start_connection(BluetoothDevice device){
        this.drop_connection();
        if (blueAdapter != null && blueAdapter.isEnabled() && !connection_in_Progress) {
            connection_in_Progress = true;
            connection = new clientBluetoothConnection(device,blueAdapter,getApplicationContext());
            connection.start();
        }
    }

    public void drop_connection(){
        if (connection != null && connection.isConnectionActive()){
            t("Dropping connection...");
            connection.close_connection();
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
// Data processing
////////////////////////////////////////////////////////////////////////////////////////////////////
    private void parse_read_data(byte[] buffer) {
        if (buffer != null) {
            String current_message = new String(buffer);
            if (validate_message(current_message)){
                Intent data_read = new Intent(MessageCode.PARSED_DATA_VOLTAGE);
                data_read.putExtra(MessageCode.PARSED_DATA_VOLTAGE, current_message.toString());
                //TODO do actual parsing before
                sendBroadcast(data_read);
            }
        }
    }

    //determines if the message is valid, and returns the message type (voltage,current,range,etc)
    private boolean validate_message(String message){
        int tags = 0;
        //check tag number and position
        for (int i=0;i<message.toCharArray().length;i++) {
            char c = message.charAt(i);
            if (i == 0 && c != '<'){
                Log.e("validate_message","no opening tag at message start:"+message);
                return false;
            }
            if (i == message.length()-1 && c != '>'){
                Log.e("validate_message","no closing tag at message end:"+message);
                return false;
            }
            //counting number of tags
            if (c == '>' || c == '<'){
                tags += 1;
            }
        }
        if (tags != 2){
            Log.e("validate_message","incorrect number of opening/closing tags in message:"+message);
            return false;
        }
        return true;
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

    public boolean get_connection_in_Progress(){
        return connection_in_Progress;
    }

    public void t(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }
    public void ts(String text){
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }
}
