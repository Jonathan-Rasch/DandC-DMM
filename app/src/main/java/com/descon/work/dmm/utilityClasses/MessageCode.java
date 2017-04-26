package com.descon.work.dmm.utilityClasses;

/**
 * Created by Work on 29/01/2017.
 */

public abstract class MessageCode {
    /*message codes for sending the received data to the relevant activity (ie voltage or current)*/
    public static final String MSG_READ_DATA = "READ_DATA";
    public static final String PARSED_DATA_DC_VOLTAGE = "PARSED_DATA_DC_VOLTAGE";
    public static final String PARSED_DATA_DC_CURRENT = "PARSED_DATA_DC_CURRENT";
    public static final String PARSED_DATA_RESISTANCE = "PARSED_DATA_RESISTANCE";
    public static final String PARSED_DATA_FREQ_RESP = "PARSED_DATA_FREQ_RESP";
    public static final String SIGGEN_ACK = "PARSED_DATA_FREQ_RESP";//sent periodically from DMM to app to confirm correct siggen settings
    public static final String PARSED_LIGHT_INTENSITY = "PARSED_LIGHT_INTENSITY";
    public static final String PARSED_CAPACITANCE = "PARSED_CAPACITANCE";
    public static final String PARSED_DIODE_VOLTAGE = "PARSED_DIODE_VOLTAGE";
    public static final String ERROR = "ERROR";

    public static final String DMM_CHANGE_MODE_REQUEST = "DMM_CHANGE_MODE_REQUEST";
    public static final String MODE = "MODE";
    public static final int DC_VOLTAGE_MODE = 1;
    public static final int DC_CURRENT_MODE = 2;
    public static final int RESISTANCE_MODE = 3;
    public static final int FREQ_RESP_MODE = 4;
    public static final int SIG_GEN_MODE = 5;
    public static final int LIGHT_INTENSITY_MODE = 6;
    public static final int CAPACITANCE_MODE = 7;
    public static final int DIODE_MODE = 8;
    /*codes for extras of the FREQ RESP packets*/
    public static final String FREQ_RESP_START_FREQ = "FREQ_RESP_START_FREQ";
    public static final String FREQ_RESP_END_FREQ = "FREQ_RESP_END_FREQ";
    public static final String FREQ_RESP_STEPS = "FREQ_RESP_STEPS";
    /*codes for extras of SIG GEN packets*/
    public static final String SIGGEN_SIGTYPE = "SIGGEN_SIGTYPE";
    public static final String SIGGEN_FREQ = "SIGGEN_FREQ";
    public static final String SIGGEN_AMPL = "SIGGEN_AMPL";
    /*Misc extra data codes*/
    public static final String TIMESTAMP = "TIMESTAMP";

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
    public static final String EXTRA = "EXTRA";
}
