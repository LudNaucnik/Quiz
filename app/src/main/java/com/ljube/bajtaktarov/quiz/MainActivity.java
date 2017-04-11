package com.ljube.bajtaktarov.quiz;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class MainActivity extends AppCompatActivity {

    public static final int CancelReqCode = 8906, FCReqCode = 8907, CapCityReqCode = 8908, PickPhotoReqCode = 8909;
    Button FCButton, CapitalCityButton, NewGameButton;
    TextView PointsTextView, imgidText, QuestionText, AnswerText;
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
        ButterKnife.bind(this);
        centerTitle();
        setTitle("Quiz");
    }


    View.OnClickListener newGameMain = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Points = 0;
            PointsTextView.setText("Points " + String.valueOf(Points));
        }
    };

    @OnClick(R.id.AddButton)
    public void AddData() {
        CreateChooseCategoryAlert();
    }


    void CreateChooseCategoryAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose Category")
                .setItems(R.array.categories, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            CreateAddDataAlert();
                        }
                        if (which == 1) {
                            CreateAddDataAlert();
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    void CreateAddDataAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View myView = inflater.inflate(R.layout.add_data_alert, null);
        QuestionText  = (TextView) myView.findViewById(R.id.question);
        AnswerText = (TextView) myView.findViewById(R.id.answer);
        imgidText = (TextView) myView.findViewById(R.id.imgid);
        Button choosePictire = (Button) myView.findViewById(R.id.choosePictureButton);
        choosePictire.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PickPhotoReqCode);
            }
        });
        builder.setView(myView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String answerString = AnswerText.getText().toString();
                        String questionString = QuestionText.getText().toString();
                        String imgidString = imgidText.getText().toString();
                        Toast.makeText(MainActivity.this,answerString+questionString+imgidString,Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create();
        builder.show();
    }

    View.OnClickListener FCButtonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, CreateQuizActivity.class);
            intent.putExtra("DataType", "fcdata");
            intent.putExtra("QuestionStructure", "Which FC is from ");
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
            intent.putExtra("QuestionStructure", "Which country has capital city ");
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
        if (requestCode == PickPhotoReqCode) {
            Uri selectedImageUri = data.getData();
            imgidText.setText(selectedImageUri.getPath());
        }
    }

    private void centerTitle() {
        ArrayList<View> textViews = new ArrayList<>();
        getWindow().getDecorView().findViewsWithText(textViews, getTitle(), View.FIND_VIEWS_WITH_TEXT);
        if (textViews.size() > 0) {
            AppCompatTextView appCompatTextView = null;
            if (textViews.size() == 1) {
                appCompatTextView = (AppCompatTextView) textViews.get(0);
            } else {
                for (View v : textViews) {
                    if (v.getParent() instanceof Toolbar) {
                        appCompatTextView = (AppCompatTextView) v;
                        break;
                    }
                }
            }
            if (appCompatTextView != null) {
                ViewGroup.LayoutParams params = appCompatTextView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                appCompatTextView.setLayoutParams(params);
                appCompatTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
    }
}
