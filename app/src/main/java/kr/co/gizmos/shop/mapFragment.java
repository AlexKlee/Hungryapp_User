package kr.co.gizmos.shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nMapView=getView().findViewById(R.id.mapView);
        nMapView.setClientId(CLIENT_ID);
        mMapContext.setupMapView(nMapView);

        fragPref=this.getActivity().getSharedPreferences("userPos", Context.MODE_PRIVATE);
        String longt=fragPref.getString("longt","0");
        String latt=fragPref.getString("latt","0");
        longit=Double.parseDouble(longt);
        latit = Double.parseDouble(latt);
        nGepoint=new NGeoPoint(longit,latit);
        nMapView.setClickable(true);
        nMapView.setEnabled(true);
        nMapView.setFocusable(true);
        nMapView.requestFocus();
    }
    @Override
    public void onStart(){
        super.onStart();
        mMapContext.onStart();

        mapController=nMapView.getMapController();
        mapController.setMapCenter(nGepoint,13);
        setMarker();


    }
    @Override
    public void onResume() {
        super.onResume();
        mMapContext.onResume();
        nMapView.setOnMapStateChangeListener(changeListener);
    }
    @Override
    public void onPause() {
        super.onPause();
        mMapContext.onPause();
    }
    @Override
    public void onStop() {
        mMapContext.onStop();
        super.onStop();
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

        NMapPOIdata poiData = new NMapPOIdata(1,nMapViewerResProvider);
        poiData.beginPOIdata(1);
        poiData.addPOIitem(longit,latit,String.valueOf(longit+", "+latit),NMapPOIflagType.FROM,0);
        poiData.endPOIdata();
        mapOverlayManager.createPOIdataOverlay(poiData,null);
    }
    private NMapView.OnMapStateChangeListener changeListener = new NMapView.OnMapStateChangeListener() {
        @Override
        public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {

        }

        @Override
        public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {
            longit=nGeoPoint.getLongitude();
            latit=nGeoPoint.getLatitude();
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
