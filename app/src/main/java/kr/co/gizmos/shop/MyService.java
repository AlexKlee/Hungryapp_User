package kr.co.gizmos.shop;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service implements LocationListener {
    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latit, longit, shoplatit, shoplongit;
    LocationManager locationManager;
    Location userLoc, shopLoc;
    private Handler mHandler = new Handler();
    private Timer mTimer ;
    long notify_interval = 3000;
    public static String str_receiver = "MyService.service.receiver";//??
    Intent intent;
    String activity_name = "";
    ArrayList<Double> arrDist;

    boolean isRun = false;
    public MyService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRun=true;
        mTimer = new Timer();
        mTimer.schedule(new TimerTasktoGetLocation(), 0, notify_interval);
        intent = new Intent(str_receiver);
        SharedPreferences sepref = getSharedPreferences("appData", MODE_PRIVATE);
        longit = Double.parseDouble(sepref.getString("longt", "0"));
        latit = Double.parseDouble(sepref.getString("latt", "0"));
        activity_name = sepref.getString("activity_name", "");
        shoplongit = Double.parseDouble(sepref.getString("shop_map_x", "0"));
        shoplatit = Double.parseDouble(sepref.getString("shop_map_y", "0"));
        userLoc = new Location("userLoca");
        shopLoc = new Location("shopLoca");
        userLoc.setLongitude(longit);
        userLoc.setLatitude(latit);
        shopLoc.setLongitude(shoplongit);
        shopLoc.setLatitude(shoplatit);
        arrDist = new ArrayList<>();//거리값 저장 arraylist
    }

    @Override
    public void onLocationChanged(Location location) {
        if(isRun){
            fn_getLocation();
        }

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
    private void fn_getLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable) {

        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (isNetworkEnable) {
                userLoc = null;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
                if (locationManager != null) {
                    userLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (userLoc != null) {//수정하자.
                        latit = userLoc.getLatitude();
                        longit = userLoc.getLongitude();
                        fn_update(userLoc);
                    }
                }
            }

            if (isGPSEnable) {
                userLoc = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
                if (locationManager != null) {
                    userLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (userLoc != null) {
                        latit = userLoc.getLatitude();
                        longit = userLoc.getLongitude();
                        fn_update(userLoc);
                    }
                }
            }


        }
    }

 /*   @Override
    public boolean stopService(Intent name) {

        return super.stopService(name);

    }*/

    @Override
    public void onDestroy() {
        isRun=false;
        super.onDestroy();

    //    mTimer=new Timer();

       // mTimer.cancel();
 //      mTimer=null;

    }

    private class TimerTasktoGetLocation extends TimerTask {
        @Override
        public void run() {
            if(isRun) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fn_getLocation();
                    }

                });
            }else{
                onDestroy();
            }
        }
    }

    private void fn_update(Location location) {
        double dist = userLoc.distanceTo(shopLoc);
        SharedPreferences serpref = getSharedPreferences("appData",MODE_PRIVATE);
        SharedPreferences.Editor seEditor = serpref.edit();
        arrDist.add(dist);
        if (arrDist.size() > 1) {
            //  for(int i=0; i<arrDist.size();i++){
            int formerDist = arrDist.size() - 2;
            int afterDist = arrDist.size() - 1;
            if (formerDist < 0) {
                formerDist = 0;
            } else if (afterDist > arrDist.size()) {
                afterDist = arrDist.size();
            }
            String distance = String.valueOf(arrDist.get(afterDist));

            if (arrDist.get(formerDist) + 100 < arrDist.get(afterDist)) {//처음 dist값보다 후에 dist값이 100미터 이상 커질경우
                //거리가 멀어졌다는 알림 발생.
                Intent popupIntent = new Intent(getApplicationContext(), AlertDialogActivity.class);
                seEditor.putString("type", "예약취소");
                seEditor.commit();

               /* Intent stopit = new Intent(getApplicationContext(), MyService.class);
                stopService(stopit);*/
                PendingIntent pie = PendingIntent.getActivity(getApplicationContext(),0,popupIntent,PendingIntent.FLAG_ONE_SHOT);

                try{
                    pie.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

              /*  AlertDialog.Builder getAwayDlg = new AlertDialog.Builder(MyService.this);
                getAwayDlg.setTitle("멀어지고 있습니다.");
                getAwayDlg.setMessage("예약을 취소하시겠습니까?");
                getAwayDlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent backtoMain = new Intent(MyService.this, Main2Activity.class);
                        backtoMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//모든 액티비티 종료, 새로 액티비티발생
                        startActivity(backtoMain);
                        //editor.putString("Distance","취소");

                        //서버에 가게 예약취소 전달


                    }
                });
                getAwayDlg.setNegativeButton("취소", null);
                getAwayDlg.show();*/

                //예약취소?
                Toast.makeText(getApplicationContext(), "멀어져간다.", Toast.LENGTH_SHORT).show();

            } else if (arrDist.get(afterDist) - arrDist.get(formerDist) < 50 && arrDist.get(afterDist) > 50) {//가게와 거리가 50미터 이상이고, 나중거리-이전거리 50이하일경우, 가까워진다는 의미.
                //접근중.. 굳이? 뺄까?
                //              editor.putString("Distance", "접근 중");
                Toast.makeText(getApplicationContext(), "접근중", Toast.LENGTH_SHORT).show();

                //가게에 접근중 전달? 봐서 삭제.
            } else if (arrDist.get(afterDist) < 200 && arrDist.get(afterDist) > 190) {
                Toast.makeText(getApplicationContext(), "200m", Toast.LENGTH_SHORT).show();
                //현재위치 가게 전달(200미터전)
                //             meterTask meterTask2=new meterTask();
                //             meterTask2.execute(id,distance);//거리값 서버전달
                //             editor.putString("Distance", "200");
            } else if (arrDist.get(afterDist) < 100 && arrDist.get(afterDist) > 95) {
                //현재위치 가게 전달(100미터전)
                Toast.makeText(getApplicationContext(), "100m", Toast.LENGTH_SHORT).show();
                //             meterTask.execute(id,distance);
                //             editor.putString("Distance","100");
            } else if (arrDist.get(afterDist) < 50) {//마지막 거리가 50이하일경우
                Toast.makeText(getApplicationContext(), "곧 도착", Toast.LENGTH_SHORT).show();
                //            editor.putString("Distance","곧 도착");
                Intent popupIntent = new Intent(getApplicationContext(), AlertDialogActivity.class);
                seEditor.putString("type", "도착완료");
                seEditor.commit();

                PendingIntent pie = PendingIntent.getActivity(getApplicationContext(),0,popupIntent,PendingIntent.FLAG_ONE_SHOT);
                try{
                    pie.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

/*
                AlertDialog.Builder arriveDlg = new AlertDialog.Builder(MyService.this);
                arriveDlg.setTitle("도착");
                arriveDlg.setMessage("도착하셨습니까?");
                arriveDlg.setPositiveButton("도착완료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Bon Appetit~ \n 식사 맛있게 하세요~!", Toast.LENGTH_SHORT).show();
                        //리뷰화면으로 이동
                        //notification 내용 변경


                    }
                });
                arriveDlg.setNegativeButton("가는 중", null);
                arriveDlg.show();*/
                //메시지 출력식사 맛있게하세요? 리뷰도 작성해주세요??
                //리뷰정보 출력?
                //      meterTask meterTask3=new meterTask();
                //         meterTask3.execute(id,distance);
            }

/*
            if (dist < 200 && dist > 190) {
                Toast.makeText(getApplicationContext(), "200m", Toast.LENGTH_SHORT).show();
            } else if (dist < 100 && dist > 90) {
                Toast.makeText(getApplicationContext(), "100m", Toast.LENGTH_SHORT).show();
            } else if (dist < 50 && dist > 0) {
                Toast.makeText(getApplicationContext(), "50m", Toast.LENGTH_SHORT).show();
                //도착 안내문구화면으로 이동
            } else {
                Toast.makeText(getApplicationContext(), dist + "m", Toast.LENGTH_SHORT).show();
            }*/


            //        SharedPreferences shpref = getSharedPreferences("appData",MODE_PRIVATE);
            //      SharedPreferences.Editor editor= shpref.edit();

        }
        //서버 통신 추가?
    }
}
