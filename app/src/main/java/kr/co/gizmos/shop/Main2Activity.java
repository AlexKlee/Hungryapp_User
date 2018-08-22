package kr.co.gizmos.shop;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Main2Activity extends AppCompatActivity {
    //메인화면뷰 선언
    ListView list1;
    Button btnRecom, btnSetup;
    //리스트아이템화면 뷰 선언
    TextView txlistDate, txlistCon;
    EditText edtPerson;

    ArrayList<MyData> arrMyData= new ArrayList<>();

    MyAdapter myadap;
    SharedPreferences mapref;

    //지도관련, 1)현재위치
    LocationManager lm;
    String longit, latit;//경도, 위도
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2_temp);
        setTitle("진짜메인화면(수정할것)");
        //액티비티 이동하며 아이디와 기존 방문내역 확인 후 출력

        edtPerson=findViewById(R.id.edtPerson);
        btnRecom=findViewById(R.id.btnRecom);
        btnSetup=findViewById(R.id.btnSetup);
        list1=findViewById(R.id.list1);



        //기존 식사 방문기록 나타내기
        recentFood();
        myadap=new MyAdapter(this);
        list1.setAdapter(myadap);
        setListViewHeight(list1);//리스트뷰 길이조절

        //현재위치 받아오기, GPS, NETWORK값 받아오기
        lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(Build.VERSION.SDK_INT>=23){//마시멜로이상이면 권한요청하기

            //권한 없는 경우
            if(ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION , Manifest.permission.ACCESS_FINE_LOCATION} , 1);
            }
            //권한이 있는 경우
            else{
                //현재 위치 요청
                requestMyLocation();
            }
        }
        else{
            //현재위치요청
            requestMyLocation();
        }
    /*
            mapref=getSharedPreferences("userPos",0);
            //현재 virtualMachine에서 테스트 안되므로 기본값은 부평역으로 설정됨.
            longit = mapref.getString("longt", "37.489473");//경도,
            latit=mapref.getString("latt","126.724765");//위도
            //37.489473, 126.724765
    */

        //네이버지도 표시.
        mapFragment mFrag= new mapFragment();
        mFrag.setArguments(new Bundle());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fTransaction = fm.beginTransaction();
        fTransaction.add(R.id.mapFrag, mFrag);
        fTransaction.commit();

        //메뉴추천
        btnRecom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //위치정보, 옵션정보 이용하여 서버에서 정보받아오기.
                Intent it = new Intent(getApplicationContext(), RandomRecommend.class);
                startActivity(it);
            }
        });//btnRecom end


        //추천 설정
        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //옵션화면으로이동, 옵션설정 받아오기.
                Intent itSetup= new Intent(getApplicationContext(), userOption.class);
                startActivity(itSetup);

            }
        });//btnSetup end





    }//onCreate end


    //최근 식사내용 요청문
    public void recentFood(){

        //shared preferences로변경할것
        //Intent it2= getIntent();
        mapref=getSharedPreferences("appData",MODE_PRIVATE);

        String jsonArray = mapref.getString("jsonarray","");
        try{
            JSONArray jarray = new JSONArray(jsonArray);
            for(int i=0; i<jarray.length(); i++){//jsonArray에서 JSONobject로 나눠서, 필요항목들 arraylist에 추가
                JSONObject jobj = jarray.getJSONObject(i);
                String index = jobj.getString("menu_idx");
                String menuname = jobj.getString("menu_name");
                String date = jobj.getString("date");
                MyData md = new MyData(index, date, menuname);
                arrMyData.add(md);
            }
        }catch (JSONException je){
            je.printStackTrace();
        }
    }//recentfood end

    //리스트뷰 높이 조절 메소드
    public static void setListViewHeight(ListView listView){
        ListAdapter listAdapter = listView.getAdapter();
        if(listAdapter==null){
            return;
        }

        int totalHeight=0;
        int desirewidth=View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for(int i=0; i<listAdapter.getCount(); i++){
            View listItem=listAdapter.getView(i,null, listView);
            listItem.measure(desirewidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight+=listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params=listView.getLayoutParams();
        params.height=totalHeight + (listView.getDividerHeight()*(listAdapter.getCount()-1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }//listviewheight end


    //리스트뷰용어댑터
    class MyAdapter extends BaseAdapter {
        Context con;
        MyAdapter(Context c){
            con=c;
        }

        @Override

        public int getCount() {
            return arrMyData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                LayoutInflater inf = LayoutInflater.from(con);
                convertView=inf.inflate(R.layout.list_view,parent,false);
            }
            txlistDate=convertView.findViewById(R.id.txlistDate);
            txlistCon=convertView.findViewById(R.id.txlistCon);

            MyData myd = arrMyData.get(position);

            txlistDate.setText(myd.date);
            txlistCon.setText(myd.contents);


            return convertView;
        }
    }//adapter end

    //현재위치값 찾기


    //지도 검색




    //나의 위치 요청
    public void requestMyLocation(){
        if(ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        //요청
        //gps검색
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, mlocationListener);
        //네트워크(와이파이)검색
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 10, mlocationListener);
    }

    //권한 요청후 응답 콜백
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //ACCESS_COARSE_LOCATION 권한
        if(requestCode==1){
            //권한받음
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                requestMyLocation();
            }
            //권한못받음
            else{
                Toast.makeText(this, "권한없음", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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
            //}
            longit=String.valueOf(longitude);
            latit=String.valueOf(latitude);
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            Toast.makeText(getApplicationContext(),"위치정보 : " + provider + "\n경도 : " + longitude + "\n위도 : " + latitude,
                    Toast.LENGTH_SHORT).show();
            lm.removeUpdates(mlocationListener);

            //임시저장
            mapref=getSharedPreferences("appData",MODE_PRIVATE);
            SharedPreferences.Editor mapeditor=mapref.edit();

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
    };


}//main end