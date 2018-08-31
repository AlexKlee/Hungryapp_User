package kr.co.gizmos.shop;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


public class mapFragment2 extends Fragment {
    //메인화면 본인 현재위치 확인용으로 설정됨.
    private NMapContext mMapContext;
    private static final String CLIENT_ID = "u6M6OeXr7S0v4vBG587W";// 애플리케이션 클라이언트 아이디 값
    //추가된내용

    private NGeoPoint nGepoint, nGepointshop;//지도상 경,위도 좌표 나타내는 클래스
    private NMapView nMapView;//지도 데이터 화면표시
    private NMapController mapController;//지도상태 변경, 컨트롤 위한 클래스
    private NMapOverlayManager mapOverlayManager;
    private NMapViewerResourceProvider nMapViewerResProvider;
    private NMapLocationManager nLocManager;
    SharedPreferences fragPref;
    private NMapPOIdata poiData;
    double longit, latit;//사용자 현재위치
    double shoplongit, shoplatit;//가게위치
    double dist;//거리
    String id;
    ScrollView scroll1;

    String activity_name;

    String longt, latt, shoplong, shoplat;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mapfragment, container, false);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMapContext=new NMapContext(super.getActivity());
        mMapContext.onCreate();
        fragPref=this.getActivity().getSharedPreferences("appData", MODE_PRIVATE);
        longt=fragPref.getString("longt","0");
        latt=fragPref.getString("latt","0");
        activity_name=fragPref.getString("activity_name","");
        shoplong=fragPref.getString("shop_map_x","0");
        shoplat=fragPref.getString("shop_map_y","0");
        if(activity_name.equals("RandomRecommend")){
            scroll1=getActivity().findViewById(R.id.scroll1);
        }



    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nMapView=getView().findViewById(R.id.mapView);

        nLocManager=new NMapLocationManager(getActivity());

        nMapView.setClientId(CLIENT_ID);
        mMapContext.setupMapView(nMapView);


        longit=Double.parseDouble(longt);
        latit = Double.parseDouble(latt);
        shoplongit=Double.parseDouble(shoplong);
        shoplatit=Double.parseDouble(shoplat);

        nGepoint=new NGeoPoint(longit,latit);
        nGepointshop=new NGeoPoint(shoplongit,shoplatit);
        nMapView.setClickable(true);
        nMapView.setEnabled(true);
        //nMapView.setBuiltInZoomControls(true,null);//화면 줌기능(버튼추가식)
       // nMapView.setFocusable(true);
        nMapView.setFocusableInTouchMode(true);
     //   nMapView.requestFocus();
        //맵 터치이동시 액티비티내 스크롤 뷰작동 금지위한 리스너 연결
        nMapView.setOnMapStateChangeListener(changeListener);
        nMapView.setOnMapViewTouchEventListener(touchListener);
        nLocManager.setOnLocationChangeListener(LocChangeListener);

       // nMapView.setBuiltInAppControl(true);
       // nMapView.executeNaverMap();

        nMapViewerResProvider = new NMapViewerResourceProvider(getActivity());

        mapOverlayManager=new NMapOverlayManager(getActivity(),nMapView,nMapViewerResProvider);
    }
    @Override
    public void onStart(){
        super.onStart();
        mMapContext.onStart();

        mapController=nMapView.getMapController();
        //mapController.setMapCenter(nGepoint,13);
        mapController.setMapCenter(nGepointshop,11);
        mapController.setZoomEnabled(true);//줌허용
        setMarker();


    }
    @Override
    public void onResume() {
        super.onResume();
        mMapContext.onResume();
        nLocManager.enableMyLocation(true);
        setMarker();
    }
    @Override
    public void onPause() {
        super.onPause();
        mMapContext.onPause();



    }
    @Override
    public void onStop() {//종료시 최종 설정된 현재위치값 전달
        mMapContext.onStop();
        super.onStop();
        //변경된 현재위치 값 sharedpreference에 저장
        /*String longitude=String.valueOf(longit);
        String latitude=String.valueOf(latit);

        fragPref=this.getActivity().getSharedPreferences("appData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = fragPref.edit();
        //editor.clear();//기존 값 삭제?
        editor.putString("longt",longitude);
        editor.putString("latt",latitude);
        editor.commit();*/

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    @Override
    public void onDestroy() {
        mMapContext.onDestroy();
        super.onDestroy();
    }

    private void setMarker(){//마커표시

        mapOverlayManager.clearOverlays();
        poiData= new NMapPOIdata(2,nMapViewerResProvider);
        poiData.beginPOIdata(2);

        poiData.addPOIitem(longit,latit,String.valueOf(longit+", "+latit),NMapPOIflagType.FROM,0);//마커 클릭시 좌표값 표시. 후에 삭제
        poiData.addPOIitem(shoplongit,shoplatit,String.valueOf(shoplongit+", "+shoplatit),NMapPOIflagType.TO,0);
        poiData.endPOIdata();
        mapOverlayManager.createPOIdataOverlay(poiData,null);

    }

    //현재위치 조사 위한 인터페이스.
    private NMapLocationManager.OnLocationChangeListener LocChangeListener = new NMapLocationManager.OnLocationChangeListener() {
        ArrayList<Double> arrDist = new ArrayList<>();//거리값 저장 arraylist



        @Override
        public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {
   //         fragPref=getActivity().getSharedPreferences("appData",MODE_PRIVATE);
     //       final SharedPreferences.Editor editor=fragPref.edit();

            nGepoint=nGeoPoint;
            longit=nGeoPoint.getLongitude();
            latit=nGeoPoint.getLatitude();

            setMarker();
            dist= NGeoPoint.getDistance(nGepoint, nGepointshop);//거리값을 미터로 반환한다...
//            Toast.makeText(getActivity(),dist+"m",Toast.LENGTH_SHORT).show();

            //mapReservation activity에서만 발생하도록.

            /*if(activity_name.equals("MapReservation")){
                arrDist.add(dist);
                if(arrDist.size()>1){
                  //  for(int i=0; i<arrDist.size();i++){
                        int formerDist = arrDist.size()-2;
                        int afterDist = arrDist.size()-1;
                        if(formerDist<0){
                            formerDist=0;
                        }*//*else if(afterDist>arrDist.size()){
                        afterDist=arrDist.size();
                    }*//*String distance = String.valueOf(arrDist.get(afterDist));

                        if(arrDist.get(formerDist)+100<arrDist.get(afterDist)){//처음 dist값보다 후에 dist값이 100미터 이상 커질경우
                            //거리가 멀어졌다는 알림 발생.
                            AlertDialog.Builder getAwayDlg = new AlertDialog.Builder(getActivity());
                            getAwayDlg.setTitle("멀어지고 있습니다.");
                            getAwayDlg.setMessage("예약을 취소하시겠습니까?");
                            getAwayDlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent backtoMain = new Intent(getActivity(), Main2Activity.class);
                                    backtoMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//모든 액티비티 종료, 새로 액티비티발생
                                    startActivity(backtoMain);
                                    editor.putString("Distance","취소");
                                }
                            });
                            getAwayDlg.setNegativeButton("취소",null);
                            getAwayDlg.show();

                            //예약취소?
                            Toast.makeText(getActivity(), "멀어져간다.",Toast.LENGTH_SHORT).show();

                        }
                        else if(arrDist.get(afterDist)-arrDist.get(formerDist)<50 && arrDist.get(afterDist)>50){//나중거리-이전거리 50이하일경우, 가까워진다는 의미.
                            //접근중.. 굳이? 뺄까?
                            editor.putString("Distance", "접근 중");
                            Toast.makeText(getActivity(), "접근중",Toast.LENGTH_SHORT).show();
                        }
                        else if(arrDist.get(afterDist)<200&&arrDist.get(afterDist)>190){
                            Toast.makeText(getActivity(),"200m", Toast.LENGTH_SHORT).show();
                            meterTask meterTask2=new meterTask();
                            meterTask2.execute(id,distance);//거리값 서버전달
                            editor.putString("Distance", "200");
                        }
                        else if(arrDist.get(afterDist)<100&&arrDist.get(afterDist)>95){
                            Toast.makeText(getActivity(),"100m", Toast.LENGTH_SHORT).show();
  //                          meterTask.execute(id,distance);
                            editor.putString("Distance","100");
                        }
                        else if(arrDist.get(afterDist)<50){//마지막 거리가 50이하일경우
                            Toast.makeText(getActivity(),"곧 도착", Toast.LENGTH_SHORT).show();
                            editor.putString("Distance","곧 도착");

                            AlertDialog.Builder arriveDlg = new AlertDialog.Builder(getActivity());
                            arriveDlg.setTitle("도착");
                            arriveDlg.setMessage("도착하셨습니까?");
                            arriveDlg.setPositiveButton("도착완료", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),"Bon Appetit~ \n 식사 맛있게 하세요~!", Toast.LENGTH_SHORT).show();
                                    //리뷰화면? 홈화면?
                                    //notification 내용 변경
                                }
                            });
                            arriveDlg.setNegativeButton("가는 중",  null);
                            arriveDlg.show();
                            //메시지 출력식사 맛있게하세요? 리뷰도 작성해주세요??
                            //리뷰정보 출력?
                            meterTask meterTask3=new meterTask();
                            meterTask3.execute(id,distance);
                        }
                    editor.commit();
                    //}
                    Toast.makeText(getActivity(),dist+"m",Toast.LENGTH_SHORT).show();
                }

            }*/


            return true;//true로 해야 현재위치 계속 탐색

        }

        @Override
        public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {

        }

        @Override
        public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {

        }
    };


    private NMapView.OnMapStateChangeListener changeListener = new NMapView.OnMapStateChangeListener() {
        @Override
        public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {

        }

        @Override
        public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {


        }

        @Override
        public void onMapCenterChangeFine(NMapView nMapView) {

        }

        @Override
        public void onZoomLevelChange(NMapView nMapView, int i) {

        }

        @Override
        public void onAnimationStateChange(NMapView nMapView, int i, int i1) {

        }
    };//


    private NMapView.OnMapViewTouchEventListener touchListener = new NMapView.OnMapViewTouchEventListener() {
        @Override
        public void onLongPress(NMapView nMapView, MotionEvent motionEvent) {
            /*nMapView.setFocusable(true);
            nMapView.setFocusableInTouchMode(true);
            nMapView.requestFocus();
            scroll1.requestDisallowInterceptTouchEvent(true);*/
        }

        @Override
        public void onLongPressCanceled(NMapView nMapView) {

        }

        @Override
        public void onTouchDown(NMapView nMapView, MotionEvent motionEvent) {
            if(activity_name.equals("RandomRecommend")){
                scroll1.requestDisallowInterceptTouchEvent(true);
            }

        }

        @Override
        public void onTouchUp(NMapView nMapView, MotionEvent motionEvent) {

        }

        @Override
        public void onScroll(NMapView nMapView, MotionEvent motionEvent, MotionEvent motionEvent1) {
            nMapView.setFocusable(true);
            nMapView.setFocusableInTouchMode(true);
            nMapView.requestFocus();

            if(activity_name.equals("RandomRecommend")){
                scroll1.requestDisallowInterceptTouchEvent(true);
            }
        }

        @Override
        public void onSingleTapUp(NMapView nMapView, MotionEvent motionEvent) {

        }
    };

    //서버에 사용자와 가게간 거리 전달 , MapReservation에서만 실행할 것.
    private class meterTask extends AsyncTask<String, Void, String> {
        String address, sendMsg, receiveMsg="";
        ProgressDialog dlg = new ProgressDialog(getActivity());
       // JSONArray items;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            address="http://00645.net/eat//??";
            dlg.setMessage("접속 중");
            dlg.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dlg.dismiss();
            if(receiveMsg.equals("성공")){
                Toast.makeText(getActivity(), "거리전달 성공", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(getActivity(), "거리값전달 실패", Toast.LENGTH_SHORT).show();
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
                if(jobj.has("list_item")) {
  /*                  //result = jobj.getJSONObject("list_item");
                    receiveMsg = "성공";
                    total = jobj.getInt("list_item");//?변수 설정필요
                    if (total >= 1) {
                        items = jobj.getJSONArray("menu");
                    }*/
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



//float metersToPixels(NGeoPoint center, float meters)	전달된 좌표를 중심으로 실제 거리(met