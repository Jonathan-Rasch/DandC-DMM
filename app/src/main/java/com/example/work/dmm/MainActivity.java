package com.example.work.dmm;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //the connection thread that is responsible for reading and writing of data
    private clientBluetoothConnection connection;
    // the device bluetooth adapter
    private BluetoothAdapter blueAdapter;
    //views
    private TextView tv_bluetoothStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getting the activity views:
        tv_bluetoothStatus = (TextView) findViewById(R.id.tv_bluetoothStatus);

    }

    private void setupBluetooth(){
        if(connection != null){
            connection.close_connection();
        }
        //getting the devices bluetooth adapter
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if(blueAdapter == null){
            toast("ERROR: no bluetooth adapter found.");
            tv_bluetoothStatus.setText("ERROR: No adapter found.");
        }else if(!blueAdapter.isEnabled()){//adapter found, but is it enabled ?
            //bluetooth is off, ask user to switch it on
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent,MessageCode.ENABLE_BLUETOOTH_REQ);
        }else{//bluetooth on, start to search for devices

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case MessageCode.ENABLE_BLUETOOTH_REQ:
                if (resultCode == RESULT_OK){
                    toast("Bluetooth Enabled");
                    //retry the setup
                    setupBluetooth();
                }else {//Resuest canceled.
                    toast("Enabeling Bluetooth FAILED");
                }
                break;
            default:
                break;
        }
    }

    private void discoverBluetoothDevicesTimer(){
        if (blueAdapter.isDiscovering()){
            blueAdapter.cancelDiscovery();
        }
        blueAdapter.startDiscovery();
        new CountDownTimer(20000,500){
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                /*test if a connection has been made, if this is not the case then restart the
                * device discovery and restart the timer*/
                if(connection == null){
                    discoverBluetoothDevicesTimer();
                }
            }
        }.start();
    }

    public void onClick_connect(View view){

    }

    private void toast(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }
}
