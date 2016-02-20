package com.travisgoodspeed.md380tool;

import android.hardware.usb.UsbManager;
import android.util.Log;

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
    void drawText(String text, int x, int y) throws MD380Exception{
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

        download(1, buf);
    }

    /* Uploads a chunk of data from the given address. */
    byte[] upload_ram(int adr, int length) throws MD380Exception{
        setAddress(adr);
        setAddress(adr);
        setAddress(adr);
        return upload(1,length);
    }

    /* Returns the source and destination of the most recent call by peeking memory.
    * In the returned array,
    * [0] is the outgoing address of this radio.
    * [1] is the source address of the most recent packet.
    * [2] is the destination address of the most recent packet.
    */
    int[] getCallLog() throws MD380Exception{
        byte log[]=upload_ram(0x2001d098,16);
        int toret[]=new int[3];


        Log.d("getCallLog",String.format("Got %d bytes.",log.length));

        toret[0]=(int) (
                log[0]
                +(log[1]<<8)
                +(log[2]<<16)
                +(log[3]<<24)
                );

        toret[1]=(int) (
                log[4]
                +(log[5]<<8)
                +(log[6]<<16)
                +(log[7]<<24)
        );
        toret[2]=(int) (
                log[8]
                +(log[9]<<8)
                +(log[10]<<16)
                +(log[11]<<24)
        );

        return toret;

    }
}
