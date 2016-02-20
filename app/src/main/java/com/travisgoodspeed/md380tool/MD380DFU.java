package com.travisgoodspeed.md380tool;


import java.util.HashMap;
import java.util.Iterator;

import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.util.Log;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

/**
 * Created by tgoodspeed on 2/19/16.
 *
 * This class will implement the Device Firmware Update protocol, as well as the standard MD380
 * extensions for use in Android.  MD380Tool extensions are managed by the MD380Tool class,
 * not by this one.
 */
public class MD380DFU {
    //This is passed from the GUI activity.
    UsbManager manager;
    //This is the connection to the device, which requires permissions.
    UsbDeviceConnection connection;


    static final int VID=0x0483;
    static final int PID=0xDF11;

    //Control Requests
    static final int DNLOAD=1;
    static final int UPLOAD=2;



    MD380DFU(UsbManager manager){
        this.manager=manager;
    }

    boolean isConnected(){
        return false;
    }

    /* Don't call this until after permission has been granted.*/
    boolean connect(){
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        String i = "";
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if(device.getVendorId()==VID &&
                    device.getProductId()==PID){
                //If you haven't gotten permission yet, this will crash.

                //Open the device.
                connection=manager.openDevice(device);



                //Close the device.
                //connection.close();

                //Brag about it.
                return true;
            }
        }

        //Couldn't connect.
        return false;
    }

    void disconnect(){
        connection.close();
    }
}
