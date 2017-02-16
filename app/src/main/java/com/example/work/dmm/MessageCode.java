package com.example.work.dmm;

/**
 * Created by Work on 29/01/2017.
 */

abstract class MessageCode {
    public static final String MSG_READ_DATA = "READ_DATA";
    public static final String PARSED_DATA_DC_VOLTAGE = "PARSED_DATA_DC_VOLTAGE";
    public static final int MSG_SOCKET_CONNECTION_FAILED = -1;
    public static final int MSG_SOCKET_RFCOM_FAILED = -2;
    public static final String CUSTOM_ACTION_SERIAL = "CUSTOM_ACTION_SERIAL";
    //startActivityForResult request code for enabling bluetooth
    public static final int ENABLE_BLUETOOTH_REQ = 3;
    public static final int MAX_MSG_LENGTH = 100;//maximum size of single a message in bytes
    public static final char MSG_OPENING_TAG = '<';
    public static final char MSG_CLOSING_TAG = '>';
    public static final String MSG_READ_DATA_SIZE = "MSG_READ_DATA_SIZE";
    public static final String VALUE = "VALUE";
    public static final String RANGE = "RANGE";
}
