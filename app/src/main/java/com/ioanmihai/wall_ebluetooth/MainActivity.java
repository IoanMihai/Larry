package com.ioanmihai.wall_ebluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    private RecyclerView deviceList;
    private GridLayoutManager gridLayoutManager;
    private RobotAdapter adapter;

    private static final int BLUETOOTH_PERMISSION_CODE = 100;
    private static final int BLUETOOTH_CONNECT_CODE = 101;
    private static final int LOCATION_PERMISSION_CODE = 102;
    private static final int BLUETOOTH_ADMIN_CODE = 103;
    private static final int LOCATION_COARSE = 104;


    @RequiresApi(api = 31)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_PERMISSION_CODE);
        checkPermission(Manifest.permission.BLUETOOTH_CONNECT, BLUETOOTH_CONNECT_CODE);
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_CODE);
        checkPermission(Manifest.permission.BLUETOOTH_ADMIN, BLUETOOTH_ADMIN_CODE);
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_COARSE);

        deviceList = (RecyclerView) findViewById(R.id.my_robots);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        paireddevicesList();

        if (myBluetooth == null){
            Toast.makeText(getApplicationContext(), "The selected device is not available...", Toast.LENGTH_LONG).show();
            finish();
        }else if (!myBluetooth.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }

        gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        deviceList.setLayoutManager(gridLayoutManager);
        deviceList.setHasFixedSize(true);

    }

    public void checkPermission(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {permission}, requestCode);
        }
    }


    private void paireddevicesList(){
        pairedDevices = myBluetooth.getBondedDevices();
        List<String> list = new ArrayList<>();
        if (pairedDevices.size() > 0){
            for (BluetoothDevice bt : pairedDevices){
                // list.add(bt.getName().toString() + "\n" + bt.getAddress().toString());
                list.add(bt.getAddress().toString() + " " + bt.getName().toString());
            }
        }else{
            Toast.makeText(getApplicationContext(), "There are no paired devices...", Toast.LENGTH_LONG).show();
        }

        adapter = new RobotAdapter(this, list);

        //MAC ADDRESS ->>> 17 digits
        deviceList.setAdapter(adapter);
        deviceList.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), deviceList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView name = view.findViewById(R.id.robot_address);
                String address = name.getText().toString();

                Intent i = new Intent(MainActivity.this, FirstPage.class);
                i.putExtra(EXTRA_ADDRESS, address);
                startActivity(i);
            }
        }));
    }

    private final AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(MainActivity.this, FirstPage.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
        }
    };

}