package kr.co.gizmos.shop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

  Button btnStart;
  String id=null;// db에 저장된 폰 id값

  SharedPreferences appData;//아이디 자체저장위한 객체
  private boolean saveLogin;//아이디 저장여부.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("뭐 드시려고?");
        //첫 메인화면, 일정 시간 애니메이션 보여준 후 바로 지나갈지, 버튼을 눌러서 지나갈지 결정

        //기존에 설정된 id값 불러오기.
        load();


        if(id==""){//만약 저장된 id값이 없다면.
            save();
        }

        btnStart=findViewById(R.id.btnStart);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //아이디확인(로그인)
                HttpTask loginTask = new HttpTask();
                loginTask.execute(id);

                //db와 통신, 아이디 있을 경우 로그인, 없으면 새로 생성
            }
        });


    }//onCreate end


    private void save(){//id값 저장하는 함수
        //에디터 객체.put타입(저장시킬 이름, 저장시킬 값)
        //저장시킬 이름이 이미존재하면 저장 안함
        HttpTask2 saveTask = new HttpTask2();
        saveTask.execute();
    }

    private void load(){//id값 불러오는 함수
        appData= getSharedPreferences("appData", MODE_PRIVATE);
        saveLogin=appData.getBoolean("save_login", false);//뒤에는 default값
        id=appData.getString("user_id", "");
        Log.i("checkid_load", id);
    }

    //아이디가 없을 경우 아이디 생성
    private class HttpTask2 extends AsyncTask<String, Void, String>{
        String address, receiveMsg;
        ProgressDialog dlg = new ProgressDialog(MainActivity.this);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address="http://00645.net/eat/register.php?app=user";
            dlg.setMessage("아이디생성 중..");
            dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dlg.dismiss();
            if(receiveMsg.equals("성공")){//아이디 생성 성공 시, 아이디 저장
                appData= getSharedPreferences("appData", MODE_PRIVATE);
                SharedPreferences.Editor editor = appData.edit();
                editor.putBoolean("save_login", true);//저장여부설정
                editor.putString("user_id", id); //db에서 받아온 아이디 값 저장
                editor.commit();
                Log.i("checkid_save", id);
            }
            else{
                Toast.makeText(getApplicationContext(), "아이디 생성 실패", Toast.LENGTH_SHORT).show();
            }


        }

        @Override
        protected String doInBackground(String... strings) {

            try{
                URL url = new URL(address);

                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestProperty("Content-type", "application/x-www-from-urlencoded;charset:UTF-8");
                con.setRequestMethod("POST");

                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();

                //전송받은 완성된 문자열을 JSON 객체에 넣는다.
                JSONObject jobj = new JSONObject(response.toString());
                if(jobj.has("user_id")) {
                    id=jobj.getString("user_id");
                    receiveMsg="성공";
                    Log.i("checkid", id);
                }
                else {
                    receiveMsg="실패";
                    Toast.makeText(getApplicationContext(), "주소가 확인되지 않습니다. 정확한 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    Log.i("checkid_fail", id);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }


    //아이디로 로그인시.
    private class HttpTask extends AsyncTask<String, Void, String>{
        String address, sendMsg, receiveMsg;
        ProgressDialog dlg = new ProgressDialog(MainActivity.this);
        JSONArray items;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address="http://00645.net/eat/last_menu.php";
            dlg.setMessage("접속 중");
            dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dlg.dismiss();
            if(receiveMsg.equals("성공")){
                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                //로그인 성공시 intent이동
                Intent it = new Intent(getApplicationContext(), Main2Activity.class);
                it.putExtra("jsonArray", items.toString());
                startActivity(it);
            }
            else{
                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {

            int total=0;//최근 먹은 음식갯수
            try{
                URL url = new URL(address);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset:UTF-8");

                con.setDoInput(true);
                con.setDoOutput(true);
                OutputStreamWriter os =new OutputStreamWriter(con.getOutputStream());
                sendMsg="app=user&user_id="+strings[0];
                os.write(sendMsg);
                os.flush();
                os.close();


                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
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
                if(jobj.has("list_item")) {
                    //result = jobj.getJSONObject("list_item");
                    receiveMsg = "성공";
                    total = jobj.getInt("list_item");//?변수 설정필요
                    if (total >= 1) {
                        items = jobj.getJSONArray("menu");
                    }
                }
                else {
                    receiveMsg = "실패";
                    //Toast.makeText(getApplicationContext(), "주소가 확인되지 않습니다. 정확한 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }

}//main end
