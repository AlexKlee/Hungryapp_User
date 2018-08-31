package kr.co.gizmos.shop;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service implements LocationListener {
    boolean isGPSEnable=false;
    boolean isNetworkEnable=false;
    double latit, longit, shoplatit, shoplongit;
    LocationManager locationManager;
    Location userLoc, shopLoc;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 3000;
    public static String str_receiver= "MyService.service.receiver";//??
    Intent intent;
    String activity_name="";
    public MyService(){

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer=new Timer();
        mTimer.schedule(new TimerTasktoGetLocation(), 0, notify_interval);
        intent=new Intent(str_receiver);
        SharedPreferences sepref= getSharedPreferences("appData",MODE_PRIVATE);
        longit=Double.parseDouble(sepref.getString("longt","0"));
        latit=Double.parseDouble(sepref.getString("latt","0"));
        activity_name=sepref.getString("activity_name","");
        shoplongit=Double.parseDouble(sepref.getString("shop_map_x","0"));
        shoplatit=Double.parseDouble(sepref.getString("shop_map_y","0"));
        userLoc=new Location("userLoca");
        shopLoc=new Location("shopLoca");
        userLoc.setLongitude(longit);
        userLoc.setLatitude(latit);
        shopLoc.setLongitude(shoplongit);
        shopLoc.setLatitude(shoplatit);

    }

    @Override
    public void onLocationChanged(Location location) {
        fn_getLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @SuppressLint("MissingPermission")
    private void fn_getLocation(){
        locationManager=(LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!isGPSEnable && !isNetworkEnable){

        }
        else {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            if(isNetworkEnable){
                userLoc=null;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,10,this);
                if(locationManager!=null){
                    userLoc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(userLoc!=null){
                        latit=userLoc.getLatitude();
                        longit=userLoc.getLongitude();
                        fn_update(userLoc);
                    }
                }
            }

            if(isGPSEnable){
                userLoc=null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,10,this);
                if(locationManager!=null){
                    userLoc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(userLoc!=null){
                        latit=userLoc.getLatitude();
                        longit=userLoc.getLongitude();
                        fn_update(userLoc);
                    }
                }
            }



        }
    }

    private class TimerTasktoGetLocation extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getLocation();
                }

            });
        }
    }
    private void fn_update(Location location){
        int dist = (int)userLoc.distanceTo(shopLoc);
        if(dist<200 && dist>190) {
            Toast.makeText(getApplicationContext(), "200m", Toast.LENGTH_SHORT).show();
        }
        else if(dist<100 && dist>90) {
            Toast.makeText(getApplicationContext(), "100m", Toast.LENGTH_SHORT).show();
        }
        else if(dist<50 && dist>0) {
            Toast.makeText(getApplicationContext(), "50m", Toast.LENGTH_SHORT).show();
            //도착 안내문구화면으로 이동
        }
        else{
            Toast.makeText(getApplicationContext(), dist+"m",Toast.LENGTH_SHORT).show();
        }


        //        SharedPreferences shpref = getSharedPreferences("appData",MODE_PRIVATE);
        //      SharedPreferences.Editor editor= shpref.edit();

    }
    //서버 통신 추가?
}
