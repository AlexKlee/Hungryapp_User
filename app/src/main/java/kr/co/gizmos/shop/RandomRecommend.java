package kr.co.gizmos.shop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.FrameLayout;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RandomRecommend extends AppCompatActivity {
    //LinearLayout mapFrag2;
    LinearLayout mapFrag2;
    TextView txMenu;
    TextView txShopInfo, txOtherMenu;
    ImageView foodImage;

    Button btnYes, btnAgain;


    //보내기 id와 현재위치.
    //받기->변수 선언(가게명, 대표명, 연락처, 주소, 가게위치, 테이블 수, 의자최소, 의자 최대,
    //  평점, 선택 인원(수?), 가게정보업데이트 날짜, 이미지 주소, 메뉴가격, 몇인분, 1인분당 가격,메뉴설명
    //  rsl_date, mi_date,다른메뉴

    //String shopName, shopOwner, shopTel, shopAddr,  shopAddrX, shopAddrY, shopTbl ,shopChairMin, shopChairMax,
    //    imageUrl,choiceScore, choiceCount, shopUpdate, menuId,menuName,menuImage,menuPrice,menuPer,
    // menuPerPrice, mi_Comment, menuUpdate,otherMenu;

    final String str[]=new String[22];//받아온 변수값 저장
    Bitmap bitmap;
    String otherMenu="";

    String person;//예약인원 수

    final int distTime=0;

    InputMethodManager im;//지도

    SharedPreferences rcpref;


    @SuppressLint("ClickableViewAccessibility")//지도 앱 터치시 스크롤이 고정되게 만들어줌,
    //suppressLint는 검사에서 제외할 항목ID 입력., 쓸데없는 검사를 빼고 빠르게 실행
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_recommend);
        setTitle("메뉴선택");
        rcpref=getSharedPreferences("appData",MODE_PRIVATE);
        String servi=rcpref.getString("dlg","");
        if(servi.equals("service quit")){
            Intent quitsvit = new Intent(this, MyService.class);
            stopService(quitsvit);
            SharedPreferences.Editor editor = rcpref.edit();
            editor.putString("dlg", "fine");
            editor.commit();

            Intent it2 = new Intent(this, Main2Activity.class);
            startActivity(it2);
            finish();
        }


        txMenu=findViewById(R.id.txMenu);
        txShopInfo=findViewById(R.id.txShopInfo);
        txOtherMenu=findViewById(R.id.txOtherMenu);
        foodImage=findViewById(R.id.foodImage);

        btnAgain=findViewById(R.id.btnAgain);
        btnYes=findViewById(R.id.btnYes);
        mapFrag2=findViewById(R.id.mapFrag2);

        //유저 아이디 좌표위치 전송 및
        //랜덤 음식메뉴와 가게정보 받아오기.

        String id= rcpref.getString("user_id","");
        String map_x = rcpref.getString("longt", "");
        String map_y=rcpref.getString("latt","");
        person=rcpref.getString("rnum", "1");//전 액티비티에서 확인을 너무빨리누르면 0으로 값이 전달됨.
        HttpTask loadFood = new HttpTask();
        loadFood.execute(id, map_x, map_y,person);


        //그래이거먹자 클릭하여, 가게업자에게 고객 방문예정 알림 보내기,
        //가는길 지도 표기, 멀어지거나 가까워지면 다이얼로그출력(안갈꺼냐? 들어갈꺼냐?로 구분)
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //다이얼로그 예약요청?예약반려?( 확인? 취소?)
                //다이얼로그 접근중? 멀어지는 중?
                //백그라운드??????

                //예약확인여부.
                AlertDialog.Builder dlgReservation = new AlertDialog.Builder(RandomRecommend.this);
                dlgReservation.setTitle("예약확인");

                dlgReservation.setMessage("예약하시겠습니까?\n"+"예약인원 : "+ person+"명");
                dlgReservation.setPositiveButton("확인", new DialogInterface.OnClickListener() {//
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), str[0]+"(으)로 "+person+"명 예약되었습니다.",Toast.LENGTH_SHORT).show();

                        //notification bar 실행할것!!

                        //mapReservaion 액티비티로 이동
                        Intent itR= new Intent(getApplicationContext(), MapReservation.class);
                        startActivity(itR);
                        finish();
                        Intent its= new Intent(RandomRecommend.this, MyService.class);
                        startService(its);

                    }
                });//
                dlgReservation.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"예약 취소되었습니다.",Toast.LENGTH_SHORT).show();
                    }
                });

                dlgReservation.show();

/*                NotificationManager notifiManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                //푸시바 누르면 이동 화면 설정
                Intent notiIntent = new Intent(RandomRecommend.this, MapReservation.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(RandomRecommend.this,0,notiIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Builder notiBuilder = new Notification.Builder(RandomRecommend.this);
                notiBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_map_no_02))
                .setSmallIcon(R.drawable.ic_map_no_02).setContentTitle("그냥 이거먹자")
                .setContentText("").setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent).setAutoCancel(false)//터치하면 종료
                .setNumber(1);//거리로?
                notiBuilder.setOngoing(false);
                notifiManager.notify(0, notiBuilder.build());*/
            }
        });

        //다시 메뉴선택. --> 현재 화면 갱신? 아니면 메인화면으로 이동?
        btnAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //메인2로 이동
                Intent it2= new Intent(getApplicationContext(),Main2Activity.class);
                startActivity(it2);
                finish();
            }
        });

    }//onCreate end


    //메뉴 추천받기.
    private class HttpTask extends AsyncTask<String, Void, String> {
        String address="", sendMsg="", receiveMsg="";
        ProgressDialog dlg = new ProgressDialog(RandomRecommend.this);
        JSONObject jShop= new JSONObject();
        JSONObject jMenu= new JSONObject();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address="http://00645.net/eat/menu_find.php";
            dlg.setMessage("접속 중");
            dlg.show();
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            dlg.dismiss();
            if(receiveMsg.equals("성공")){
                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                //로그인 성공시 값불러오기.

                try {
                    str[0]=jShop.getString("rs_shop_name");
                    str[1]=jShop.getString("rs_operator");
                    str[2]=jShop.getString("rs_tel");
                    str[3]=jShop.getString("rs_address");
                    str[4]=jShop.getString("rs_map_x");//
                    str[5]=jShop.getString("rs_map_y");//
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

                //가게 좌표값,주소 저장
                rcpref=getSharedPreferences("appData",MODE_PRIVATE);
                SharedPreferences.Editor editor=rcpref.edit();
                editor.putString("shop_map_x",str[4]);
                editor.putString("shop_map_y",str[5]);
                editor.putString("activity_name", "RandomRecommend");//현재 이 액티비티에서 실행하고 있다고. pref에 저장., fragment에서 어떤 앱에서 호출하는지 판단 가능하도록.
                editor.commit();

                //메뉴정보표시
                txMenu.setText(str[14]+"\n"+ "가격 : "+ str[16]+"원 / "+ str[17]+"인분\n"+"거리 : "+"약 "+str[6]+"m    "+ distTime+"\n"
                );
                        //"대표 : "+str[1]+"\n"+"연락처 : "+ str[2]+"\n"+"주소 : "+ str[3]+"\n"+

                //가게정보표시
                txShopInfo.setText(str[0]+"\n"+"대표 : "+str[1]+"\n"+ "주소 : "+str[3]+"\n"+ "연락처 : "+ str[2]+"\n"+
                "테이블 수 : "+str[7]+"\n"+"테이블 좌석 수 : "+str[8]+" ~ "+str[9]+"\n"+"평점 : "+ str[10]+"\n"+"방문횟수 : "+str[11]);

                otherMenu=otherMenu.substring(1, otherMenu.length()-1);
                otherMenu=otherMenu.replace("\"", " ");
                String[] oMenu=otherMenu.split(",");
                //다른 메뉴 표기
                txOtherMenu.setText(oMenu[0]+"\n"+oMenu[1]+"\n"+oMenu[2]+"\n"+oMenu[3]);

                //이미지 표기
                Thread imaThread = new Thread(){//이미지 url위한 thread
                    @Override
                    public void run() {
                        try{
                        //    String t = "1";
                            URL url= new URL(str[15]);
                            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                            conn.setDoInput(true);
                            conn.connect();

                            InputStream is = conn.getInputStream();
                            bitmap= BitmapFactory.decodeStream(is);
                        }catch(IOException ie){
                            ie.printStackTrace();
                        }
                    }
                };
                imaThread.start();
                try{
                    imaThread.join();
                    foodImage.setImageBitmap(bitmap);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }//이미지 표시 완료

                //지도표시
                // 1)가게위치 표시
                // 2)본인현재위치 표시와 가게 경로 표시

                mapFragment2 mFrag2= new mapFragment2();
                mFrag2.setArguments(new Bundle());

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fTransaction = fm.beginTransaction();
                fTransaction.add(R.id.mapFrag2, mFrag2);
                fTransaction.commit();
                im=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);



     //           RecomData rData = new RecomData(str[0],str[1],str[2],str[3],str[4],str[5],str[6],str[7],str[8],
     //                   str[9],str[10],str[11],str[12],str[13],str[14],str[15],str[16],str[17],str[18],str[19],str[20],str[21]);

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
                OutputStreamWriter osw =new OutputStreamWriter(con.getOutputStream());
                sendMsg="app=user&user_id="+strings[0]+"&map_x="+strings[1]+"&map_y="+strings[2]+"&person="+strings[3];
                osw.write(sendMsg);
                osw.flush();
                osw.close();


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
