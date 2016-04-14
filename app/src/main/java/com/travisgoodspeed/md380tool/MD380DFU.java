package com.travisgoodspeed.md380tool;


import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.os.AsyncTask;
import android.util.Log;

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
    UsbDeviceConnection connection=null;
    UsbInterface usbInterface;

    static final int VID=0x0483;
    static final int PID=0xDF11;

    //Control Requests
    static final int DETACH=0;
    static final int DNLOAD=1;
    static final int UPLOAD=2;
    static final int GETSTATUS=3;
    static final int CLRSTATUS=4;
    static final int GETSTATE=5;
    static final int ABORT=6;


    MD380DFU(UsbManager manager){
        this.manager=manager;
    }

    private boolean connected=false;
    public boolean isConnected(){
        return connected;
    }

    /* Don't call this until after permission has been granted.*/
    public boolean connect() throws MD380Exception{
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

                Log.d("MD380","Trying to open interface on 0");
                usbInterface = device.getInterface(0);
                if (usbInterface==null || !connection.claimInterface(usbInterface, true)) {
                    Log.d("MD380","Could not claim interface on 0");
                    return false;
                }

                //We're good, so return true.
                Log.d("MD380","Connected");
                connected=true;
                return true;
            }
        }

        //Couldn't connect.
        return false;
    }

    public void disconnect(){
        connected=false;
        connection.close();
    }


    /* Gets the DFU status. */
    public byte[] getStatus() throws MD380Exception{
        byte buf[]=new byte[6];
        int len=0;

        if(connection.controlTransfer(0xA1,GETSTATUS,0,0,buf,6,3000)<0) {
            connected=false;
            throw new MD380Exception("Transfer Error");
        }

        return buf;
    }

    /* Gets the DFU state. */
    public int getState() throws MD380Exception{
        byte[] buf=new byte[1];
        if(connection.controlTransfer(0xA1,GETSTATE,0,0,buf,1,3000)<0) {
            connected=false;
            throw new MD380Exception("Transfer Error");
        }
        return (int) buf[0];
    }

    /* Sets the DFU target address. */
    public void setAddress(int address) throws MD380Exception{
        byte buf[]=new byte[5];

        //Secret command code to set the address when writing to Block 0.
        buf[0]=0x21;
        //Little-endian representation of the address.
        buf[1]=(byte) (address&0xFF);
        buf[2]=(byte) ((address>>8)&0xFF);
        buf[3]=(byte) ((address>>16)&0xFF);
        buf[4]=(byte) ((address>>24)&0xFF);


        download(0, buf);

        return;
    }



    /* Detaches from the target.  The STM32's DFU will execute the application when this is called. */
    void detach(){
        //This will probably timeout.  So it goes.
        connection.controlTransfer(0x21, DETACH, 0, 0, null, 0, 3000);
    }

    /* Uploads data from the radio at the target address. */
    public byte[] upload(int block, int length) throws MD380Exception{
        byte[] data=new byte[length];

        //Log.d("dfu","Uploading block "+block+" of length "+length);

        if(connection.controlTransfer(0xA1,UPLOAD,block,0,data,length,3000)<0) {
            connected=false;
            throw new MD380Exception("Transfer Error");
        }

        getStatus();

        return data;
    }

    /* Gets the command response. */
    byte[] getCommand() throws MD380Exception{
        /* The command block comes from block zero.  The size is always 32. */
        //byte[] toret=upload(0, 5);
        byte[] toret=upload(0, 5);
        Log.d("getCommand()", "Status of " + bytes2hexstr(toret));
        return toret;

    }

    /* Aborts the current transaction. */
    public void abort() throws MD380Exception{
        byte data[]=null;
        connection.controlTransfer(0x21, ABORT, 0, 0, data,0, 3000);
    }

    /* Downloads data to a target block. */
    public byte[] download(int block, byte buf[]) throws MD380Exception{
        //Log.d("DNLOAD", bytes2hexstr(buf, buf.length < 32 ? buf.length : 16));
        if(connection.controlTransfer(0x21,DNLOAD,block,0,buf,buf.length,3000)<0)
            throw new MD380Exception("Transfer Error");
        //First we apply the change.
        getStatus();
        /*
        try{
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //Then we return the result.
        return getStatus();
    }

    /* Calls an MD380 custom DFU command. */
    public void md380cmd(byte a, byte b) throws MD380Exception{
        byte data[]=new byte[2];
        data[0]=a;
        data[1]=b;
        byte[] res;
        res = download(0, data);
        //Log.d("md380cmd",": " +res[2]);
    }

    /* Reboots the radio. */
    public void reboot() throws MD380Exception{
        //This is one of the custom commands in the 91 series.
        md380cmd((byte) 0x91, (byte) 0x05);
    }
    /* Halts all threads and displays "Programming Mode" on the screen. */
    public void programMode() throws MD380Exception{
        //This is one of the custom commands in the 91 series.
        md380cmd((byte) 0x91, (byte) 0x01);
    }

    /* Enters into DFU Mode. */
    public void enterDfuMode() throws MD380Exception{
        while(true) {
            int state = getState();
            switch(state){
                case 2://dfuIDLE
                    return;//We're done!
                case 5://DNLOAD_IDLE
                case 3://DNLOAD_SYNC
                case 6://MANIFEST SYNC
                case 9://UPLOAD IDLE
                    abort();
                    break;
                default:
                    Log.d("enterDfuMode()","Unhandled state "+state);

            }
        }
    }

    //Convenience function that hexdumps some data.
    public static String bytes2hexstr(byte[] data){
        String str="";
        int i,j;
        for(i=0;i<data.length;i++){
            byte b=data[i];
            //str=str+Byte.toString(b)+" ";
            str=str+String.format("%02x ",b);
            if(i%32==16)
                str=str+" ";
            if(i%32==31)
                str=str+"\n";
        }
        return str;
    }
    //Convenience function that hexdumps some data.
    public static String bytes2hexstr(byte[] data, int len){
        String str="";
        int i,j;
        for(i=0;i<len;i++){
            byte b=data[i];
            //str=str+Byte.toString(b)+" ";
            str=str+String.format("%02x ",b);
            if(i%32==16)
                str=str+" ";
            if(i%32==31)
                str=str+"\n";
        }
        return str;
    }

    //Convenience function to grab the unsigned value of a byte.
    public static int u8(byte b){
        return ((int)b)&0xFF;
    }
    //Convenience function to yank a 32-bit word from a byte array.
    public static int intfrombytes(byte[] data, int i){
        int j=0;
        j= (int) (
                 u8(data[i])
                         | (u8(data[i+1])<<8)
                |(u8(data[i+2])<<16)
                |(u8(data[i+3])<<24)
        );
        return j;
    }

    /* Erases a block, and by side effect sets the target address. */
    public void eraseBlock(int address) throws MD380Exception{
        byte buf[]=new byte[5];

        //Secret command code to set the address when writing to Block 0.
        buf[0]=0x41;
        //Little-endian representation of the address.
        buf[1]=(byte) (address&0xFF);
        buf[2]=(byte) ((address>>8)&0xFF);
        buf[3]=(byte) ((address>>16)&0xFF);
        buf[4]=(byte) ((address>>24)&0xFF);

        download(0,buf);
        return;
    }

    /* This function performs a complete upgrade.  For obvious reasons, it oughtn't run
       inside the rendering thread, and you oughtn't run other
     */
    public void upgradeApplication(byte[] upgrade) throws MD380Exception {
        upgradeApplicationInit(upgrade);
        while(!upgradeApplicationNextStep());
    }

    private ByteBuffer codeplugBuf=null;
    public MD380Codeplug uploadCodeplug(){
        MD380Codeplug codeplug=null;

        codeplugBuf=ByteBuffer.allocate(262144);
        try {
            //Enter DFU Mode
            enterDfuMode();
            //Enter programming mode and select SPI memory.
            md380cmd((byte) 0x91, (byte) 0x01);//Programming mode.
            md380cmd((byte) 0xa2, (byte) 0x02);
            //getStatus();
            //getCommand();
            md380cmd((byte) 0xa2, (byte) 0x02);
            md380cmd((byte) 0xa2, (byte) 0x03);
            md380cmd((byte) 0xa2, (byte) 0x04);
            md380cmd((byte) 0xa2, (byte) 0x07);

            //Move to the beginning of the codeplug.
            setAddress(0x00000000);
            enterDfuMode();

            int blocksize=1024;
            for(int blockadr=2;blockadr<0x102;blockadr++){
                byte[] data=upload(blockadr,blocksize);
                //Log.d("Codeplug","Got "+data.length+" bytes of the codeplug.");
                //getStatus();
                if(data.length!=blocksize){
                    Log.e("Codeplug","Block was "+data.length+" bytes.  Should have been "+blocksize+".");
                    return null;
                }
                codeplugBuf.put(data.clone());
            }

            //Now dump the buffer.
            byte codeplugData[]=new byte[262144];
            //codeplugBuf.get(codeplugData,0,262144);
            codeplugBuf.rewind();
            codeplugBuf.get(codeplugData);

            codeplug=new MD380Codeplug(codeplugData);
        } catch (MD380Exception e) {
            e.printStackTrace();
            return null;
        }
        return codeplug;
    }

    public void downloadCodeplug(MD380Codeplug codeplug){
        byte[] data=codeplug.getImage();
        if(data.length!=262144){
            Log.e("Codeplug","Refusing to send a codeplug of "+data.length+" bytes.");
        }

        Log.e("Codeplug","Refusing to send a codeplug because I don't know what the hell I'm doing.");

        return;
    }

    private ByteBuffer upgradeBuf=null;
    private int upgradeAddress=0;
    public void upgradeApplicationInit(byte[] upgrade) throws MD380Exception{
        //Check the filesize.
        if(upgrade.length!=994816){
            Log.e("upgradeApplication","Update is "+upgrade.length+" bytes, not 994816.  Aborting.");
            return;
        }

        //Enter programming mode and select flash memory.
        md380cmd((byte) 0x91, (byte) 0x01);
        md380cmd((byte) 0x91, (byte) 0x31);

        //Erase the old application.
        eraseBlock(0x0800c000);
        for(int i=0x08010000; i<0x080f0000; i+=0x10000)
            eraseBlock(i);

        //Write in the new application.
        upgradeBuf=ByteBuffer.wrap(upgrade);
        int blocksize=1024;
        byte[] block=new byte[blocksize];

        //Point at the beginning of flash.
        upgradeAddress=0x0800c000;
        setAddress(0x0800c000);

        //Skip file header, which begins with "OutSecurityBin"
        upgradeBuf.get(block, 0, 0x100);
        
    }

    /* Performs an upgrade step, returning true when the upgrade has been completed. */
    public boolean upgradeApplicationNextStep() throws MD380Exception{
        int blocksize=1024;
        int toget=1024;
        byte[] block=new byte[blocksize];

        if(upgradeBuf.remaining()<toget)
            toget=upgradeBuf.remaining();

        try {
            upgradeBuf.get(block, 0, toget);
        }catch(BufferUnderflowException e){
            //We don't care about an underflow, just write what we've got.
            Log.e("Mismatch","Ignoring a BufferUnderflowException");
        }

        //Write it to the MD380's flash, starting with blockadr=2.
        setAddress(upgradeAddress);
        int adr=2; //(i-0x0800C000)/1024+2;
        download(adr, block);

        upgradeAddress=upgradeAddress+toget;

        return !upgradeBuf.hasRemaining();
    }
}









