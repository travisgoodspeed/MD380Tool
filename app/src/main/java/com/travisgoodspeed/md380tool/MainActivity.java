package com.travisgoodspeed.md380tool;

import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import layout.DmesgFragment;
import layout.HomeFragment;
import layout.LogFragment;


/**
 * Created by tgoodspeed on 2/19/16.
 */






public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DmesgFragment.OnFragmentInteractionListener{
    //Button btnCheck;
    //TextView textInfo;

    //This points to our global tool.
    public static MD380Tool tool=null;
    //This is ugly, but so it goes.
    public static MainActivity selfy=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Record our static handle for the other views.
        selfy=this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Fragment fragment = null;

        Class fragmentClass;

        fragmentClass = HomeFragment.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // old stuff
        /*
        btnCheck = (Button) findViewById(R.id.check);
        textInfo = (TextView) findViewById(R.id.info);
        btnCheck.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getPermissions();

            }
        });
        */

    }

    private final String ACTION_USB_PERMISSION="com.travisgoodspeed.md380tool.USB_INTENT";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            try {
                                //if(tool==null)
                                tool = new MD380Tool((UsbManager) getSystemService(Context.USB_SERVICE));
                                if (tool.connect()) {
                                    int[] log=tool.getCallLog();
                                    //textInfo.setText(String.format("DMR call from %d to %d.\n",
                                    //        log[1], log[2])+textInfo.getText());

                                    tool.drawText("Done!",160,50);

                                } else {
                                    //textInfo.setText("Failed to connect.");
                                }
                            }catch(MD380Exception e){
                                Log.e("MD380",e.getMessage());
                                e.printStackTrace();
                                //textInfo.setText(e.getMessage());
                                tool.disconnect();
                            }
                        }
                    } else {
                        //textInfo.setText("Device permission denied.");
                    }
                }
            }
        }
    };


    //Requests permissions to the target device.
    public void getPermissions(){
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);;
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);



        String i = "";
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if(device.getVendorId()==0x0483 && device.getProductId()==0xDF11){
                manager.requestPermission(device, pendingIntent);
                //textInfo.setText("Found device and requested permissions.");
            }
        }
        //textInfo.setText("Device not found.");
        return;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Create a new fragment and specify the planet to show based on
        // position
        Fragment fragment = null;

        Class fragmentClass;
        switch(item.getItemId()) {
            case R.id.nav_manage:
                fragmentClass = HomeFragment.class;
                Log.w("Nav", "Home fragment.");
                break;
            case R.id.nav_log:
                fragmentClass = LogFragment.class;
                Log.w("Nav", "Log fragment.");
                break;
            case R.id.nav_dmesg:
                fragmentClass = DmesgFragment.class;
                Log.w("Nav", "Dmesg fragment.");
                break;
            default:
                fragmentClass = HomeFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        /*
        if (id == R.id.nav_manage) {
            Log.w("myApp", "manage");
        } else if (id == R.id.nav_log) {
            Log.w("myApp", "log");
        }
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void doConnect(View view){

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("Huh","Some misunderstood interaction with "+uri);
    }
}
