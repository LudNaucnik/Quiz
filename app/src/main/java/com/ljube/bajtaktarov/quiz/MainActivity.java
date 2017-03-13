package com.ljube.bajtaktarov.quiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int CancelReqCode = 8906, FCReqCode = 8907, CapCityReqCode = 8908;
    Button FCButton, CapitalCityButton, NewGameButton;
    TextView PointsTextView;
    int Points = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FCButton = (Button) findViewById(R.id.fcButton);
        CapitalCityButton = (Button) findViewById(R.id.capitalCityButton);
        NewGameButton = (Button) findViewById(R.id.newGameMainButton);
        PointsTextView = (TextView) findViewById(R.id.pointsMainText);
        PointsTextView.setText("Points " + String.valueOf(Points));
        FCButton.setOnClickListener(FCButtonOnClick);
        NewGameButton.setOnClickListener(newGameMain);
        CapitalCityButton.setOnClickListener(capitalCityOnClick);
    }

    View.OnClickListener newGameMain = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Points = 0;
            PointsTextView.setText("Points " + String.valueOf(Points));
        }
    };

    View.OnClickListener FCButtonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, CreateQuizActivity.class);
            intent.putExtra("DataType", "fcdata");
            intent.putExtra("QuestionStructure","Which FC is from ");
            intent.putExtra("Title", "Football Clubs");
            intent.putExtra("ReqCode", FCReqCode);
            startActivityForResult(intent, FCReqCode);
        }
    };

    View.OnClickListener capitalCityOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, CreateQuizActivity.class);
            intent.putExtra("DataType", "capitalcitydata");
            intent.putExtra("QuestionStructure","Which country has capital city ");
            intent.putExtra("Title", "Capital Cities");
            intent.putExtra("ReqCode", CapCityReqCode);
            startActivityForResult(intent, CapCityReqCode);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FCReqCode) {
            Points += data.getIntExtra("Points", 0);
            PointsTextView.setText("Points " + String.valueOf(Points));
        }
        if (requestCode == CapCityReqCode) {
            Points += data.getIntExtra("Points", 0);
            PointsTextView.setText("Points " + String.valueOf(Points));
        }
        if (requestCode == CancelReqCode) {
            PointsTextView.setText("Points " + String.valueOf(Points));
        }
    }
}
