package kr.co.gizmos.shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

public class ReviewAll extends AppCompatActivity {
    ArrayList<ReviewData> arrRvData= new ArrayList<>();
    ListView reviewList;

    TextView txRvMenuName, txRvReview;// 음식명, 별점, 후기
    RatingBar rbRvListRating;
    MyAdapter rvadap;
    String menuID,ratingNum,userReview,shopReview;
    String menuName;
    boolean save=false;
    SharedPreferences rapref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_all);
        setTitle("리뷰목록");
        rapref=getSharedPreferences("appData",MODE_PRIVATE);
        menuID=rapref.getString("menu_id","");
        reviewTask rtask = new reviewTask();
        rtask.execute(menuID);
        reviewList=findViewById(R.id.reviewList);

        rvadap=new MyAdapter(this);
        reviewList.setAdapter(rvadap);

    }//onCreate end




    public class ReviewData{
        String menuName;
        String ratingNum;
        String userReviewText, shopReviewText;
        ReviewData(String mN, String rN, String rT, String sT){
            menuName=mN;
            ratingNum=rN;
            userReviewText=rT;
            shopReviewText=sT;
        }
    }
    //리뷰통신, 받아오기, 전달하기.
    public class reviewTask extends AsyncTask<String, Void, String> {
        String address, sendMsg, receiveMsg="";
        //   ProgressDialog dlg = new ProgressDialog(getApplicationContext());
        String result=null;
        JSONArray jArray;
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
                    for(int i=0; i<jArray.length(); i++){//jsonArray에서 JSONobject로 나눠서, 필요항목들 arraylist에 추가
                        JSONObject jobj = jArray.getJSONObject(i);
                        menuID = jobj.getString("mi_idx");
                        ratingNum= jobj.getString("rv_score");
                        userReview = jobj.getString("rv_comment");
                        shopReview=jobj.getString("rv_recomment");

                        ReviewData rvd = new ReviewData(menuName,ratingNum,userReview,shopReview);
                        arrRvData.add(rvd);
                        save=false;
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
                Toast.makeText(getApplicationContext(), "리뷰불러오기실패", Toast.LENGTH_SHORT).show();
            }
        }

        //해당메뉴에대한전체 리뷰목록 불러오기.
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

                    sendMsg="app=user&mi_idx="+strings[0];//메뉴번호 전송, 메뉴관련 모든리뷰 불러오기.


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

                if(jobj.has("av_score")) {//로딩, 저장 상황 구분?!?
                    receiveMsg="성공";
                    String result = jobj.getString("review");
                    jArray=new JSONArray(result);


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



    //리스트뷰용어댑터
    class MyAdapter extends BaseAdapter {
        Context con;
        MyAdapter(Context c){
            con=c;
        }

        @Override

        public int getCount() {
            return arrRvData.size();
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
                convertView=inf.inflate(R.layout.review_list,parent,false);
            }

            txRvMenuName=convertView.findViewById(R.id.txRvMenuName);
            txRvReview=convertView.findViewById(R.id.txRvReview);
            rbRvListRating=convertView.findViewById(R.id.rbRvListRating);

            ReviewData rvd = arrRvData.get(position);

            txRvMenuName.setText(rvd.menuName);
            rbRvListRating.setNumStars(Integer.parseInt(rvd.ratingNum));
            txRvReview.setText(rvd.userReviewText);


            return convertView;
        }
    }//adapter end
}
