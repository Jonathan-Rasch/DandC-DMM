package com.example.work.dmm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;


/**
 * Created by Work on 28/01/2017.
 */

class clientBluetoothConnection extends Thread implements Serializable{
    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    private  BluetoothDevice bluetoothDevice;
    private  BluetoothSocket bluetoothSocket;
    private  BluetoothAdapter bluetoothAdapter;
    private volatile boolean bool_data_to_write;
    private InputStream inputStream;
    private OutputStream outputStream;
    private byte[] buffer;//input stream buffer.
    private byte[] writeBuffer;
    private Context main_context;
    private volatile Boolean connectionActive = false;


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
            return;
        }
        try {
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            connectionActive = true;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        //connection established, start reading input
        while(connectionActive){
            try {
                //see if there is data to be read
                if (inputStream.available() > 0){//read from the stream, will block thread until data received
                    buffer = new byte[1024];
                    number_of_bytes_read = inputStream.read(buffer);
                    parse_and_send(buffer);
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
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
// Connection Management, and writing to other device [this will run in UI thread when called]
////////////////////////////////////////////////////////////////////////////////////////////////////

    public void close_connection(){
        connectionActive = false;
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

////////////////////////////////////////////////////////////////////////////////////////////////////
// read data parsing and sending
////////////////////////////////////////////////////////////////////////////////////////////////////

    private void send_received_data(byte[] byte_array,int size){
        if (byte_array != null && size>0) {
            //creating byte array of correct size
            byte[] data = new byte[size];
            for (int i=0;i<size;i++){
                data[i]=byte_array[i];
            }
            //sending data
            Intent read_data_Intent = new Intent();
            read_data_Intent.setAction(MessageCode.CUSTOM_ACTION_SERIAL);
            read_data_Intent.putExtra(MessageCode.MSG_READ_DATA,data);
            read_data_Intent.putExtra(MessageCode.MSG_READ_DATA_SIZE,size);
            main_context.sendBroadcast(read_data_Intent);
            Log.d("message sent:",new String(data));
        }
    }

    int number_of_bytes_read;
    byte[] message=new byte[MessageCode.MAX_MSG_LENGTH];
    int message_index=0;
    boolean partial_message_present = false;//if read data contains no closing tag for msg

    private void parse_and_send(byte[] local_buffer){
        for (int i=0;i<number_of_bytes_read;i++){
            if (!partial_message_present) {
                //look for starting character < (#60)
                if (local_buffer[i] == MessageCode.MSG_OPENING_TAG){
                    //creating new message
                    i++;//advancing index
                    partial_message_present = true;
                    message = new byte[MessageCode.MAX_MSG_LENGTH];
                    message[0]=MessageCode.MSG_OPENING_TAG;//adding opening tag to msg
                    for (message_index=0;//resetting message index
                         message_index < MessageCode.MAX_MSG_LENGTH && i < number_of_bytes_read;
                         message_index++){
                        if (local_buffer[i] == MessageCode.MSG_CLOSING_TAG){
                            //closing tag,MESSAGE COMPLETE
                            partial_message_present = false;
                            message[message_index]=MessageCode.MSG_CLOSING_TAG;//adding opening tag to msg
                            send_received_data(message,message_index+1);
                            break;
                        }else if(local_buffer[i] == MessageCode.MSG_OPENING_TAG){
                        /*something went very wrong. a partial message is present and not
                        * completed, but a new message has started. disregard partial message
                        * and set the index i one position back so the opening tag is picked
                        * up in the next cycle where the new message will be parsed*/
                            i--;
                            partial_message_present=false;
                            break;
                        }else {
                            //prevent non ASCII characters from being added to message
                            if(local_buffer[i] >= 32 && local_buffer[i] <= 126) {
                                message[message_index] = local_buffer[i];
                            }else{
                                message_index--;
                            }
                            //no closing tag yet, increase index i.
                            i++;
                        }
                    }
                }
            } else {
                while(message_index<MessageCode.MAX_MSG_LENGTH
                        && i<number_of_bytes_read){
                    message_index++;//message index needs to increase first,points to last filled index
                    if (local_buffer[i] == MessageCode.MSG_CLOSING_TAG){
                        //partial message completed
                        partial_message_present=false;
                        message[message_index]=MessageCode.MSG_CLOSING_TAG;//adding opening tag to msg
                        send_received_data(message,message_index+1);
                        break;
                    }else if(local_buffer[i] == MessageCode.MSG_OPENING_TAG){
                    /*something went very wrong. a partial message is present and not
                    * completed, but a new message has started. disregard partial message
                    * and set the index i one position back so the opening tag is picked
                    * up in the next cycle where the new message will be parsed*/
                        i--;
                        partial_message_present=false;
                        break;
                    }else{
                        //prevent non ASCII characters from being added to message
                        if(local_buffer[i] >= 32 && local_buffer[i] <= 126) {
                            message[message_index] = local_buffer[i];
                        }else{
                            message_index--;
                        }
                        //no closing tag yet, increase index i.
                        i++;
                    }
                }
            }
        }
    }


}
