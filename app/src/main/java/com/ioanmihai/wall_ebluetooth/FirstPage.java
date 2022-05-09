package com.ioanmihai.wall_ebluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.LinkAddress;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FirstPage extends AppCompatActivity {

    private ImageView backButton, settingsButton;
    private CardView remoteControl, sendLocation, securityControl, sendCoordinates, rename;
    private TextView robotName, helpButton;
    private String address, newName;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter myBluetooth = null;
    public static String EXTRA_ADDRESS = "device_address";
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private ProgressDialog progressDialog;
    private Switch mySwitch;
    public static String NEW_NAME = "device_name";
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle  savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);

        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        newName = intent.getStringExtra(TestActivity.NEW_NAME);
        Toast.makeText(getApplicationContext(), address, Toast.LENGTH_LONG).show();

        backButton = findViewById(R.id.back_button);

        new FirstPage.ConnectBT().execute();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(FirstPage.this, MainActivity.class);
                Disconnect();
                startActivity(mainIntent);
            }
        });

        mySwitch = findViewById(R.id.tracking_switch);
        remoteControl = findViewById(R.id.remote_control_card);
        rename = findViewById(R.id.rename);
        robotName = findViewById(R.id.robot_name_title);
        settingsButton = findViewById(R.id.settings_button);
        helpButton = findViewById(R.id.help_button);

        if (newName != null){
            robotName.setText(newName);
        }else{
            robotName.setText("Wall-E");
        }


        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sendSignal("traking_on");
                    remoteControl.setClickable(false);
                    rename.setClickable(false);
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                    /*LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    LocationListener locationListener = new MyLocationListener();
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 3, locationListener);
                    }*/


                    /*Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                getLocation();
                            }
                            handler.postDelayed(this, 1000);
                        }
                    };
                    handler.postDelayed(runnable, 1000);*/
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // getLocation();
                    }

                }else{
                    sendSignal("traking_off");
                    remoteControl.setClickable(true);
                    rename.setClickable(true);
                }
            }
        });


        if (remoteControl.isClickable()){
            remoteControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent remoteIntent = new Intent(FirstPage.this, RemoteControl.class);
                    remoteIntent.putExtra(EXTRA_ADDRESS, address);
                    Disconnect();
                    startActivity(remoteIntent) ;
                }
            });
        }

        if (rename.isClickable()){
            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent renameIntent = new Intent(getApplicationContext(), TestActivity.class);
                    renameIntent.putExtra(EXTRA_ADDRESS, address);
                    renameIntent.putExtra(NEW_NAME, robotName.getText().toString());
                    Disconnect();
                    startActivity(renameIntent);
                }
            });
        }

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(FirstPage.this);
                alert.setTitle("Disconnect?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                        Disconnect();
                        dialogInterface.dismiss();
                        startActivity(mainIntent);
                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alert.show();
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(FirstPage.this);
                alertDialog.setTitle("Need help?");
                final EditText input = new EditText(FirstPage.this);
                input.setHint("Enter message here...");
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(FirstPage.this, "Message sent. We will answer as soon as possible.", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alertDialog.show();
            }
        });

    }

/*    @SuppressLint("MissingPermission")
    private void getLocation()
    {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null){
                    Geocoder geocoder = new Geocoder(FirstPage.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        String latitude = Double.toString(addresses.get(0).getLatitude());
                        String longitude = Double.toString(addresses.get(0).getLongitude());
                        String coord = longitude + " " + latitude;
                        sendSignal(coord);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }*/

    private void sendSignal(String signal){
        if (btSocket != null){
            try{
                btSocket.getOutputStream().write(signal.getBytes());
            } catch (IOException e){
                msg("Error");
            }
        }
    }

    private void Disconnect(){
        if (btSocket != null){
            try{
                btSocket.close();
            }catch(IOException e){
                msg("Error");
            }
        }
        finish();
    }

    private void msg(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute(){
            progressDialog = ProgressDialog.show(FirstPage.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try{
                if (btSocket == null || !isBtConnected){
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispozitiv = myBluetooth.getRemoteDevice(address);
                    btSocket = dispozitiv.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e){
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess){
                msg("Connection failed =((");
                finish();
            } else{
                msg("Connected");
                isBtConnected = true;
            }
            progressDialog.dismiss();

        }
    }

    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(@NonNull Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            String Longitude = Double.toString(longitude);
            String Latitude = Double.toString(latitude);

            String coord = Longitude + " " + Latitude;
            sendSignal(coord);
        }

        @Override
        public void onFlushComplete(int requestCode) {

        }
    }

/*    public class GPSTracker extends Service implements LocationListener{
        private final Context mContext;

        boolean isGPSEnabled = false;
        boolean isNetworkEnabled = false;
        boolean canGetLocation = false;

        Location location;
        double latitude;
        double longitude;

        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
        private static final long MIN_TIME_BW_UPDATES = 1000 * 60;

        protected LocationManager locationManager;

        public GPSTracker(Context context){
            this.mContext = context;
            getLocation();
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {

        }
    }*/

}