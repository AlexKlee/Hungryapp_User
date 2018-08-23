package kr.co.gizmos.shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ScrollingView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;

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

    SharedPreferences fragPref;
    double longit, latit;//사용자 현재위치
    double shoplongit, shoplatit;//가게위치

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

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nMapView=getView().findViewById(R.id.mapView);
        nMapView.setClientId(CLIENT_ID);
        mMapContext.setupMapView(nMapView);

        fragPref=this.getActivity().getSharedPreferences("appData", Context.MODE_PRIVATE);
        String longt=fragPref.getString("longt","0");
        String latt=fragPref.getString("latt","0");

        String shoplong=fragPref.getString("shop_map_x","0");
        String shoplat=fragPref.getString("shop_map_y","0");
        longit=Double.parseDouble(longt);
        latit = Double.parseDouble(latt);
        shoplongit=Double.parseDouble(shoplong);
        shoplatit=Double.parseDouble(shoplat);

        nGepoint=new NGeoPoint(longit,latit);
        nGepointshop=new NGeoPoint(shoplongit,shoplatit);
        nMapView.setClickable(true);
        nMapView.setEnabled(true);
        //nMapView.setBuiltInZoomControls(true,null);//화면 줌기능(버튼추가식)
        nMapView.setFocusable(true);
        nMapView.setFocusableInTouchMode(true);
        nMapView.requestFocus();
        nMapView.setOnMapStateChangeListener(changeListener);

       // nMapView.setOnMapViewTouchEventListener(touchListener);

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
        mapOverlayManager=new NMapOverlayManager(getActivity(),nMapView,nMapViewerResProvider);
        mapOverlayManager.clearOverlays();
        nMapViewerResProvider = new NMapViewerResourceProvider(getActivity());

        NMapPOIdata poiData = new NMapPOIdata(2,nMapViewerResProvider);
        poiData.beginPOIdata(2);
        poiData.addPOIitem(longit,latit,String.valueOf(longit+", "+latit),NMapPOIflagType.FROM,1);//마커 클릭시 좌표값 표시. 후에 삭제
        poiData.addPOIitem(shoplongit,shoplatit,String.valueOf(shoplongit+", "+shoplatit),NMapPOIflagType.TO,2);
        poiData.endPOIdata();
        mapOverlayManager.createPOIdataOverlay(poiData,null);

    }
    private NMapView.OnMapStateChangeListener changeListener = new NMapView.OnMapStateChangeListener() {
        @Override
        public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {

        }

        @Override
        public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {
       /*     longit=nGeoPoint.getLongitude();
            latit=nGeoPoint.getLatitude();
            setMarker();*/
        /*    nMapView.setFocusable(true);
            nMapView.setFocusableInTouchMode(true);
            nMapView.requestFocus();*/
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

/*    private NMapView.OnMapViewTouchEventListener touchListener = new NMapView.OnMapViewTouchEventListener() {
        @Override
        public void onLongPress(NMapView nMapView, MotionEvent motionEvent) {
            nMapView.setFocusable(true);
            nMapView.setFocusableInTouchMode(true);
            nMapView.requestFocus();
        }

        @Override
        public void onLongPressCanceled(NMapView nMapView) {

        }

        @Override
        public void onTouchDown(NMapView nMapView, MotionEvent motionEvent) {
            nMapView.setFocusable(true);
            nMapView.setFocusableInTouchMode(true);
            nMapView.requestFocus();
        }

        @Override
        public void onTouchUp(NMapView nMapView, MotionEvent motionEvent) {

        }

        @Override
        public void onScroll(NMapView nMapView, MotionEvent motionEvent, MotionEvent motionEvent1) {
            nMapView.setFocusable(true);
            nMapView.setFocusableInTouchMode(true);
            nMapView.requestFocus();
        }

        @Override
        public void onSingleTapUp(NMapView nMapView, MotionEvent motionEvent) {

        }
    };*/
}



//float metersToPixels(NGeoPoint center, float meters)	전달된 좌표를 중심으로 실제 거리(met