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
    public void drawText(String text, int x, int y) throws MD380Exception{
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
    public byte[] upload_ram(int adr, int length) throws MD380Exception{
        setAddress(adr);
        return upload(1,length);
    }

    /* Uploads the latest dmesg log and clears the buffer from the device. */
    public String getDmesg() throws MD380Exception{
        //First we use DNLOAD to set the address.
        byte buf[] = new byte[1];
        buf[0]=0x00; //DMESG command.
        download(1,buf);

        //Then we fetch it with an UPLOAD.
        byte dmesgbuf[]=upload(1,1024);

        //And we re-order it if there has been an overflow.
        String tail="";
        String head=null;
        for(int i=0;i<1024;i++){
            int b=((int) dmesgbuf[i])&0xFF;
            if(head==null){
                if(b>0) //If the character isn't null, we append it.
                    tail=tail+String.format("%c",b);
                else //otherwise, we initialize the head and hope the later characters are.
                    head="";
            }else{
                if(b>0)
                    head=head+String.format("%c",b);
                else
                    break;
            }
        }

        //Return the linearized buffer.
        if(head==null)
            return tail;
        return head+tail;
    }

    /* Returns the source and destination of the most recent call by peeking memory.
    * In the returned array,
    * [0] is the outgoing address of this radio.
    * [1] is the source address of the most recent packet.
    * [2] is the destination address of the most recent packet.
    */
    public int[] getCallLog() throws MD380Exception{
        byte log[]=upload_ram(0x2001d098,16);
        int toret[]=new int[3];


        Log.d("getCallLog",String.format("Got %d bytes.",log.length));

        toret[0]=intfrombytes(log,0);
        toret[1]=intfrombytes(log,4);
        toret[2]=intfrombytes(log,8);

        return toret;

    }
}
