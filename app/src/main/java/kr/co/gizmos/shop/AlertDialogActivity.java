package kr.co.gizmos.shop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlertDialogActivity extends Activity {
    TextView txdlgTitle, txdlgMessage;
    Button btndlgYes, btndlgCancel;
    SharedPreferences dlgpref;
    String type="";
    String id, menuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_alert_dialog);
        setTitle("현재위치");
        txdlgTitle=findViewById(R.id.txdlgTitle);
        txdlgMessage=findViewById(R.id.txdlgMessage);
        btndlgYes=findViewById(R.id.btndlgYes);
        btndlgCancel=findViewById(R.id.btndlgCancel);
        dlgpref=getSharedPreferences("appData", MODE_PRIVATE);
        type=dlgpref.getString("type","");
        id=dlgpref.getString("user_id","");
        menuid=dlgpref.getString("menu_id","");
        if(type.equals("예약취소")){//예약취소확인 다이얼로그
            txdlgTitle.setText("멀어지고 있습니다.");
            txdlgMessage.setText("예약을 취소하시겠습니까?");
        }
        else if(type.equals("곧 도착")){
            txdlgTitle.setText("근처입니다.");
            txdlgMessage.setText("도착하셨습니까?");
        }


        //확인 버튼//예약취소버튼. or 도착확인 버튼
        btndlgYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type.equals("예약취소")){//확인 누를 시 예약취소
                    SharedPreferences.Editor editor = dlgpref.edit();
                    editor.putString("dlg", "service_quit_cancel");
                    editor.commit();
                    Intent itMain2= new Intent(AlertDialogActivity.this, RandomRecommend.class);
                    startActivity(itMain2);
              //      RandomRecommend rr = new RandomRecommend();
            //        rr.stopService(new Intent(getApplicationContext(), MyService.class));
                    MyService ms=new MyService();
                    ms.onDestroy();

                    meterTask2 cancelTask = new meterTask2();
                    cancelTask.execute(id, menuid, "cancel");//예약취소
                    finish();
                }else{//확인 누름시 도착완료
                    //리뷰페이지로 이동
                    SharedPreferences.Editor editor = dlgpref.edit();
                    editor.putString("dlg", "service_quit_entrance");
                    editor.commit();
                    Intent itMain2= new Intent(AlertDialogActivity.this, RandomRecommend.class);
                    startActivity(itMain2);
                    MyService ms=new MyService();
                    ms.onDestroy();
                    Toast.makeText(getApplicationContext(), "Bon Appetit~ \n 식사 맛있게 하세요~!", Toast.LENGTH_SHORT).show();
                    meterTask2 arriveTask = new meterTask2();
                    arriveTask.execute(id, menuid, "arrival");//도착완료
                    finish();
                }
            }
        });

        //취소버튼
        btndlgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type.equals("예약취소")){
                    finish();
                }else{
                    finish();
                }
            }
        });

    }

    public class meterTask2 extends AsyncTask<String, Void, String> {
        String address, sendMsg, receiveMsg="";
     //   ProgressDialog dlg = new ProgressDialog(getApplicationContext());
        String result=null;
        String rvid=null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address="http://00645.net/eat/menu_choice.php";
      //      dlg.setMessage("접속 중");
      //      dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
  //          dlg.dismiss();
            if(receiveMsg.equals("성공")){
                //Toast.makeText(getApplicationContext(), "거리전달 성공", Toast.LENGTH_SHORT).show();
                if(result.equals("wait")){//가게에서 확인 안함.
                    if(type.equals("예약취소")){
                        Toast.makeText(getApplicationContext(), "예약취소완료, 가게 확인 중",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "도착완료, 가게 확인 중",Toast.LENGTH_SHORT).show();
                    }
                    SharedPreferences sepref = getSharedPreferences("appData",MODE_PRIVATE);
                    SharedPreferences.Editor edit= sepref.edit();
                    edit.putString("rv_id",rvid);
                    edit.commit();
                }
                else if(result.equals("ok")){
                    if(type.equals("예약취소")){
                        Toast.makeText(getApplicationContext(), "예약취소완료, 가게 확인완료",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "도착완료, 가게 확인 완료",Toast.LENGTH_SHORT).show();
                    }
                }
                /*//로그인 성공시 sharedpreferrence값 저장
                fragPref= getActivity().getSharedPreferences("appData", MODE_PRIVATE);
                SharedPreferences.Editor editor = fragPref.edit();
                editor.putString("jsonarray", items.toString());//jsonarray내용 String으로 변환해서 저장
                editor.commit();
                Intent it = new Intent(getActivity(), Main2Activity.class);
//                it.putExtra("jsonArray", items.toString());
                startActivity(it);*/
            }
            else{
                Toast.makeText(getApplicationContext(), "거리값전달 실패", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {

            try{
                URL url = new URL(address);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset:UTF-8");

                con.setDoInput(true);
                con.setDoOutput(true);
                OutputStreamWriter os =new OutputStreamWriter(con.getOutputStream());
                sendMsg="app=user&user_id="+strings[0]+"&menu_id="+strings[1]+"&action="+strings[2];//거리전달 추가.
                // userId, Menu, action(choice, away, arrival, cancel)
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
                if(jobj.has("action_result")) {
                    result=jobj.getString("shop_result");//가게에서 정보 수신햇는지(wait or ok)
                    rvid=jobj.getString("rv_idx");
                    receiveMsg="성공";


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


}
