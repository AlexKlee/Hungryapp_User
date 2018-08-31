package kr.co.gizmos.shop;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.maplib.NGeoPoint;

public class MapReservation extends AppCompatActivity {
    //1. 예약확인 버튼 누른 후 지도표시, 지도 버튼 클릭 시 앱 실행으로 이동? (현재위치 변경에 따라 현재위치 마크 변경?)
    //2. 일정 거리 이상 멀어지거나 가까워지면 다이얼로그 출력
    //2-1. 멀어질 경우 : 방문하지 않으시겠습니까?
    //2-2. 가까워질 경우 : 도착하셨습니까?

    //notification builder로 앱 종료시에도 켜져있게 설정, 백그라운드로 해야하나.
    //1. 가게 도착여부 확인
    //2. 도착했다고했을 시 리뷰 알림 발생

    Button btnReview;
    NGeoPoint ngpoint;
    InputMethodManager im;
    SharedPreferences rePref;
    NMapLocationManager nLoc;

    LocationManager lm;
    String longit, latit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_reservation);
        setTitle("지도안내");

        btnReview=findViewById(R.id.btnReview);
        //지도 표시 - 현재위치, 가게위치.
        //현재위치 변경에 따라 마크 이동, 초점이동.
        rePref=getSharedPreferences("appData", MODE_PRIVATE);
        SharedPreferences.Editor editor = rePref.edit();
        editor.putString("activity_name", "MapReservation");// 현재 액티비티 위치 mapFragment2에 전달
        editor.commit();

        lm= (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        //nLoc= new NMapLocationManager(this);//?
        //ngpoint=new NGeoPoint();

        mapFragment2 mFrag2_1= new mapFragment2();
        mFrag2_1.setArguments(new Bundle());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fTransaction = fm.beginTransaction();
        fTransaction.add(R.id.mapFrag3, mFrag2_1);
        fTransaction.commit();
        im=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);


        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });




    }//onCreate end

/*

    private NMapLocationManager.OnLocationChangeListener activityLocChangeListener = new NMapLocationManager.OnLocationChangeListener() {
        @Override
        public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {

            return true;
        }

        @Override
        public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {

        }

        @Override
        public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {

        }
    };
*/


   /* public void requestMyLocation(){
        if(ContextCompat.checkSelfPermission(MapReservation.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MapReservation.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        //요청
        //gps검색
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, mlocationListener);
        //네트워크(와이파이)검색
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 10, mlocationListener);
    }
    //위치정보 구하기 리스너
    private final LocationListener mlocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            String provider = location.getProvider();   //위치제공자
            //if(longitude==0||latitude==0){
            latitude =37.49085971;
            longitude  =126.72073882;
            //default부평역
            //
            longit=String.valueOf(longitude);
            latit=String.valueOf(latitude);
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            Toast.makeText(getApplicationContext(),"위치정보 : " + provider + "\n경도 : " + longitude + "\n위도 : " + latitude,
                    Toast.LENGTH_SHORT).show();
            lm.removeUpdates(mlocationListener);

            //임시저장
            rePref=getSharedPreferences("appData",MODE_PRIVATE);
            SharedPreferences.Editor mapeditor=rePref.edit();

            mapeditor.putString("longt",longit);
            mapeditor.putString("latt",latit);
            mapeditor.commit();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { Log.d("gps", "onStatusChanged"); }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };*/
}//main end
