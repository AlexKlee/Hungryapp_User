package kr.co.gizmos.shop;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service implements LocationListener {
    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latit, longit, shoplatit, shoplongit;
    LocationManager locationManager;
    Location userLoc, shopLoc;
    private Handler mHandler = new Handler();
    private Timer mTimer;
    long notify_interval =8000;
    public static String str_receiver = "MyService.service.receiver";//??
    Intent intent;
    String activity_name = "";
    ArrayList<Double> arrDist;
    String id;
    String menuid;
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
        isRun = true;
        mTimer = new Timer();
        mTimer.schedule(new TimerTasktoGetLocation(), 1000, notify_interval);//처음 딜레이

        intent = new Intent(str_receiver);
        SharedPreferences sepref = getSharedPreferences("appData", MODE_PRIVATE);
        id = sepref.getString("user_id", "");
        menuid = sepref.getString("menu_id", "");
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
        if (isRun) {
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

    @Override
    public void onDestroy() {
        isRun = false;
        super.onDestroy();

        //    mTimer=new Timer();

        // mTimer.cancel();
        //      mTimer=null;

    }

    private class TimerTasktoGetLocation extends TimerTask {
        @Override
        public void run() {
            if (isRun) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fn_getLocation();
                    }

                });
            } else {
                onDestroy();
            }
        }
    }

    private void fn_update(Location location) {

        double dist = userLoc.distanceTo(shopLoc);
        SharedPreferences serpref = getSharedPreferences("appData", MODE_PRIVATE);
        SharedPreferences.Editor seEditor = serpref.edit();
        arrDist.add(dist);

        String disMsg = null;//서버에 전달할 메시지.

        if (arrDist.size() > 1) {
            //  for(int i=0; i<arrDist.size();i++){
            int formerDist = arrDist.size() - 2;
            int afterDist = arrDist.size() - 1;
            if (formerDist < 0) {
                formerDist = 0;
            } else if (afterDist > arrDist.size()) {
                afterDist = arrDist.size();
            }
            //String distance = String.valueOf(arrDist.get(afterDist));

            if (arrDist.get(formerDist) + 100 < arrDist.get(afterDist)) {//처음 dist값보다 후에 dist값이 100미터 이상 커질경우
                //거리가 멀어졌다는 알림 발생.,dlg에서 예약취소 문의
                Intent popupIntent = new Intent(getApplicationContext(), AlertDialogActivity.class);
                seEditor.putString("type", "예약취소");
                seEditor.commit();
                meterTask meterTask2 = new meterTask();
                meterTask2.execute(id, menuid, "away");//멀어지는 중 이후 메시지 전송

                PendingIntent pie = PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);

                try {
                    pie.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "멀어지는 중", Toast.LENGTH_SHORT).show();
                MapReservation.txRes.setText("멀어지는 중..");

            } else if (arrDist.get(formerDist) + 50 < arrDist.get(afterDist)) {//처음 dist값보다 후에 dist값이 100미터 이상 커질경우
                //거리가 멀어졌다는 알림 발생.,dlg에서 예약취소 문의
                Intent popupIntent = new Intent(getApplicationContext(), AlertDialogActivity.class);
                seEditor.putString("type", "예약취소");
                seEditor.commit();
                meterTask meterTask2 = new meterTask();
                meterTask2.execute(id, menuid, "away");//멀어지는 중 이후 메시지 전송

                PendingIntent pie = PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);

                try {
                    pie.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "멀어지는 중", Toast.LENGTH_SHORT).show();
                MapReservation.txRes.setText("멀어지는 중..");
            }else
                if (arrDist.get(afterDist) - arrDist.get(formerDist) < 50 && arrDist.get(afterDist) > 50) {//가게와 거리가 50미터 이상이고, 나중거리-이전거리 50이하일경우, 가까워진다는 의미.
                    //접근중.. 굳이? 뺄까?
                    //              editor.putString("Distance", "접근 중");
                    Toast.makeText(getApplicationContext(), "접근중", Toast.LENGTH_SHORT).show();
                    MapReservation.txRes.setText("접근 중..");
                    meterTask meterTask2 = new meterTask();
                    meterTask2.execute(id, menuid, "approach");
                    //가게에 접근중 전달? 봐서 삭제.
                } else if (arrDist.get(afterDist) < 50) {//마지막 거리가 50이하일경우
                    Toast.makeText(getApplicationContext(), "곧 도착", Toast.LENGTH_SHORT).show();
                    //            editor.putString("Distance","곧 도착");
                    Intent popupIntent = new Intent(getApplicationContext(), AlertDialogActivity.class);
                    seEditor.putString("type", "곧 도착");
                    seEditor.commit();

                    PendingIntent pie = PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
                    try {
                        pie.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    //서버에 사용자와 가게간 거리 정보 전달(예약취소 관련은 AlertDialogActivity에서)
    private class meterTask extends AsyncTask<String, Void, String> {
        String address, sendMsg, receiveMsg = "";
     //   ProgressDialog dlg = new ProgressDialog(getApplicationContext());
        String result = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address = "http://00645.net/eat/menu_choice.php";
         //   dlg.setMessage("접속 중");
         //   dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
       //     dlg.dismiss();
            if (receiveMsg.equals("성공")) {
                //Toast.makeText(getApplicationContext(), "거리전달 성공", Toast.LENGTH_SHORT).show();
                if (result.equals("wait")) {//가게에서 확인 안함.
                    Toast.makeText(getApplicationContext(), "거리전달 완료, 가게 확인 중", Toast.LENGTH_SHORT).show();
                } else if (result.equals("ok")) {
                    Toast.makeText(getApplicationContext(), "거리전달 완료, 손님맞을 준비 중", Toast.LENGTH_SHORT).show();
                }


            } else {
                Toast.makeText(getApplicationContext(), "거리값전달 실패", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                URL url = new URL(address);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset:UTF-8");

                con.setDoInput(true);
                con.setDoOutput(true);
                OutputStreamWriter os = new OutputStreamWriter(con.getOutputStream());
                sendMsg = "app=user&user_id=" + strings[0] + "&menu_id=" + strings[1] + "&action=" + strings[2];//거리전달 추가.
                // userId, Menu, action(choice, away, arrival, cancel)
                os.write(sendMsg);
                os.flush();
                os.close();


                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {//stringbuffer에 계속 추가 저장
                    response.append(inputLine);
                }
                br.close();

                //전송받은 완성된 문자열을 JSON 객체에 넣는다.
                JSONObject jobj = new JSONObject(response.toString());//string buffer값을 json객체에 추가
                if (jobj.has("action_result")) {
                    result = jobj.getString("shop_result");//가게에서 정보 수신햇는지(wait or ok)
                    receiveMsg = "성공";
                } else {
                    receiveMsg = "실패";
                    //Toast.makeText(getApplicationContext(), "주소가 확인되지 않습니다. 정확한 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }//end metertask
}

