package com.travisgoodspeed.md380tool;

import android.hardware.usb.UsbManager;

/**
 * Created by tgoodspeed on 2/19/16.
 *
 * This class will implement the custom MD380Tool extensions to the MD380's
 * DFU protocol.  Most of these routines will not work with a stock radio.
 */
public class MD380Tool extends MD380DFU {
    MD380Tool(UsbManager manager){
        super(manager);
    }

    /* Draws text to the MD380's screen using a hooked DNLOAD handler. */
    boolean drawText(String text, int x, int y){
        byte buf[];
        int len=0;

        buf=new byte[text.length()*2+3];

        buf[0]=(byte) 0x80;  //DrawText Command
        buf[1]=(byte) x;
        buf[2]=(byte) y;
        for(int i=0;i<text.length();i++){
            buf[3+2*i]=(byte) text.charAt(i);
            buf[3+2*i+1]=0;
        }

        if(connection.controlTransfer(0x21,DNLOAD,1,0,buf,buf.length,3000)<0)
            return false;

        return true;
    }
}
