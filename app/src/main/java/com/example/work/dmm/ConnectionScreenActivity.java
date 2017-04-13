package com.example.work.dmm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.LinkedList;

public class ConnectionScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BaseApplication base;
    private TextView tv_device_address,tv_device_name,tv_received_data;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MessageCode.PARSED_DATA_DC_VOLTAGE.equals(intent.getAction())) {
                String message = "DC Voltage:"+String.valueOf(intent.getFloatExtra(MessageCode.VALUE,0f));
                displayReceivedPacket(message);
            }else if(MessageCode.PARSED_DATA_DC_CURRENT.equals(intent.getAction())){
                String message = "DC Current:"+String.valueOf(intent.getFloatExtra(MessageCode.VALUE,0f));
                displayReceivedPacket(message);
            }else if(MessageCode.PARSED_DATA_RESISTANCE.equals(intent.getAction())){
                String message = "Resistance:"+String.valueOf(intent.getFloatExtra(MessageCode.VALUE,0f));
                displayReceivedPacket(message);
            }
        }
    };

    private LinkedList<String> message_list = new LinkedList<>();
    private static final int CONSOL_BUFFER_SIZE = 30; // how many entries the console holds
    private void displayReceivedPacket(String message){
        if (consolEnabled) {
            message_list.addFirst(message);
            //check if list contains more elements than allowed by the buffer size, and remove last if needed
            if (message_list.size()>= CONSOL_BUFFER_SIZE){
                message_list.removeLast();
            }
            // build string to assign to textview:
            StringBuilder local_stringBuilder = new StringBuilder("");
            for (int i=message_list.size()-1;i>0;--i){//reversing order
                local_stringBuilder.append(message_list.get(i)+"\n");
            }
            //now update the text view
            tv_received_data.setText(local_stringBuilder.toString());
        }
    }

    public void onClick_ScrollView(View view){
        //scroll to the bottom to see newest entries on click
        ((ScrollView)view).fullScroll(view.FOCUS_DOWN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //registering brodcast listener
        IntentFilter filter = new IntentFilter(MessageCode.PARSED_DATA_DC_VOLTAGE);
        filter.addAction(MessageCode.PARSED_DATA_DC_CURRENT);
        filter.addAction(MessageCode.PARSED_DATA_RESISTANCE);
        registerReceiver(receiver,filter);

        //getting views
        base = (BaseApplication)getApplicationContext();
        tv_device_address = (TextView)findViewById(R.id.connectionScreen_device_address);
        tv_device_name = (TextView)findViewById(R.id.connectionScreen_device_name);
        tv_received_data = (TextView)findViewById(R.id.tv_received_data);
        tv_device_address.setText(base.getConnectedDeviceAddress());
        tv_device_name.setText(base.getConnectedDeviceName());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);
        }
    }

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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_Voltage) {
            Intent startVoltageActivity = new Intent(this,DCvoltageActivity.class);
            startActivity(startVoltageActivity);
        } else if (id == R.id.nav_Current) {

        } else if (id == R.id.nav_Resistance) {

        } else if (id == R.id.nav_freqResp) {
            Intent startFreqRespActivity = new Intent(this,FrequencyResponseActivity.class);
            startActivity(startFreqRespActivity);
        } else if (id == R.id.nav_disconnect) {
            base.drop_connection();
            this.unregisterReceiver(receiver);
            Intent startMainScreen = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(startMainScreen);
            finish();
        } 

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean consolEnabled = false;
    public void onClick_displayConsolSwitch(View view){
        consolEnabled = ((Switch)view).isChecked();
    }


}
