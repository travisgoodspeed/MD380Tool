package com.travisgoodspeed.md380tool;

/**
 * Created by travis on 2/20/16.
 *
 * This is thrown whenever there is a DFU exception.
 */
public class MD380Exception extends Exception {
    MD380Exception(String s){
        super(s);
    }
}
