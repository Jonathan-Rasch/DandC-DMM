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
import android.widget.TextView;

public class ConnectionScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BaseApplication base;
    private TextView tv_device_address,tv_device_name,tv_received_data;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MessageCode.PARSED_DATA_VOLTAGE.equals(intent.getAction())) {
                String message = intent.getStringExtra(MessageCode.PARSED_DATA_VOLTAGE);
                tv_received_data.setText(tv_received_data.getText() + "\n" + message);
            }
        }
    };

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
        IntentFilter filter = new IntentFilter(MessageCode.PARSED_DATA_VOLTAGE);
        registerReceiver(receiver,filter);
        //getting views
        base = (BaseApplication)getApplicationContext();
        tv_device_address = (TextView)findViewById(R.id.connectionScreen_device_address);
        tv_device_name = (TextView)findViewById(R.id.connectionScreen_device_name);
        tv_received_data = (TextView)findViewById(R.id.tv_received_data);
        tv_device_address.setText(base.getDeviceAddress());
        tv_device_name.setText(base.getDeviceName());

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            base.drop_connection();
            //super.onBackPressed();
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
            return true;
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

        } else if (id == R.id.nav_disconnect) {

        } 

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
