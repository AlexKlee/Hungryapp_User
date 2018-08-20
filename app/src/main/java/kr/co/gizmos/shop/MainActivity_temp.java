package kr.co.gizmos.shop;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity_temp extends AppCompatActivity {

  Button btnStart;
  String id=null;// db에 저장된 폰 id값

  private SharedPreferences appData;//아이디 자체저장위한 객체
  private boolean saveLogin;//아이디 저장여부.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("뭐 드시려고?");
        //첫 메인화면, 일정 시간 애니메이션 보여준 후 바로 지나갈지, 버튼을 눌러서 지나갈지 결정

        //기존에 설정된 id값 불러오기.
        appData= getSharedPreferences("appData", MODE_PRIVATE);
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

                Intent it = new Intent(getApplicationContext(), Main2Activity.class);
                startActivity(it);
                //db와 통신, 아이디 있을 경우 로그인, 없으면 새로 생성
            }
        });



    }//onCreate end

    private void save(){//id값 저장하는 함수
        SharedPreferences.Editor editor = appData.edit();

        //에디터 객체.put타입(저장시킬 이름, 저장시킬 값)
        //저장시킬 이름이 이미존재하면 저장 안함
        HttpTask2 saveTask = new HttpTask2();
        saveTask.execute();
        editor.putBoolean("save_login", true);//저장여부설정
        editor.putString("user_id", id); //db에서 받아온 아이디 값 저장
        Log.i("id_save", id);
    }

    private void load(){//id값 불러오는 함수
        saveLogin=appData.getBoolean("save_login", false);//뒤에는 default값
        id=appData.getString("user_id", "");
        Log.i("id_load", id);

    }

    //아이디가 없을 경우 아이디 생성
    private class HttpTask2 extends AsyncTask<String, Void, String>{
        String address, receiveMsg;
        ProgressDialog dlg = new ProgressDialog(MainActivity_temp.this);


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
            //?

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
                    Log.i("id", id);
                }
                else {
                    receiveMsg="실패";
                    Toast.makeText(getApplicationContext(), "주소가 확인되지 않습니다. 정확한 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    Log.i("id_fail", id);
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
        ProgressDialog dlg = new ProgressDialog(MainActivity_temp.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address="http://00645.net/eat/user_load.php";
            dlg.setMessage("접속 중");
            dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dlg.dismiss();
            //?
        }

        @Override
        protected String doInBackground(String... strings) {
            JSONObject result;
            int total=0;
            try{
                URL url = new URL(address);
              /*  HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                conn.setRequestProperty("Content-type", "application/x-www-from-urlencoded;charset:UTF-8");
                conn.setRequestMethod("POST");

                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg="app=user&user_id="+strings[0];
                osw.write(sendMsg);
                osw.flush();*/


                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("app", "user");
                con.setRequestProperty("user_id", strings[0]);
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
                if(jobj.has("result")) {
                    result = jobj.getJSONObject("result");
                    total = result.getInt("total");
                    if (total >= 1) {
                        JSONArray items = result.getJSONArray("items");
                        receiveMsg = "성공";
                    }
                }

                else {
                    receiveMsg = "실패";
                    Toast.makeText(getApplicationContext(), "주소가 확인되지 않습니다. 정확한 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }



            } catch (Exception e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }



/*
    private class SendPost extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... voids) {
            String content = executeClient();
            return content;
        }

        protected void onPostExecute(String result){
            //모두 작업을 마치고 실행할 일(메소드 등등)
        }

        //실제 전송하는 부분
        public String executeClient(){
            ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
            //전송할 값 추가
            String id = null;//폰에서 저장하고 있는 id 값, 수정할것!
            post.add(new BasicNameValuePair("id", id));

            //연결 HttpClient 객체 생성
            HttpClient client = new DefaultHttpClient();

            //객체 연결설정부분, 연결 최대시간 등
            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params,5000);
            HttpConnectionParams.setSoTimeout(params,5000);

            //Post객체 생성
            HttpPost httpPost= new HttpPost("연결할 웹주소 입력!!");// 연결할 웹주소입력!!
            try{
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post, "UTF-8");
                httpPost.setEntity(entity);;
                client.execute(httpPost);
                return EntityUtils.getContentCharSet(entity);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }*/

}//main end
