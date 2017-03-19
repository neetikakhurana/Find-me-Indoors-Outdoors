package com.lecture.nitika.acclocation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private Object sync = new Object();
    Dialog alertDialog;
    SensorManager sensorManager;
    Sensor accelerometer, rotation, lightsensor;
    float[] gravityValues = new float[3];
    float[] velocityValues = new float[3];
    private float timestamp;
    float[] magneticValues = null;
    EditText latitude,longitude,latacc,longacc,dist;
    double gx = 0.0, gy = 0.0, gz = 0.0;
    Handler sensorHandler = new Handler();
    static Double earthRadius = 6378D;
    static Double oldLat, oldLong;
    static Boolean IsFirst = true,night=true;
    private static String TAG="Assignment1";
    static Double sensorLatitude, sensorLongitude;
    int level,gpscount=0,nwcount=0;
    static long GPSTime;
    public static Float currAcceleration = 0.0F;
    public static Float currentDirection = 0.0F;
    public static Float CurrentSpeed = 0.0F;
    public static Float distanceTravelled = 0.0F;
    float[] prevValues;
    List<Float> accValuesx=new ArrayList<Float>();
    List<Float> accValuesy=new ArrayList<Float>();
    List<Float> accValuesz=new ArrayList<Float>();

    float prevTime, currentTime, changeTime, distanceX, distanceY, distanceZ;
    WifiManager wifi;
    Float lightLevel=0f;
    double distanceInMeters;

    LocationManager locationManager;
    public static Float prevAcceleration = 0.0F;
    public static Float prevSpeed = 0.0F;
    public static Float prevDistance = 0.0F;

    Handler locationHandler=new Handler();
    Boolean First, initSensor = true;
    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;
    int mSignalStrength = 0;
    Double latMethod1, longMethod1;
    boolean strengthFlag=false, lightFlag=false,isGPSEnabled=false,isNetworkEnabled=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        First = initSensor = true;
        prevValues = new float[3];
        latitude=(EditText)findViewById(R.id.editText);
        longitude=(EditText)findViewById(R.id.editText2);
        latacc=(EditText)findViewById(R.id.editText3);
        longacc=(EditText)findViewById(R.id.editText4);
        dist=(EditText)findViewById(R.id.editText5);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        wifi = (WifiManager)getSystemService(WIFI_SERVICE);
        lightsensor=sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this,lightsensor,SensorManager.SENSOR_DELAY_FASTEST);
        mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        isGPSEnabled =locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    @Override
    protected void onResume() {
        super.onResume();

        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        Log.i(TAG,"LEVEL"+level);
        if(!wifi.isWifiEnabled()){
            Log.i(TAG,"onCreate : WIFI DISABLED");
        }
        else{

            if (level<-90){
                //outdoors
                synchronized (sync) {
                        gpscount += 1;
                    if (nwcount > 0) {
                        nwcount--;
                    }
                }

            }
            else{
                synchronized (sync) {
                        nwcount++;
                    if (gpscount > 0) {
                        gpscount--;
                    }
                }
            }
        }
 }
    class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            strengthFlag=true;
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength.getGsmSignalStrength();
            mSignalStrength = (2 * mSignalStrength) - 113; // -> dBm

            if (mSignalStrength>-90){
                synchronized (sync) {
                        gpscount += 1;
                    if (nwcount > 0) {
                        nwcount--;
                    }
                }

            }
            else{
                synchronized (sync) {
                    if (nwcount <= 5) {
                        nwcount += 1;
                    } else if (gpscount > 0) {
                        gpscount--;
                    }
                }

            }
        }
    }

    public void calculateAccuracy(double latitude1, double long1, double lat2, double lng2) {
        distanceInMeters=0;
        double dLat = Math.toRadians(lat2 - latitude1);
        double dLon = Math.toRadians(lng2 - long1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        distanceInMeters = Math.round(earthRadius * c*1000);
        dist.setText(new Double(distanceInMeters).toString()+" m");
    }

    public class Method1 implements Runnable{

        @Override
        public void run() {
            latitude.setText(latMethod1.toString());
            longitude.setText(longMethod1.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //TAKING VALUES FROM GPS/NETWORK FOR THE FIRST TIME
        if(First) {
            oldLat = location.getLatitude();
            oldLong = location.getLongitude();
            GPSTime = location.getTime();
            sensorManager.registerListener(this,rotation,SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        }
        latMethod1=location.getLatitude();
        longMethod1=location.getLongitude();
        locationHandler.post(new Method1());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        alertDialog.dismiss();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getApplicationContext(), "Location services off. Turn on Location", Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Services Not Active");
        builder.setMessage("Please enable Location Services and GPS");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                // Show location settings when the user acknowledges the alert dialog
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    public class Method2 implements Runnable{

        @Override
        public void run() {
            gx=(float)0.98*gx+(float)0.02*gravityValues[0];
            gy=(float)0.98*gy+(float)0.02*gravityValues[1];
            gz=(float)0.98*gz+(float)0.02*gravityValues[2];
            gravityValues[0]-=gx;
            gravityValues[1]-=gy;
            gravityValues[2]-=gz;

            //this is the time when the data is taken from the gps/network provider
            if(First){
                prevValues = gravityValues;
                accValuesx.add(gravityValues[0]);
                accValuesy.add(gravityValues[1]);
                accValuesz.add(gravityValues[2]);
                prevTime = timestamp / 1000000000;
                First = false;
                distanceX = distanceY= distanceZ = 0;
            }
            else{

                currentTime = timestamp / 1000000000.0f;
                changeTime = currentTime - prevTime;
                prevTime = currentTime;
                accValuesx.add(gravityValues[0]);
                accValuesy.add(gravityValues[1]);
                accValuesz.add(gravityValues[2]);

                if(accValuesx.size()>49){
                    float avgx=0,avgy=0,avgz=0;
                    for (int i=0;i<50;i++){
                        avgx+=accValuesx.get(i);
                        avgy+=accValuesy.get(i);
                        avgz+=accValuesz.get(i);
                    }
                    accValuesx.clear();
                    accValuesy.clear();
                    accValuesz.clear();
                    gravityValues[0]=avgx/50;
                    gravityValues[1]=avgy/50;
                    gravityValues[2]=avgz/50;

                    calculateDistance(gravityValues, changeTime);
                    currAcceleration =  (float) Math.sqrt(gravityValues[0] * gravityValues[0] + gravityValues[1] * gravityValues[1] + gravityValues[2] * gravityValues[2]);
                    CurrentSpeed = (float) Math.sqrt(velocityValues[0] * velocityValues[0] + velocityValues[1] * velocityValues[1] + velocityValues[2] * velocityValues[2]);
                    distanceTravelled = (float) Math.sqrt(distanceX *  distanceX + distanceY * distanceY +  distanceZ * distanceZ);
                    distanceTravelled = distanceTravelled / 1000;
                }


                if(initSensor){
                    prevAcceleration = currAcceleration;
                    prevDistance = distanceTravelled;
                    prevSpeed = CurrentSpeed;
                    initSensor = false;
                }
                prevValues = gravityValues;

            }
            if(currAcceleration != prevAcceleration || CurrentSpeed != prevSpeed || prevDistance != distanceTravelled){

                if (gravityValues != null && magneticValues != null && currAcceleration != null) {
                    float RT[] = new float[9];
                    float I[] = new float[9];
                    boolean success = SensorManager.getRotationMatrix(RT, I, gravityValues,
                            magneticValues);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(RT, orientation);
                        float azimut = (float) Math.round(Math.toDegrees(orientation[0]));
                        currentDirection =(azimut+ 360) % 360;
                            calculatePosition(distanceTravelled,currentDirection);
                    }
                    prevAcceleration = currAcceleration;
                    prevSpeed = CurrentSpeed;
                    prevDistance = distanceTravelled;
                }
            }
        }
        public void calculateDistance(float[] values,float dT){
            float[] distance = new float[values.length];
            for (int i = 0; i < values.length; i++) {
                velocityValues[i] = values[i] * dT;
                distance[i] = velocityValues[i] * dT + values[i] * dT * dT / 2;
            }
            distanceX = distance[0];
            distanceY = distance[1];
            distanceZ = distance[2];
        }

        public void calculatePosition(Float distanceTravelled, Float currentDirection){
            Log.i(TAG,"calculatePosition");
            
            //when gps/network provider is being used
            if(IsFirst){
                sensorLatitude = oldLat;
                sensorLongitude = oldLong;
                IsFirst  = false;
                return;
            }

            Date CurrentTime = new Date();

            if(CurrentTime.getTime() - GPSTime > 0) {
                //Convert Variables to Radian for the Formula
                oldLat = Math.PI * oldLat / 180;
                oldLong = Math.PI * oldLong / 180;
                currentDirection = (float) (Math.PI * currentDirection / 180.0);

                //Formulae to Calculate the NewLAtitude and NewLongtiude
                Double newLatitude = Math.asin(Math.sin(oldLat) * Math.cos(distanceTravelled / earthRadius) +
                        Math.cos(oldLat) * Math.sin(distanceTravelled / earthRadius) * Math.cos(currentDirection));
                Double newLongitude = oldLong + Math.atan2(Math.sin(currentDirection) * Math.sin(distanceTravelled / earthRadius)
                        * Math.cos(oldLat), Math.cos(distanceTravelled / earthRadius)
                        - Math.sin(oldLat) * Math.sin(newLatitude));
                //Convert Back from radians
                newLatitude = 180 * newLatitude / Math.PI;
                newLongitude = 180 * newLongitude / Math.PI;
                currentDirection = (float) (180 * currentDirection / Math.PI);
                latacc.setText(newLatitude.toString());
                longacc.setText(newLongitude.toString());
                
                //Update old Latitude and Longitude
                oldLat = newLatitude;
                oldLong = newLongitude;

                sensorLatitude = oldLat;
                sensorLongitude = oldLong;
                calculateAccuracy(latMethod1, longMethod1, sensorLatitude, sensorLongitude);

            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                gravityValues=event.values;
                timestamp=event.timestamp;
                sensorHandler.post(new Method2());
            }
            if(event.sensor.getType()==Sensor.TYPE_LIGHT){
                lightFlag=true;
                lightLevel=event.values[0];
                //activate light sensor
                if(lightLevel>3000){
                    //this means it is daylight and we are outdoor so gps should be enabled
                    gpscount+=1;
                    if (nwcount > 0) {
                        nwcount--;
                    }
                }
                else if (lightLevel<5 && night){
                    //outdoor
                    synchronized (sync) {
                            gpscount += 1;
                        if (nwcount > 0) {
                            nwcount--;
                        }
                    }

                }
                else{
                    synchronized (sync) {
                            nwcount += 1;
                        if (gpscount > 0) {
                            gpscount--;
                        }
                    }

                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                    if (gpscount > nwcount) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        //Toast.makeText(getApplicationContext(), "GPS Selected", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "gps selected");
                        gpscount = 0;
                        nwcount = 0;
                    } else {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                       // Toast.makeText(getApplicationContext(), "Network Selected", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "nw selected");
                        gpscount = 0;
                        nwcount = 0;
                    }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = event.values;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
    }

}
