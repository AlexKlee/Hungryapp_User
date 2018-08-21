package kr.co.gizmos.shop;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RandomRecommend extends AppCompatActivity {

    TextView txMenu;
    TextView txShopInfo, txOtherMenu;
    ImageView foodImage;

    SharedPreferences ranPref;

    //보내기 id와 현재위치.
    //받기->변수 선언(가게명, 대표명, 연락처, 주소, 가게위치, 테이블 수, 의자최소, 의자 최대,
    //  평점, 선택 인원(수?), 가게정보업데이트 날짜, 이미지 주소, 메뉴가격, 몇인분, 1인분당 가격,메뉴설명
    //  rsl_date, mi_date,다른메뉴


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_recommend);
        setTitle("메뉴선택");

        txMenu=findViewById(R.id.txMenu);
        txShopInfo=findViewById(R.id.txShopInfo);
        txOtherMenu=findViewById(R.id.txOtherMenu);
        foodImage=findViewById(R.id.foodImage);



    }//onCreate end


    //메뉴 추천받기.
    private class HttpTask extends AsyncTask<String, Void, String> {
        String address="", sendMsg="", receiveMsg="";
        ProgressDialog dlg = new ProgressDialog(RandomRecommend.this);
        JSONObject jShop= new JSONObject();
        JSONObject jMenu= new JSONObject();
        String otherMenu;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address="http://http://00645.net/eat/menu_find.php";
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

              //String shopName, shopOwner, shopTel, shopAddr,  shopAddrX, shopAddrY, shopTbl ,shopChairMin, shopChairMax,
                //    imageUrl,choiceScore, choiceCount, shopUpdate, menuId,menuName,menuImage,menuPrice,menuPer,
                // menuPerPrice, mi_Comment, menuUpdate,otherMenu;
                String str[]=new String[22];
                try {
                    str[0]=jShop.getString("rs_shop_name");
                    str[1]=jShop.getString("rs_operator");
                    str[2]=jShop.getString("rs_tel");
                    str[3]=jShop.getString("rs_address");
                    str[4]=jShop.getString("rs_map_x");
                    str[5]=jShop.getString("rs_map_y");
                    str[6]=jShop.getString("distance");
                    str[7]=jShop.getString("rs_table_count");
                    str[8]=jShop.getString("rs_table_size_min");
                    str[9]=jShop.getString("rs_table_size_mx");
                    str[10]=jShop.getString("rs_choice_score");
                    str[11]=jShop.getString("rs_choice_count");
                    str[12]=jShop.getString("rs_ldate");

                    str[13]=jMenu.getString("mi_idx");
                    str[14]=jMenu.getString("mi_name");
                    str[15]=jMenu.getString("mi_img");
                    str[16]=jMenu.getString("mi_price");
                    str[17]=jMenu.getString("mi_per");
                    str[18]=jMenu.getString("mi_per_price");
                    str[19]=jMenu.getString("mi_comment");
                    str[20]=jMenu.getString("mi_date");
                    str[21]=otherMenu;


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RecomData rData = new RecomData(str[0],str[1],str[2],str[3],str[4],str[5],str[6],str[7],str[8],
                        str[9],str[10],str[11],str[12],str[13],str[14],str[15],str[16],str[17],str[18],str[19],str[20],str[21]);

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
                sendMsg="app=user&user_id="+strings[0]+"&map_x="+strings[1]+"&map_y="+strings[2];
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
                if(jobj.has("shop_info")) {
                    receiveMsg = "성공";
                    jShop=jobj.getJSONObject("shop_info");
                    jMenu=jobj.getJSONObject("menu_info");
                    otherMenu=jobj.getString("other_menu");
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
