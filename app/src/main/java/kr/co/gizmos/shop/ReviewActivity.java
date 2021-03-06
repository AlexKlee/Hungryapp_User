package kr.co.gizmos.shop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {
    RatingBar rating;
    EditText edtReview;
    TextView foodName;
    ImageView imgReview;
    Button btnReviewDone;

    SharedPreferences revpref;



    String ratingNum;
    String menuName, menuID;
    String userReview, shopReview;
    String rvid;
    String userId;

    boolean save=false;//처음엔 저장이 아니라 불러오기.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        setTitle("리뷰");
        //리뷰 하나만 띄우기, 저장

        rating=findViewById(R.id.rating);
        edtReview=findViewById(R.id.edtReview);
        btnReviewDone=findViewById(R.id.btnReviewDone);
        foodName=findViewById(R.id.reviewFood);
        imgReview=findViewById(R.id.imgReview);

        //서버에서 저장된 리뷰내용과 평점 불러오기.
        revpref=getSharedPreferences("appData",MODE_PRIVATE);
        userId=revpref.getString("user_id","");
        menuName=revpref.getString("menu_name","");
        menuID=revpref.getString("menu_id","");
        rvid=revpref.getString("rv_id","");
        //fName=revpref.getString("foodName","탕");
        //review=getString("review","리뷰내용");
/*        rating.setNumStars(Integer.parseInt(ratingNum));
        foodName.setText(menuName);
        edtReview.setText(userReview);*/
        //현재 액티비티 위치 mapfragment2에 전달
        SharedPreferences.Editor editor = revpref.edit();
        editor.putString("activity_name", "ReviewActivity");
        editor.commit();

        //메뉴아이디 불러올것.
        reviewTask loadTask = new reviewTask();
        save=false;
        loadTask.execute(menuID,userId,rvid,null,null);




        btnReviewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //서버에 리뷰정보 전달.
                ratingNum=String.valueOf(rating.getRating());
                userReview=edtReview.getText().toString();
                save=true;
                reviewTask reTask = new reviewTask();
                reTask.execute(rvid,menuID,userId,ratingNum,userReview);//userid? menuid?

                Intent it = new Intent(ReviewActivity.this,ReviewAll.class);
                startActivity(it);
                //finish();
            }
        });
    }//onCreate end

    //불러오기.

    //리뷰통신, 받아오기, 전달하기.
    public class reviewTask extends AsyncTask<String, Void, String> {
        String address, sendMsg, receiveMsg="";
        //   ProgressDialog dlg = new ProgressDialog(getApplicationContext());
        JSONObject result= new JSONObject();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address="http://00645.net/eat/review_view.php";
            //      dlg.setMessage("접속 중");
            //      dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //          dlg.dismiss();
            if(receiveMsg.equals("성공")){


                try {
                        menuID = result.getString("mi_idx");
                        ratingNum= result.getString("rv_score");
                        userReview = result.getString("rv_comment");
                        shopReview=result.getString("rv_recomment");
                        rating.setNumStars(Integer.parseInt(ratingNum));
     /*                   ReviewData rvd = new ReviewData(menuName,ratingNum,userReview,shopReview);
                        arrRvData.add(rvd);*/
                        save=false;
                        if(userReview.equals("")&&ratingNum.equals("")){
                            btnReviewDone.setText("리뷰등록");
                        }else{
                            btnReviewDone.setText("리뷰수정");
                            edtReview.setText(userReview);
                        }
                } catch (JSONException e) {
                        e.printStackTrace();
                }

                //Toast.makeText(getApplicationContext(), "거리전달 성공", Toast.LENGTH_SHORT).show();
 /*               if(result.equals("wait")){//가게에서 확인 안함.
                    if(type.equals("예약취소")){
                        Toast.makeText(getApplicationContext(), "예약취소완료, 가게 확인 중",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "도착완료, 가게 확인 중",Toast.LENGTH_SHORT).show();
                    }
                }
                else if(result.equals("ok")){
                    if(type.equals("예약취소")){
                        Toast.makeText(getApplicationContext(), "예약취소완료, 가게 확인완료",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "도착완료, 가게 확인 완료",Toast.LENGTH_SHORT).show();
                    }
                }*/
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
                Toast.makeText(getApplicationContext(), "리뷰저장실패", Toast.LENGTH_SHORT).show();
            }
        }

        //처음에 리스트뷰에 모든 댓글을 불러온다.
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
                if(!save){//로딩상황(저장 상황이 아니라면)
                    sendMsg="app=user&mi_idx="+strings[0]+"&ru_idx="+strings[1]+"&rv_idx="+strings[2];//메뉴번호 전송, 메뉴관련 모든리뷰 불러오기.
                }else{//저장상황
                    //메뉴번호mi_idx, 작성자번호(id)ru_idx, 평점rv_score, 후기내용rv_comment
                    sendMsg="app=user&rv_idx="+strings[0]+"&mi_idx="+strings[1]+"&ru_idx="+strings[2]+"&rv_score="+strings[3]+"&rv_comment="+strings[4];//거리전달 추가.
                }

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

                if(jobj.has("review")) {//로딩, 저장 상황 구분?!?
                    receiveMsg="성공";
                    //String result = jobj.getString("review");
                    //jArray=new JSONArray(result);
                    //jobj.getJSONObject()
                    result=jobj.getJSONObject("review");

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
