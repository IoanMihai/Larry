package com.ioanmihai.wall_ebluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class TestActivity extends AppCompatActivity {

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter myBluetooth = null;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    public static String EXTRA_ADDRESS = "device_address";
    private String address, name, newName;
    private ImageView backButton;
    private TextView currentName;
    public static String NEW_NAME = "device_name";
    private EditText editText;
    private Button renameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Intent intent = getIntent();
        address = intent.getStringExtra(FirstPage.EXTRA_ADDRESS);
        name = intent.getStringExtra(FirstPage.NEW_NAME);
        currentName = findViewById(R.id.current_name);
        backButton = findViewById(R.id.back_button);
        editText = findViewById(R.id.name_edit_text);
        renameButton = findViewById(R.id.rename_button);

        new TestActivity.ConnectBT().execute();

        currentName.setText(name);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(getApplicationContext(), FirstPage.class);
                backIntent.putExtra(EXTRA_ADDRESS, address);
                Disconnect();
                startActivity(backIntent);
            }
        });

        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newName = editText.getText().toString();
                Intent renameIntent = new Intent(getApplicationContext(), FirstPage.class);
                Disconnect();
                renameIntent.putExtra(EXTRA_ADDRESS, address);
                renameIntent.putExtra(NEW_NAME, newName);
                startActivity(renameIntent);
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
            }

        }
    }

}