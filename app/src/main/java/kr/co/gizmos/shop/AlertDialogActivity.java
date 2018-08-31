package kr.co.gizmos.shop;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class AlertDialogActivity extends Activity {
    TextView txdlgTitle, txdlgMessage;
    Button btndlgYes, btndlgCancel;
    SharedPreferences dlgpref;
    String type="";
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

        if(type.equals("예약취소")){//예약취소확인 다이얼로그
            txdlgTitle.setText("멀어지고 있습니다.");
            txdlgMessage.setText("예약을 취소하시겠습니까?");
        }
        else if(type.equals("도착완료")){
            txdlgTitle.setText("근처입니다.");
            txdlgMessage.setText("도착하셨습니까?");
        }


        //확인 버튼//예약취소버튼. or 도착확인 버튼
        btndlgYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type.equals("예약취소")){//확인 누를 시 예약취소
                    SharedPreferences.Editor editor = dlgpref.edit();
                    editor.putString("dlg", "service quit");
                    editor.commit();
                    Intent itMain2= new Intent(AlertDialogActivity.this, RandomRecommend.class);
                    startActivity(itMain2);
              //      RandomRecommend rr = new RandomRecommend();
            //        rr.stopService(new Intent(getApplicationContext(), MyService.class));
                    MyService ms=new MyService();
                    ms.onDestroy();

                    finish();
                }else{//확인 누름시 도착완료
                    //리뷰페이지로 이동
                    SharedPreferences.Editor editor = dlgpref.edit();
                    editor.putString("dlg", "service quit");
                    editor.commit();
                    Intent itMain2= new Intent(AlertDialogActivity.this, RandomRecommend.class);
                    startActivity(itMain2);
                    MyService ms=new MyService();
                    ms.onDestroy();

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
}
