package kr.co.gizmos.shop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class userOption extends AppCompatActivity {
    LinearLayout optionLayout;
    Button btnSetupYes;

    Spinner spinRangeDist, spinPrice1, spinPrice2;
    RadioGroup rg1;
    RadioButton rbInc,rbExc;

   /* CheckBox[] chM=new CheckBox[10];//재료
    CheckBox[] chOri=new CheckBox[3];//원산지
    CheckBox[] chCu=new CheckBox[13];//조리법
    CheckBox[] chTy=new CheckBox[8];//음식유형*/

    CheckBox checkboxes[]= new CheckBox[34];
   /* int chkIds[]={R.id.chM1, R.id.chM2, R.id.chM3, R.id.chM4, R.id.chM5, R.id.chM6, R.id.chM7, R.id.chM8, R.id.chM9, R.id.chM10,
    R.id.chCu1, R.id.chCu2, R.id.chCu3, R.id.chCu4, R.id.chCu5, R.id.chCu6, R.id.chCu7, R.id.chCu8, R.id.chCu9, R.id.chCu10, R.id.chCu11, R.id.chCu12, R.id.chCu13,
    R.id.chTy1, R.id.chTy2, R.id.chTy3, R.id.chTy4, R.id.chTy5, R.id.chTy6, R.id.chTy7, R.id.chTy8,
    R.id.chOri1, R.id.chOri2, R.id.chOri3};*/

    //옵션값 저장
    String menutag, range, pay_min, pay_max;

    SharedPreferences oppref;
    String id;
    //1.추천거리(미터단위)
    //2.추천가격대
    //3.추천유형(기존선택지역 포함 유무)
    //4.음식종류체크박스

    String menut[]= new String[34];
    //돼지고기,소고기,닭고기,양고기,기타육류,생선,갑각류,조개류,곡물,채식, 10 재료
    // 찌개,탕,볶음,구이,조림,국,튀김,찜,삶은,말린,발효,숙성,안 익힘, 13조리법
    // 가정식,백반,한식,중식,양식,분식,일식,기타외국식, 8음식유형
    // 국내산,수입산,혼합  원산지


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_option);
        setTitle("옵션변경");

        //체크박스 연결
        for(int i=0; i<34; i++){
            if(i<10){//재료  0~9번
                int chid=getResources().getIdentifier("chM"+(i+1),"id",getPackageName());
                checkboxes[i]=findViewById(chid);
            }
            else if(i<23){//조리법 10~22번
                int chid=getResources().getIdentifier("chCu"+(i-9), "id", getPackageName());
                checkboxes[i]=findViewById(chid);
            }
            else if(i<31){//음식유형 23~30번
                int chid=getResources().getIdentifier("chTy"+(i-22), "id", getPackageName());
                checkboxes[i]=findViewById(chid);
            }
            else if(i<34){//원산지  31~33
                int chid=getResources().getIdentifier("chOri"+(i-30), "id", getPackageName());
                checkboxes[i]=findViewById(chid);
            }
        }


        //int chid=getResources().getIdentifier("chM"+1, "id", getPackageName());
        //Toast.makeText(getApplicationContext(), "chid:"+chid+", R.id:"+R.id.chM1,Toast.LENGTH_SHORT).show();
        spinRangeDist=findViewById(R.id.spinRangeDist);
        spinPrice1=findViewById(R.id.spinPrice1);
        spinPrice2=findViewById(R.id.spinPrice2);
        rg1=findViewById(R.id.rg1);
        rbInc=findViewById(R.id.rbInc);
        rbExc=findViewById(R.id.rbExc);
        optionLayout=findViewById(R.id.optionLayout);
        btnSetupYes=findViewById(R.id.btnSetupYes);

        //scrollbar 설정 변경,
        optionLayout.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        optionLayout.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);     //영역추가없이 내용물 안쪽에 투명하게 스크롤바 생성


        load();

        if(menut.length==34) {
            for (int i = 0; i < 34; i++) {
                checkboxes[i].setChecked(true);
            }
        }



        btnSetupYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });//btn end


        //기존 옵션 불러와서 표시.

    }//onCreate end


    private void load(){//접속해서 option불러오기.
        oppref= getSharedPreferences("appData", MODE_PRIVATE);
        id=oppref.getString("user_id","");
        HttpTask optLoad= new HttpTask();
        optLoad.execute(id);

    }

    //아이디로 로그인, 기존 설정메뉴 불러오기.
    private class HttpTask extends AsyncTask<String, Void, String> {
        String address="", sendMsg="", receiveMsg="";
        ProgressDialog dlg = new ProgressDialog(userOption.this);


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
            if(receiveMsg.equals("성공")){
                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
             //   Toast.makeText(getApplicationContext(), range+","+pay_max+","+pay_min+","+menutag, Toast.LENGTH_SHORT).show();
                //로그인 성공시 값불러오기.

                menut=menutag.split(",");


            }
            else{
                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
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
                if(jobj.has("range")) {
                    receiveMsg = "성공";
                    range=jobj.getString("range");
                    pay_max=jobj.getString("pay_max");
                    pay_min=jobj.getString("pay_min");
                    menutag=jobj.getString("tag");
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
