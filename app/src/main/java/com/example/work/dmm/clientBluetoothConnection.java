package com.example.work.dmm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;


/**
 * Created by Work on 28/01/2017.
 */

class clientBluetoothConnection extends Thread implements Serializable{
    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    private final BluetoothDevice bluetoothDevice;
    private final BluetoothSocket bluetoothSocket;
    private final BluetoothAdapter bluetoothAdapter;
    private volatile boolean bool_data_to_write;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] buffer;//input stream buffer.
    private byte[] writeBuffer;
    private Context main_context;
    private volatile Boolean connectionActive = false;
    private volatile int connection_state = 0;// 0:initializing;1:connected;-1:failed.




    clientBluetoothConnection(BluetoothDevice bluetoothDevice, BluetoothAdapter bluetoothAdapter, Context context) {
        this.bluetoothDevice = bluetoothDevice;
        this.bluetoothAdapter = bluetoothAdapter;

        main_context=context;
        BluetoothSocket temp_socket;
        try{
            temp_socket = (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(this.bluetoothDevice,1);
        }catch (Exception e) {
            bluetoothSocket = null;
            return;
        }
        bluetoothSocket = temp_socket;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
// Establishing connection, and listening for incoming data [this will run in created thread]
////////////////////////////////////////////////////////////////////////////////////////////////////
    public void run(){
        //stopping bluetooth discovery
        bluetoothAdapter.cancelDiscovery();
        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            //connection failed
            e.printStackTrace();
            connection_state = -1;
            close_connection();
            return;
        }
        try {
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            connectionActive = true;
        } catch (IOException e) {
            e.printStackTrace();
            connection_state = -1;
        }
        //connection established, start reading input
        connection_state = 1;
        int number_of_bytes_read;
        while(connectionActive){
            try {
                //see if there is data to be read
                if (inputStream.available() > 0){//read from the stream, will block thread until data received
                    buffer = new byte[1024];
                    number_of_bytes_read = inputStream.read(buffer);
                    if (number_of_bytes_read > 0){
                        Intent read_data_Intnet = new Intent();
                        read_data_Intnet.setAction(MessageCode.CUSTOM_ACTION_SERIAL);
                        read_data_Intnet.putExtra(MessageCode.MSG_READ_DATA,buffer);
                        main_context.sendBroadcast(read_data_Intnet);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(bool_data_to_write){
                try {
                    outputStream.write(writeBuffer);//
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bool_data_to_write = false;
            }
        }

    }

////////////////////////////////////////////////////////////////////////////////////////////////////
// Connection Management, and writing to other device [this will run in UI thread when called]
////////////////////////////////////////////////////////////////////////////////////////////////////

    void close_connection(){
        try {
            connectionActive = false;
            inputStream.close();
            outputStream.close();
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean write(byte[] msg_data){
        if(bool_data_to_write){
            return false;//busy
        }else{
            writeBuffer = msg_data;
            bool_data_to_write = true;
        }
        return true;
    }

    public boolean isConnectionActive(){
        return bluetoothSocket.isConnected();
    }

    public int get_connection_state(){
        return connection_state;
    }

}
