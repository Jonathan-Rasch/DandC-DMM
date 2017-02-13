package com.example.work.dmm;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //the connection thread that is responsible for reading and writing of data
    private clientBluetoothConnection connection;
    // the device bluetooth adapter
    private BluetoothAdapter blueAdapter;
    //Broadcast receiver for bluetooth state changes
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //testing for changed bluetooth state
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int AdapterState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (AdapterState){
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    default:
                        break;
                }
            }
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                toast("Discovery finished.");
            }
        }
    };
    //views
    private TextView tv_bluetoothStatus;
    private Button btn_connect;//starts the bluetooth setup


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getting the activity views:
        tv_bluetoothStatus = (TextView) findViewById(R.id.tv_bluetoothStatus);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        //broadcast receiver setup
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver,filter);

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
            toast("Bluetooth disabled, please enable bluetooth");
            tv_bluetoothStatus.setText("OFF");
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent,MessageCode.ENABLE_BLUETOOTH_REQ);
        }else{//bluetooth on, start to search for devices
            tv_bluetoothStatus.setText("ON");
            toast("Starting device discovery...");

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case MessageCode.ENABLE_BLUETOOTH_REQ:
                if (resultCode == RESULT_OK){
                    toast("Bluetooth Enabled");
                    tv_bluetoothStatus.setText("ON");
                    //retry the setup
                    setupBluetooth();
                }else {//Resuest canceled.
                    toast("Enabeling Bluetooth FAILED");
                    tv_bluetoothStatus.setText("OFF");
                    btn_connect.setEnabled(true);//allow the user to restart bluetooth setup
                }
                break;
            default:
                break;
        }
    }


    public void onClick_connect(View view){
        btn_connect.setEnabled(false);//disable button whilst trying to setup the bluetooth
        setupBluetooth();
        //Intent startConnectionScreen = new Intent(this,ConnectionScreenActivity.class);
        //startActivity(startConnectionScreen);
    }

    private void toast(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }
}
