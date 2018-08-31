package kr.co.gizmos.shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;

public class mapFragment extends Fragment {
    //메인화면 본인 현재위치 확인용으로 설정됨.
    private NMapContext mMapContext;
    private static final String CLIENT_ID = "u6M6OeXr7S0v4vBG587W";// 애플리케이션 클라이언트 아이디 값
    //추가된내용

    private NGeoPoint nGepoint;//지도상 경,위도 좌표 나타내는 클래스
    private NMapView nMapView;//지도 데이터 화면표시
    private NMapController mapController;//지도상태 변경, 컨트롤 위한 클래스
    private NMapOverlayManager mapOverlayManager;
    private NMapViewerResourceProvider nMapViewerResProvider;

    SharedPreferences fragPref;
    double longit, latit;

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


    //37.489471, 126.724550부평역
    //37.491063, 126.720468
    //37.491293, 126.722325
    //37.491259, 126.725651
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nMapView=getView().findViewById(R.id.mapView);
        nMapView.setClientId(CLIENT_ID);
        mMapContext.setupMapView(nMapView);

        fragPref=this.getActivity().getSharedPreferences("appData", Context.MODE_PRIVATE);
        String longt=fragPref.getString("longt","0");
        String latt=fragPref.getString("latt","0");
        longit=Double.parseDouble(longt);
        latit = Double.parseDouble(latt);
        nGepoint=new NGeoPoint(longit,latit);
        nMapView.setClickable(true);
        nMapView.setEnabled(true);
        nMapView.setFocusable(true);
        nMapView.setFocusableInTouchMode(true);
        nMapView.requestFocus();
        nMapView.setOnMapStateChangeListener(changeListener);
    }
    @Override
    public void onStart(){
        super.onStart();
        mMapContext.onStart();

        mapController=nMapView.getMapController();
        mapController.setMapCenter(nGepoint,13);
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
        String longitude=String.valueOf(longit);
        String latitude=String.valueOf(latit);

        fragPref=this.getActivity().getSharedPreferences("appData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = fragPref.edit();
        //editor.clear();//기존 값 삭제?
        editor.putString("longt",longitude);
        editor.putString("latt",latitude);
        editor.commit();//

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
        nMapViewerResProvider = new NMapViewerResourceProvider(getActivity());
        mapOverlayManager=new NMapOverlayManager(getActivity(),nMapView,nMapViewerResProvider);
        mapOverlayManager.clearOverlays();

        NMapPOIdata poiData = new NMapPOIdata(1,nMapViewerResProvider);
        poiData.beginPOIdata(1);
        poiData.addPOIitem(longit,latit,String.valueOf(longit+", "+latit),NMapPOIflagType.FROM,0);//마커 클릭시 좌표값 표시. 후에 삭제
        poiData.endPOIdata();
        mapOverlayManager.createPOIdataOverlay(poiData,null);

    }
    private NMapView.OnMapStateChangeListener changeListener = new NMapView.OnMapStateChangeListener() {
        @Override
        public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {

        }

        @Override
        public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {
            nGepoint=nGeoPoint;
            longit=nGepoint.getLongitude();
            latit=nGepoint.getLatitude();
            setMarker();
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

}



//float metersToPixels(NGeoPoint center, float meters)	전달된 좌표를 중심으로 실제 거리(meters)를 화면상의 거리(pixels)로 변환한다.