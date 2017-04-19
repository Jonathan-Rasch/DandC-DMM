package com.example.work.dmm.utilityClasses;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Work on 13/02/2017.
 */

public class BaseApplication extends Application {
    private static final String TAG = "BASEAPPLICATION";
    //settings variables
    private static int maxDataPointsToKeep = 50;
    public static int getMaxDataPointsToKeep() {return maxDataPointsToKeep;}
    public static void setMaxDataPointsToKeep(int maxDataPoints) {maxDataPointsToKeep = maxDataPoints;}

    private static int DmmDeviceId =0000;
    public static int getDmmDeviceId() {return DmmDeviceId;}
    public static void setDmmDeviceId(int dmmDeviceId) {DmmDeviceId = dmmDeviceId;}

    private static String adapterAddress = "98:D3:31:40:19:31";
    public static String getAdapterAddress() {return adapterAddress;}
    public static void setAdapterAddress(String newAddress) {adapterAddress = newAddress;}

    private static int freqRespStartFreqHz = 1000;
    public static int getFreqRespStartFreqHz() {return freqRespStartFreqHz;}
    public static void setFreqRespStartFreqHz(int freqRespStartFreqHz) {BaseApplication.freqRespStartFreqHz = freqRespStartFreqHz;}

    private static int freqRespEndFreqHz = 10000;
    public static int getFreqRespEndFreqHz() {return freqRespEndFreqHz;}
    public static void setFreqRespEndFreqHz(int freqRespEndFreqHz) {BaseApplication.freqRespEndFreqHz = freqRespEndFreqHz;}

    private static int numberOfSteps = 10;
    public static int getNumberOfSteps() {return numberOfSteps;}
    public static void setNumberOfSteps(int numberOfSteps) {BaseApplication.numberOfSteps = numberOfSteps;}

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
                        //message from the connection containing the received packet
                        parse_read_data(intent.getByteArrayExtra(MessageCode.MSG_READ_DATA));
                        break;
                    case MessageCode.DMM_CHANGE_MODE_REQUEST://telling the DMM to switch to the given mode
                        if(connection == null){
                            ts("connection is null, are you in DEBUG mode ? if not then this is an " +
                                    "error since a change mode request has been issued to a DMM that" +
                                    "is not currently connected.");
                            return;
                        }
                        int mode= intent.getIntExtra(MessageCode.MODE,-1);
                        if (mode != -1 && mode != MessageCode.FREQ_RESP_MODE) {
                            String message = "<m:"+String.valueOf(mode)+">";
                            connection.write(message.getBytes());
                            Log.d("wrote:",message);
                        }else if (mode == MessageCode.FREQ_RESP_MODE){//mode change request for freq resp contains extra data
                            int startFreqKhz = intent.getIntExtra(MessageCode.FREQ_RESP_START_FREQ,-1);
                            int endFreqKhz = intent.getIntExtra(MessageCode.FREQ_RESP_END_FREQ,-1);
                            int numberOfSteps = intent.getIntExtra(MessageCode.FREQ_RESP_STEPS,-1);
                            if(startFreqKhz < 0 || endFreqKhz < 0 || numberOfSteps < 0){
                                Log.e(TAG,"Error when parsing the frequency response packet");
                                return;
                            }
                            String message = "<m:"+String.valueOf(mode)+";start:"+startFreqKhz+";end:"+endFreqKhz+";steps:"+numberOfSteps+">";
                            connection.write(message.getBytes());
                            Log.d("wrote:",message);
                        }else if(mode == MessageCode.SIG_GEN_MODE){//mode change request for SigGen contains extra data
                            int signalFrequency = intent.getIntExtra(MessageCode.SIGGEN_FREQ,-1);
                            float signalAmplitude = intent.getFloatExtra(MessageCode.SIGGEN_AMPL,-1);
                            String wavetype = intent.getStringExtra(MessageCode.SIGGEN_SIGTYPE);
                            if(signalFrequency <0 || signalAmplitude < 0 || wavetype == null){
                                Log.e(TAG,"Invalid signal generator parameters. Sending aborted");
                                return;
                            }
                            String message = "<m:"+mode+";freq:"+signalFrequency+";ampl:"+signalAmplitude+";type:"+wavetype+">";
                            connection.write(message.getBytes());
                            Log.d("wrote:",message);
                        }
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
        filter.addAction(MessageCode.DMM_CHANGE_MODE_REQUEST);
        this.registerReceiver(receiver,filter);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
//Haptic feedback
////////////////////////////////////////////////////////////////////////////////////////////////////
    private Vibrator vibrator;
    public void vibratePulse(long durationMs){
        if(vibrator.hasVibrator()){
            vibrator.vibrate(durationMs);
        }
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
                parse_and_send(current_message);
            }
        }
    }

    //determines if the message is valid
    private boolean validate_message(String message){
        int tags = 0;
        //check tag number and position
        for (int i=0;i<message.toCharArray().length;i++) {
            char c = message.charAt(i);
            if (i == 0 && c != '<'){
                Log.e("validate_message","no opening tag at message start: "+message);
                return false;
            }
            if (i == message.length()-1 && c != '>'){
                Log.e("validate_message","no closing tag at message end: "+message);
                return false;
            }
            //counting number of tags
            if (c == '>' || c == '<'){
                tags += 1;
            }
        }
        if (tags != 2){
            Log.e("validate_message","incorrect number of opening/closing tags in message: "+message);
            return false;
        }
        if (!message.contains("m:") || !message.contains(";v:") || !message.contains(";r:")){
            if(!message.contains("m:")){
                Log.e("validate_message","message missing 'm:' component. ");
            }
            if(!message.contains(";v:")){
                Log.e("validate_message","message missing ';v:' component. ");
            }
            if(!message.contains(";r:")){
                Log.e("validate_message","message missing ';r:' component. ");
            }
            return false;
        }
        return true;
    }

    /*
    Parses the string received from the connection class and determines to what activity
    to send the data.
     */
    private void parse_and_send(String data){
        //Parsing the operation mode of the DMM
        int mode = 0;
        String mode_string = data.substring(data.indexOf("<m:")+3,data.indexOf(";v:"));
        try {
            mode = Integer.parseInt(mode_string);
        } catch (NumberFormatException e) {
            Log.e("parse_and_send","could not parse mode from message: "+data);
            return;
        }
        //Parsing the value
        float value = 0;
        String value_string = data.substring(data.indexOf(";v:")+3,data.indexOf(";r:"));
        try {
            value = Float.parseFloat(value_string);
        } catch (NumberFormatException e) {
            Log.e("parse_and_send","could not parse value from message: "+data);
            return;
        }
        //Parsing the range of the value
        int range = 0;
        String range_string = data.substring(data.indexOf(";r:"),data.indexOf(">"));
        try{
            range = Integer.parseInt(range_string);
        }catch (NumberFormatException e){
            Log.e("parse_and_send","could not parse range from message:" + data);
            return;
        }

        //sending message to relevant activity:
        Intent intent_to_send = new Intent(MessageCode.ERROR);
        switch (mode){
            /*package format <m:int;v:float;r:int>
                * m: stands for mode, which is an int value defined in MessageCOde
                * v: value
                * r: range int value, also defined in MessageCode*/
            case MessageCode.DC_VOLTAGE_MODE:
                intent_to_send = new Intent(MessageCode.PARSED_DATA_DC_VOLTAGE);
                break;
            case MessageCode.DC_CURRENT_MODE://DC current
                intent_to_send = new Intent(MessageCode.PARSED_DATA_DC_CURRENT);
                break;
            case MessageCode.RESISTANCE_MODE://resistance
                intent_to_send = new Intent(MessageCode.PARSED_DATA_RESISTANCE);
                break;
            case MessageCode.FREQ_RESP_MODE://freq resp
                /*freq response package uses the same format as all other <m:int;v:float;r:int>
                * m: stands for mode, which is 4 for freq resp packets
                * v: Gain for this specific frequency value
                * r: the frequency at which the value is taken (in Hz)*/
                intent_to_send = new Intent(MessageCode.PARSED_DATA_FREQ_RESP);
                break;
            case MessageCode.SIG_GEN_MODE:
                /*tells the app that the DMM is carrying out its orders and generating signal. this
                * also allows the app to check that the settings of the DMM are still correct, and
                * to adjust them if needed by sending mode change request with correct settings.
                * SigGen package uses the same format as all other <m:int;v:float;r:int>
                * m: should always be 5 for sigGen packets
                * v: should be the amplitude of the signal that is being generated
                * r: should be frequency of signal in Hz*/
                intent_to_send = new Intent(MessageCode.SIGGEN_ACK);
                break;
            default:
                Log.e("parse_and_send","invalid mode: "+mode);
                break;
        }
        // if message is valid then add the value and range data and send it
        if (intent_to_send.getAction() != MessageCode.ERROR){
            intent_to_send.putExtra(MessageCode.VALUE,value);
            intent_to_send.putExtra(MessageCode.RANGE,range);
            sendBroadcast(intent_to_send);
        }else{
            Log.e("MsgCode","Invalid message mode, send canceled");
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
//getters and setters
////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getConnectedDeviceName(){
        if (getConnection_active()){
            return connection.getBluetoothDevice().getName();
        }
        return "";
    }

    public String getConnectedDeviceAddress(){
        if (getConnection_active()){
            return connection.getBluetoothDevice().getAddress();
        }
        return "";
    }

    public boolean get_connection_in_Progress(){
        return connection_in_Progress;
    }

    public void t(String text){
        Log.w(TAG,text);
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }
    public void ts(String text){
        Log.w(TAG,text);
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }
}
