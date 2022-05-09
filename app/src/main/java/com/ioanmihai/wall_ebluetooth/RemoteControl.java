package com.ioanmihai.wall_ebluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class RemoteControl extends AppCompatActivity {

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter myBluetooth = null;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    public static String EXTRA_ADDRESS = "device_address";
    private String address;
    private ImageView backButton;
    private CardView up, down, left, right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);

        Intent intent = getIntent();
        address = intent.getStringExtra(FirstPage.EXTRA_ADDRESS);
        Toast.makeText(getApplicationContext(), address, Toast.LENGTH_LONG).show();

        backButton = findViewById(R.id.back_button);

        new RemoteControl.ConnectBT().execute();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(RemoteControl.this, FirstPage.class);
                backIntent.putExtra(EXTRA_ADDRESS, address);
                Disconnect();
                startActivity(backIntent);
            }
        });

        up = findViewById(R.id.arrow_up);
        down = findViewById(R.id.arrow_down);
        right = findViewById(R.id.arrow_right);
        left = findViewById(R.id.arrow_left);

        up.setCardBackgroundColor(Color.TRANSPARENT);
        down.setCardBackgroundColor(Color.TRANSPARENT);
        right.setCardBackgroundColor(Color.TRANSPARENT);
        left.setCardBackgroundColor(Color.TRANSPARENT);

        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    up.setCardBackgroundColor(Color.BLACK);
                    sendSignal("UP");
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    up.setCardBackgroundColor(Color.TRANSPARENT);
                    sendSignal("UPP");
                }
                return true;
            }

        });

        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    down.setCardBackgroundColor(Color.BLACK);
                    sendSignal("DOWN");
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    down.setCardBackgroundColor(Color.TRANSPARENT);
                    sendSignal("DOWNN");
                }
                return true;
            }
        });

        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    right.setCardBackgroundColor(Color.BLACK);
                    sendSignal("RIGHT");
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    right.setCardBackgroundColor(Color.TRANSPARENT);
                    sendSignal("RIGHTT");
                }
                return true;
            }
        });

        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    left.setCardBackgroundColor(Color.BLACK);
                    sendSignal("LEFT");
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    left.setCardBackgroundColor(Color.TRANSPARENT);
                    sendSignal("LEFTT");
                }
                return true;
            }
        });

    }

    private void Disconnect(){
        if (btSocket != null){
            try{
                btSocket.close();
            }catch(IOException e){
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }

    private void sendSignal(String signal){
        if (btSocket != null){
            try{
                btSocket.getOutputStream().write(signal.getBytes());
            }catch(IOException e){
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                if (btSocket == null || !isBtConnected){
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = myBluetooth.getRemoteDevice(address);
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }catch (IOException e){
                ConnectSuccess = false;
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            if (!ConnectSuccess){
                Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_LONG).show();
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                isBtConnected = true;
            }

        }
    }

}