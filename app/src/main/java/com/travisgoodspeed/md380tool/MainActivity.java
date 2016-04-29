package com.travisgoodspeed.md380tool;

import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;


import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import layout.CloneFragment;
import layout.CodeplugFragment;
import layout.DmesgFragment;
import layout.HomeFragment;
import layout.LogFragment;
import layout.MessageEditFragment;
import layout.UpgradeFragment;


/**
 * Created by tgoodspeed on 2/19/16.
 *
 * This is the main activity window for all MD380 interactions on Android.
 */






public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DmesgFragment.OnFragmentInteractionListener,
        UpgradeFragment.OnFragmentInteractionListener,
        CodeplugFragment.OnFragmentInteractionListener,
        ContactsFragment.OnListFragmentInteractionListener,
        MessagesFragment.OnListFragmentInteractionListener,
        CloneFragment.OnFragmentInteractionListener,
        MessageEditFragment.OnFragmentInteractionListener

{
    //This points to our global tool.
    public static MD380Tool tool=null;
    //This is ugly, but so it goes.
    public static MainActivity selfy=null;
    //Codeplug database.
    public static MD380CodeplugDB db=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Record our static handle for the other views.
        selfy=this;
        if(db==null)
            db=new MD380CodeplugDB(this.getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                                tool.connect();
                            }catch(MD380Exception e){
                                Log.e("MD380",e.getMessage());
                                e.printStackTrace();
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


    /* Requests permissions to the target device.
       Return true if the permissions were requested, false if device not found.
     */
    public boolean getPermissions(){
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
                return true;
            }
        }
        return false;
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

        Class fragmentClass=null;

        //Panes which do not require a connection.
        switch(item.getItemId()){
            case R.id.nav_manage:
                fragmentClass = HomeFragment.class;
                Log.w("Nav", "Home fragment.");
                break;
            case R.id.nav_contacts:
                fragmentClass = ContactsFragment.class;
                Log.w("Nav", "Contacts fragment.");
                break;

            case R.id.nav_messages:
                fragmentClass = MessagesFragment.class;
                Log.w("Nav", "Messages fragment.");
                break;

        }

        //Only allow new tabs if the connection exists.
        if(fragmentClass==null && (tool==null || !tool.isConnected())){
            fragmentClass = HomeFragment.class;
            Log.w("Nav", "Forcing home for broken connection.");
        }

        if(fragmentClass==null) switch(item.getItemId()) {
            case R.id.nav_codeplug:
                fragmentClass = CodeplugFragment.class;
                Log.w("Nav", "Codeplug fragment.");
                break;
            case R.id.nav_codeplugclone:
                fragmentClass = CloneFragment.class;
                Log.w("Nav", "Codeplug Cloner");
                break;
            case R.id.nav_log:
                fragmentClass = LogFragment.class;
                Log.w("Nav", "Log fragment.");
                break;
            case R.id.nav_dmesg:
                fragmentClass = DmesgFragment.class;
                Log.w("Nav", "Dmesg fragment.");
                break;
            case R.id.nav_upgrade:
                fragmentClass = UpgradeFragment.class;
                Log.w("Nav", "Upgrade fragment.");
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("Huh","Some misunderstood interaction with "+uri);
    }

    @Override
    public void onListFragmentInteraction(MD380Contact item) {
        Log.d("MainActivity","TODO Implement a contact viewer/editor.");
    }
    @Override
    public void onListFragmentInteraction(MD380Message item) {
        Log.d("MainActivity","TODO Implement a message viewer/editor.");
        FragmentManager fragmentManager=getSupportFragmentManager();
        Fragment fragment=MessageEditFragment.newInstance(item);
        fragmentManager.beginTransaction().replace(R.id.flContent,fragment).commit();
    }
}
