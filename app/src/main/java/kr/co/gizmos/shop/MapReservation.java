package kr.co.gizmos.shop;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.maplib.NGeoPoint;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapReservation extends AppCompatActivity {
    //1. 예약확인 버튼 누른 후 지도표시, 지도 버튼 클릭 시 앱 실행으로 이동? (현재위치 변경에 따라 현재위치 마크 변경?)
    //2. 일정 거리 이상 멀어지거나 가까워지면 다이얼로그 출력
    //2-1. 멀어질 경우 : 방문하지 않으시겠습니까?
    //2-2. 가까워질 경우 : 도착하셨습니까?

    //notification builder로 앱 종료시에도 켜져있게 설정, 백그라운드로 해야하나.
    //1. 가게 도착여부 확인
    //2. 도착했다고했을 시 리뷰 알림 발생
    Button btnReview;
    static TextView txRes;
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
        txRes=findViewById(R.id.txRes);
        //지도 표시 - 현재위치, 가게위치.
        //현재위치 변경에 따라 마크 이동, 초점이동.
        rePref=getSharedPreferences("appData", MODE_PRIVATE);
        SharedPreferences.Editor editor = rePref.edit();
        editor.putString("activity_name", "MapReservation");// 현재 액티비티 위치 mapFragment2에 전달
        editor.commit();

        String userid= rePref.getString("user_id","");
        String choice ="choice";//메뉴선택확정
        String person = rePref.getString("rnum","1");//사람수
        String menuid=rePref.getString("menu_id","");
        meterTask choiceFinish = new meterTask();
        choiceFinish.execute(userid,menuid,choice,person);
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
                Intent it= new Intent(MapReservation.this, ReviewActivity.class);
                startActivity(it);
            }
        });







    }//onCreate end
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
                    Toast.makeText(getApplicationContext(), "예약전달 완료, 가게 확인 중", Toast.LENGTH_SHORT).show();
                } else if (result.equals("ok")) {
                    Toast.makeText(getApplicationContext(), "예약전달 완료, 손님맞을 준비 중", Toast.LENGTH_SHORT).show();
                }


            } else {
                Toast.makeText(getApplicationContext(), "예약전달 실패", Toast.LENGTH_SHORT).show();
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
                sendMsg = "app=user&user_id=" + strings[0] + "&menu_id=" + strings[1] + "&action=" + strings[2]+"&person="+strings[3];//거리전달 추가.
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
}//main end
