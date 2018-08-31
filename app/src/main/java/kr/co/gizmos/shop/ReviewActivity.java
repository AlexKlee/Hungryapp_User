package kr.co.gizmos.shop;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class ReviewActivity extends AppCompatActivity {
    RatingBar rating;
    EditText edtReview;
    TextView foodName;
    ImageView imgReview;
    Button btnReviewDone;

    SharedPreferences revpref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        setTitle("리뷰");

        rating=findViewById(R.id.rating);
        edtReview=findViewById(R.id.edtReview);
        btnReviewDone=findViewById(R.id.btnReviewDone);
        foodName=findViewById(R.id.reviewFood);
        imgReview=findViewById(R.id.imgReview);

        //서버에서 저장된 리뷰내용과 평점 불러오기.
        revpref=getSharedPreferences("appData",MODE_PRIVATE);

        int ratingNum=7;
        //ratingNum=revpref.getInt("ratingNum",0);
        String fName= "탕";
        //fName=revpref.getString("foodName","탕");
        String review="리뷰내용";
        //review=getString("review","리뷰내용");
        rating.setNumStars(ratingNum);
        foodName.setText(fName);
        edtReview.setText(review);



        btnReviewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //서버에 리뷰정보 전달.
            }
        });
    }
}
