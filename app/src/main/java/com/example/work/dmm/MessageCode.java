package com.example.work.dmm;

/**
 * Created by Work on 29/01/2017.
 */

abstract class MessageCode {
    public static final int MSG_READ_DATA = 1;
    public static final int MSG_SOCKET_CONNECTION_FAILED = -1;
    public static final int MSG_SOCKET_RFCOM_FAILED = -2;
    public static final String CUSTOM_ACTION_SERIAL = "CUSTOM_ACTION_SERIAL";
    //startActivityForResult request code for enabling bluetooth
    public static final int ENABLE_BLUETOOTH_REQ = 3;
}
