package com.ljube.bajtaktarov.quiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import javax.xml.datatype.Duration;

public class CreateQuizActivity extends AppCompatActivity {

    Intent CalledFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);
        CalledFrom = getIntent();
        Toast.makeText(this,CalledFrom.getStringExtra("DataType"), Toast.LENGTH_LONG).show();
    }
}
