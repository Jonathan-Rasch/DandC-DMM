package com.descon.work.dmm.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.descon.work.dmm.utilityClasses.BaseApplication;
import com.descon.work.dmm.utilityClasses.MessageCode;
import com.descon.work.dmm.R;
import com.descon.work.dmm.utilityClasses.clientBluetoothConnection;
import com.skyfishjy.library.RippleBackground;

public class MainActivity extends AppCompatActivity {
    //Ripple Effect for searching
    private RippleBackground rippleBackground;
    //base application
    private BaseApplication base;
    // the device bluetooth adapter
    private BluetoothAdapter blueAdapter;
    //the class than handles the bluetooth connection.
    private clientBluetoothConnection connection;
    //views
    private TextView tv_bluetoothStatus;
    private TextView tv_discovered_devices;
    private Button btn_connect;//starts the bluetooth setup
    private Button btn_DEBUG_CONNECT;
    //Broadcast receiver for bluetooth state changes
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            /*detecting Bluetooth adapter broadcasts*/
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int AdapterState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (AdapterState){
                    case BluetoothAdapter.STATE_OFF:
                        tv_bluetoothStatus.setText("OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        tv_bluetoothStatus.setText("ON");
                        break;
                    default:
                        break;
                }
            }
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                base.ts("Discovery finished.");
                btn_connect.setEnabled(true);
                rippleBackground.stopRippleAnimation();
                if (blueAdapter.isEnabled()){
                    tv_bluetoothStatus.setText("ON");
                }else{
                    tv_bluetoothStatus.setText("OFF");
                }
                return;
            }else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                base.ts("Starting device discovery...");
                tv_bluetoothStatus.setText("Discovering...");
            }

            /*detecting Bluetooth device broadcasts*/
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String device_address = device.getAddress();
                    tv_discovered_devices.setText(tv_discovered_devices.getText()+"\n"+device.getName()+" "+device_address);
                    if (device_address != null && device_address.equals(base.getAdapterAddress()) && !base.get_connection_in_Progress()){
                        base.ts("Bluetooth device found: "+ device.getName());
                        base.start_connection(device);
                        //start connection screen activity
                        Intent startConnectionScreen = new Intent(getApplicationContext(),ConnectionScreenActivity.class);
                        startActivity(startConnectionScreen);
                        unregisterReceiver(receiver);
                        finish();
                        Log.e("tag","CONNECTION STARTED");
                    }
                }
            }

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connection_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent start_settingsActivity_intent = new Intent(this,SettingsActivity.class);
            startActivity(start_settingsActivity_intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        base = ((BaseApplication)this.getApplicationContext());
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Connect to DMM");

        //Get search animation reference
        rippleBackground = (RippleBackground)findViewById(R.id.content);

        //getting the activity views:
        tv_bluetoothStatus = (TextView) findViewById(R.id.tv_bluetoothStatus);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        tv_discovered_devices = (TextView)findViewById(R.id.tv_discovered_devices);
        //broadcast receiver setup
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,filter);

    }

    private void setupBluetooth(){
        rippleBackground.startRippleAnimation();
        //clearing text view that displays the discovered devices
        tv_discovered_devices.setText("");
        //close any existing connections
        if(base.getConnection_active()){
            base.drop_connection();
        }
        //getting the devices bluetooth adapter
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if(blueAdapter == null){
            base.ts("ERROR: no bluetooth adapter found.");
            tv_bluetoothStatus.setText("ERROR: No adapter found.");
            rippleBackground.stopRippleAnimation();
        }else if(!blueAdapter.isEnabled()){//adapter found, but is it enabled ?
            //bluetooth is off, ask user to switch it on
            base.ts("Bluetooth disabled, please enable bluetooth");
            tv_bluetoothStatus.setText("OFF");
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, MessageCode.ENABLE_BLUETOOTH_REQ);
        }else{//bluetooth on, start to search for devices
            tv_bluetoothStatus.setText("ON");
            //setting the bluetooth adapter in base class
            base.setBluetoothAdapter(blueAdapter);
            blueAdapter.startDiscovery();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case MessageCode.ENABLE_BLUETOOTH_REQ:
                if (resultCode == RESULT_OK){
                    base.ts("Bluetooth Enabled");
                    tv_bluetoothStatus.setText("ON");
                    //retry the setup
                    setupBluetooth();
                }else {//Resuest canceled.
                    base.ts("Enabeling Bluetooth FAILED");
                    rippleBackground.stopRippleAnimation();
                    tv_bluetoothStatus.setText("OFF");
                    btn_connect.setEnabled(true);//allow the user to restart bluetooth setup
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(receiver);
        finish();
        super.onBackPressed();
    }

    public void onClick_connect(View view){
        btn_connect.setEnabled(false);//disable button whilst trying to setup the bluetooth
        setupBluetooth();
    }

    public void onClick_DEBUG_CONNECT(View view){
        Intent startConnectionScreen = new Intent(getApplicationContext(),ConnectionScreenActivity.class);
        startActivity(startConnectionScreen);
        unregisterReceiver(receiver);
        Log.w("DEBUG","Connection screen transition via debug button");
        finish();
    }


}
