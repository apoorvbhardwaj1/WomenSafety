package com.ab.womensafety;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private GoogleMap mMap;
    TextView latitudeText;
    TextView longitudeText;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Double myLatitude;
    private Double myLongitude;
    private boolean permissionIsGranted = false;
    float[] distance = new float[2];
    private MediaPlayer mediaPlayer;
    private Place place;
    private LatLng point;
    private CircleOptions circleOptions2;
    private EditText e1;
    String phoneNo;
    List<Address> addresses;
    String currentLocation;
    Vibrator vibrat;
    //boolean enter=true;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String phone = "phoneKey";
    SharedPreferences sharedpreferences;
    private static final int MY_PERMISSION_REQUEST_MULTIPLE =101 ;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE =102 ;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 103 ;
    private static final int MY_PERMISSION_REQUEST_LOCATION =104 ;

    private void drawCircle(LatLng point){

        // Instantiating CircleOptions to draw a circle around the marker
        circleOptions2 = new CircleOptions();

        // Specifying the center of the circle
        circleOptions2.center(point);

        // Radius of the circle
        circleOptions2.radius(1000.0);

        // Border color of the circle
        circleOptions2.strokeColor(0xffff0000);
        // Fill color of the circle
        circleOptions2.fillColor(0x44ff0000);

        // Border width of the circle
        circleOptions2.strokeWidth(2);

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions2);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);

        vibrat = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        latitudeText = (TextView) findViewById(R.id.tvLatitude);
        longitudeText = (TextView) findViewById(R.id.tvLongitude);


        mediaPlayer = MediaPlayer.create(this, RingtoneManager.getValidRingtoneUri(this));
        mediaPlayer.setLooping(true);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        final PlaceAutocompleteFragment places = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                MapsActivity.this.place = place;
                Toast.makeText(getApplicationContext(), place.getName(), Toast.LENGTH_SHORT).show();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12));
                mMap.clear();               //Clears the map before adding another Marker
                MarkerOptions marker = new MarkerOptions().position(
                        new LatLng(place.getLatLng().latitude, place.getLatLng().longitude)).title("Destination");
                mMap.addMarker(marker);

                drawCircle(marker.getPosition());

            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


        Button b2 = (Button) findViewById(R.id.button2);
        Button b3 = (Button) findViewById(R.id.button3);
        Button b4 = (Button) findViewById(R.id.button4);
        Button b5 = (Button) findViewById(R.id.button5);
        e1= (EditText) findViewById(R.id.editText);

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:100"));

                if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[] {Manifest.permission.CALL_PHONE},MY_PERMISSIONS_REQUEST_CALL_PHONE);
                    } else
                    {
                        permissionIsGranted = true;
                    }
                    return;
                }
                startActivity(callIntent);
                Toast.makeText(getApplicationContext(),"Calling 100",Toast.LENGTH_SHORT).show();

            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNo = e1.getText().toString();
                if(phoneNo.length()<3)
                {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    String phonenum=sharedpreferences.getString(phone,null);
                    if(!TextUtils.isEmpty(phonenum))
                    {
                        callIntent.setData(Uri.parse("tel:"+phonenum));

                        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[] {Manifest.permission.CALL_PHONE},MY_PERMISSIONS_REQUEST_CALL_PHONE);
                                } else
                                {
                                    permissionIsGranted = true;
                                }
                                return;
                        }
                        startActivity(callIntent);
                        Toast.makeText(getApplicationContext(),"Calling "+phonenum,Toast.LENGTH_SHORT).show();
                    }
                   else
                    {
                        Toast.makeText(getApplicationContext(),"Please Enter the Number",Toast.LENGTH_LONG).show();
                    }
                }

                else
                {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+phoneNo));

                    if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[] {Manifest.permission.CALL_PHONE},MY_PERMISSIONS_REQUEST_CALL_PHONE);
                        } else
                        {
                            permissionIsGranted = true;
                        }
                        return;
                    }
                    startActivity(callIntent);
                    Toast.makeText(getApplicationContext(),"Calling "+phoneNo,Toast.LENGTH_SHORT).show();
                }
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                    if(isLocationServiceEnabled())
                    {
                            getAddress();
                            sendSMSMessage();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Please Enable Location(GPS) first to use this service ",Toast.LENGTH_LONG).show();
                    }

            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phoneNo = e1.getText().toString();

                if(phoneNo.length()>=3)
                {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(phone,phoneNo);
                    editor.commit();
                    Toast.makeText(getApplicationContext(),"Number Saved : "+ sharedpreferences.getString(phone,null),Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please enter valid number",Toast.LENGTH_LONG).show();
                }
            }
        });
        
        


        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(final int count) {

                /*final long time = System.currentTimeMillis();
                long Timestamp;
                final long TIME_INTERVAL=10000;
                Timestamp = time;

                if (Timestamp + TIME_INTERVAL > time) {*/
                    
                    if(count==2)
                    {

                       // Toast.makeText(getApplicationContext(),"count="+count,Toast.LENGTH_SHORT).show();

                        vibrat.vibrate(800);

                        Toast.makeText(getApplicationContext(),"You have 10 seconds to cancel sending the danger alert message",Toast.LENGTH_LONG).show();
                        dialog.setTitle("Sending SMS ").setMessage("Do you want to cancel sending SMS ? ");
                        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //exitLauncher();
                                dialog.cancel();
                            }
                        });

                        final AlertDialog alert = dialog.create();


                      /*  if(!enter)
                        {
                            alert.dismiss();
                            enter=true;

                        }

                        else
                        {
                            enter=false;
                        }*/

                        alert.show();

// Hide after some seconds

                        final Handler handler  = new Handler();
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {


                                if (alert.isShowing()) {
                                    alert.dismiss();
                                    if(isLocationServiceEnabled())
                                    {
                                        getAddress();
                                        sendSMSMessage();
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplicationContext(),"Please Enable Location(GPS) first to use this service ",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        };

                        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                handler.removeCallbacks(runnable);
                            }
                        });

                        handler.postDelayed(runnable, 10000);
                        
                    }


                }

              /* else if(count == 2 && !enter)
                {
                    alert.dismiss();
                    enter=true;
                  
                }*/
            //}
        });
    }

    protected void sendSMSMessage() {

        phoneNo = e1.getText().toString();

        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.SEND_SMS},MY_PERMISSIONS_REQUEST_SEND_SMS);
            } else
            {
                permissionIsGranted = true;
            }
            return;
        }

        if(phoneNo.length()<3)
        {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                StringBuilder smsBody = new StringBuilder();
                smsBody.append("I am in Danger. HELP!!  \n");
                smsBody.append(currentLocation);
                smsBody.append("\nhttp://maps.google.com/maps?q=loc:");
                smsBody.append(myLatitude);
                smsBody.append(",");
                smsBody.append(myLongitude);
                ArrayList<String> parts = smsManager.divideMessage(smsBody.toString());
                String phonenum=sharedpreferences.getString(phone,null);

                if(!TextUtils.isEmpty(phonenum))
                {
                    smsManager.sendMultipartTextMessage(phonenum, null,parts, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent to "+phonenum, Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please Enter Number to send SMS ",Toast.LENGTH_LONG).show();
                }
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                e.printStackTrace();}

        }

        else
        {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                StringBuilder smsBody = new StringBuilder();
                smsBody.append("I am in Danger. HELP!!  \n");
                smsBody.append(currentLocation);
                smsBody.append("\nhttp://maps.google.com/maps?q=loc:");
                smsBody.append(myLatitude);
                smsBody.append(",");
                smsBody.append(myLongitude);
                ArrayList<String> parts = smsManager.divideMessage(smsBody.toString());

                smsManager.sendMultipartTextMessage(phoneNo, null,parts, null, null);
                Toast.makeText(getApplicationContext(), "SMS sent to "+phoneNo, Toast.LENGTH_LONG).show();
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                e.printStackTrace();}

        }
    }

    //Geocoder

    public Address getAddress(double latitude, double longitude)
    {
        Geocoder geocoder;

        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);// Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void getAddress()
    {

        Address locationAddress=getAddress(myLatitude,myLongitude);

        if(locationAddress!=null)

        {

            String address = locationAddress.getAddressLine(0);

            String address1 = locationAddress.getAddressLine(1);

          //  String city = locationAddress.getLocality();

            String state = locationAddress.getAdminArea();

            String country = locationAddress.getCountryName();

           // String postalCode = locationAddress.getPostalCode();


            if(!TextUtils.isEmpty(address))

            {

                currentLocation=address;


               if (!TextUtils.isEmpty(address1))

                   currentLocation+=", "+address1;


               /* if (!TextUtils.isEmpty(city))

                {
                     currentLocation+=", "+city;

                     if (!TextUtils.isEmpty(postalCode))

                        currentLocation+=" - "+postalCode;

                }

                else

                {

                    if (!TextUtils.isEmpty(postalCode))

                        currentLocation+="  "+postalCode;

                }*/

                if (!TextUtils.isEmpty(state))

                    currentLocation+=", "+state;


                if (!TextUtils.isEmpty(country))

                   currentLocation+=", "+country;
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.SEND_SMS,Manifest.permission.CALL_PHONE},MY_PERMISSION_REQUEST_MULTIPLE);
            } else
            {
                permissionIsGranted = true;
            }
            return;
        }

        else
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_LOCATION);
                } else
                {
                    permissionIsGranted = true;
                }
                return;
            }
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);

        //Location service check
        isLocationServiceEnabled();

    }

    public boolean isLocationServiceEnabled()
    {
        LocationManager lm;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {
            Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
        }

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("To continue, let your device turn on location, which uses Google's location service " );
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                }
            });
            dialog.show();
        }
        return gps_enabled||network_enabled;
    }


    @Override
    public void onMapClick (LatLng point) {

        this.point = point;

        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(point.latitude, point.longitude)).title("Destination");
        mMap.clear();
        drawCircle(marker.getPosition());
        mMap.addMarker(marker);
    }


    @Override
    public void onLocationChanged(Location location) {
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();
        latitudeText.setText("Latitude : " + String.valueOf(myLatitude));
        longitudeText.setText("Longitude : " + String.valueOf(myLongitude));

        LatLng loc = new LatLng(myLatitude,myLongitude);
        //mMap.addMarker(new MarkerOptions().position(loc).title("current position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc,16));

        if (place != null){
            Location.distanceBetween(myLatitude, myLongitude, place.getLatLng().latitude, place.getLatLng().longitude, distance);

            if (distance[0] >= circleOptions2.getRadius()) {
                // Toast.makeText(getBaseContext(), " PLACE You are not in the circle", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getBaseContext(), "You are going to reach your destination.", Toast.LENGTH_LONG).show();


                if (mediaPlayer.isPlaying()) {
                    return;
                } else {
                    mediaPlayer.start();
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setCancelable(false).setMessage("You Are Going To Reach Your Destination").setPositiveButton("STOP ALARM                           ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mediaPlayer.stop();
                            mediaPlayer.prepareAsync();
                            mediaPlayer.seekTo(0);
                            dialog.cancel();
                            mMap.clear();
                            place = null;
                        }
                    });

                    AlertDialog dialog = alert.create();
                    dialog.show();
                }
            }
        }

        if(point != null) {
            //place = null;
            Location.distanceBetween(myLatitude, myLongitude,
                    point.latitude, point.longitude, distance);

            if (distance[0] >= circleOptions2.getRadius()) {
                // Toast.makeText(getBaseContext(), "POINTYou are not in the circle", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getBaseContext(), "You are going to reach your destination.", Toast.LENGTH_LONG).show();


                if (mediaPlayer.isPlaying()) {
                    return;
                } else {
                    mediaPlayer.start();
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setCancelable(false).setMessage("You Are Going To Reach Your Destination").setPositiveButton("STOP ALARM                           ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mediaPlayer.stop();
                            mediaPlayer.prepareAsync();
                            mediaPlayer.seekTo(0);
                            dialog.cancel();
                            mMap.clear();
                            point = null;
                        }
                    });

                    AlertDialog dialog = alert.create();
                    dialog.show();
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();

    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        googleApiClient.connect();

    }

    protected void onResume()
    {
        super.onResume();
        if(permissionIsGranted) {
            if (googleApiClient.isConnected()) {
                requestLocationUpdates();
            }
        }

        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {

        super.onPause();

        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);

        if (permissionIsGranted) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (permissionIsGranted) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_PERMISSION_REQUEST_MULTIPLE:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permissions granted
                    permissionIsGranted = true;
                }  else {
                    //permission denied
                    permissionIsGranted = false;
                    Toast.makeText(getApplicationContext(),"This app requires location, calling & SMS permission to work properly", Toast.LENGTH_SHORT).show();
                    latitudeText.setText("Latitude : Permission is not granted");
                    longitudeText.setText("Longitude : Permission is not granted");

                }
                break;
            }

            case MY_PERMISSIONS_REQUEST_CALL_PHONE:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permissions granted
                    permissionIsGranted = true;
                }  else {
                    //permission denied
                    permissionIsGranted = false;
                    Toast.makeText(getApplicationContext(),"This app requires calling permission to work properly", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case MY_PERMISSIONS_REQUEST_SEND_SMS:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permissions granted
                    permissionIsGranted = true;
                }  else {
                    //permission denied
                    permissionIsGranted = false;
                    Toast.makeText(getApplicationContext(),"This app requires SMS permission to work properly", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case MY_PERMISSION_REQUEST_LOCATION:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permissions granted
                    permissionIsGranted = true;
                }  else {
                    //permission denied
                    permissionIsGranted = false;
                    Toast.makeText(getApplicationContext(),"This app requires Location permission to work properly", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}




