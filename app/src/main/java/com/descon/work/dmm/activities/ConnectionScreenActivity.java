package com.descon.work.dmm.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

import com.descon.work.dmm.activities.measurementActivities.Level1.DCcurrentActivity;
import com.descon.work.dmm.activities.measurementActivities.Level1.ResistanceActivity;
import com.descon.work.dmm.activities.measurementActivities.Level3.LightIntensityActivity;
import com.descon.work.dmm.activities.measurementActivities.Level3.SignalGeneratorActivity;
import com.descon.work.dmm.utilityClasses.BaseApplication;
import com.descon.work.dmm.utilityClasses.MessageCode;
import com.descon.work.dmm.R;
import com.descon.work.dmm.activities.measurementActivities.Level1.DCvoltageActivity;
import com.descon.work.dmm.activities.measurementActivities.Level3.FrequencyResponseActivity;

import java.util.LinkedList;

public class ConnectionScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BaseApplication base;
    private TextView tv_device_address,tv_device_name,tv_received_data;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MessageCode.PARSED_DATA_DC_VOLTAGE.equals(intent.getAction())) {
                String message = "[Voltage]"+String.valueOf(intent.getFloatExtra(MessageCode.VALUE,0f));
                displayReceivedPacket(message);
            }else if(MessageCode.PARSED_DATA_DC_CURRENT.equals(intent.getAction())){
                String message = "[Current]"+String.valueOf(intent.getFloatExtra(MessageCode.VALUE,0f));
                displayReceivedPacket(message);
            }else if(MessageCode.PARSED_DATA_RESISTANCE.equals(intent.getAction())){
                String message = "[Resistance] value:"+String.valueOf(intent.getFloatExtra(MessageCode.VALUE,0f)+
                " range:"+String.valueOf(intent.getFloatExtra(MessageCode.RANGE,0f)));
                displayReceivedPacket(message);
            }else if(MessageCode.PARSED_DATA_FREQ_RESP.equals(intent.getAction())){
                String message = "[FreqResp] Vout/Vin:"+String.valueOf(intent.getFloatExtra(MessageCode.VALUE,0f)+
                        " @frequency:"+String.valueOf(intent.getFloatExtra(MessageCode.RANGE,0f)));
                displayReceivedPacket(message);
            }else if(MessageCode.SIGGEN_ACK.equals(intent.getAction())){
                String message = "[SigGen] amplitude:"+String.valueOf(intent.getFloatExtra(MessageCode.VALUE,0f)+
                        " @frequency:"+String.valueOf(intent.getFloatExtra(MessageCode.RANGE,0f)));
                displayReceivedPacket(message);
            }else if(MessageCode.PARSED_CAPACITANCE.equals(intent.getAction())){
                String message = "[Capacitance] value:"+String.valueOf(intent.getFloatExtra(MessageCode.VALUE,0f));
                displayReceivedPacket(message);
            }
    }};

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
        //setting up the look of the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Connection overview");
        //nav drawer setup
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //registering brodcast listener
        base = (BaseApplication)getApplicationContext();
        registerReceiver(receiver,base.FILTER);

        //getting views
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
            Intent startCurrentActivity = new Intent(this,DCcurrentActivity.class);
            startActivity(startCurrentActivity);
        } else if (id == R.id.nav_Resistance) {
            Intent startCurrentActivity = new Intent(this,ResistanceActivity.class);
            startActivity(startCurrentActivity);
        } else if (id == R.id.nav_freqResp) {
            Intent startFreqRespActivity = new Intent(this,FrequencyResponseActivity.class);
            startActivity(startFreqRespActivity);
        }else if (id == R.id.nav_SigGen) {
            Intent startSigGenActivity = new Intent(this,SignalGeneratorActivity.class);
            startActivity(startSigGenActivity);
        }else if (id == R.id.nav_LightIntensity) {
            Intent startLightIntensityActivity = new Intent(this,LightIntensityActivity.class);
            startActivity(startLightIntensityActivity);
        }else if (id == R.id.nav_capacitance) {
            //TODO implement activity
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
