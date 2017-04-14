package com.ljube.bajtaktarov.quiz;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    TextView PointsTextView, imgidText, QuestionText, AnswerText, ImageURL;
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
        if (isOnline() == false) {
            CreateOfflineAlert();
        }
    }


    View.OnClickListener newGameMain = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isOnline() == false) {
                CreateOfflineAlert();
            } else {
                FCButton.setEnabled(true);
                CapitalCityButton.setEnabled(true);
                Points = 0;
                PointsTextView.setText("Points " + String.valueOf(Points));
            }
        }
    };

    @OnClick(R.id.AddButton)
    public void AddData() {
        if (isOnline() == false) {
            CreateOfflineAlert();
        } else {
            CreateChooseCategoryAlert();
        }
    }


    void CreateChooseCategoryAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose Category")
                .setItems(R.array.categories, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            CreateAddDataAlert("Football Club");
                        }
                        if (which == 1) {
                            CreateAddDataAlert("Capital City");
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    void CreateAddDataAlert(final String Category) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View myView = inflater.inflate(R.layout.add_data_alert, null);
        QuestionText = (TextView) myView.findViewById(R.id.question);
        AnswerText = (TextView) myView.findViewById(R.id.answer);
//        imgidText = (TextView) myView.findViewById(R.id.imgid);
        ImageURL = (TextView) myView.findViewById(R.id.imgurlText);
//        Button choosePictire = (Button) myView.findViewById(R.id.choosePictureButton);
//        choosePictire.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                startActivityForResult(intent, PickPhotoReqCode);
//            }
//        });
        builder.setView(myView)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (isOnline() == false) {
                            CreateOfflineAlert();
                        } else {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("SubmitQuestion");
                            SubmitQuestionData question = new SubmitQuestionData();
                            question.Answer = AnswerText.getText().toString();
                            question.Question = QuestionText.getText().toString();
                            question.imgid = ImageURL.getText().toString();
                            question.QuestionCategory = Category;
                            if (question.Answer.length() == 0 || question.Question.length() == 0 || question.imgid.length() == 0) {
                                Toast.makeText(MainActivity.this, "Fill all the field before submitting questions.", Toast.LENGTH_LONG).show();
                            } else {
                                myRef.push().setValue(question);
                                Toast.makeText(MainActivity.this, "Question is submitted", Toast.LENGTH_LONG).show();
                            }
                        }
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

    void CreateOfflineAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connection Error");
        builder.setMessage("Looks like your device is not connected to internet.\nPlease connect to internet and try again.")
                .setCancelable(false)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isOnline() == false) {
                            CreateOfflineAlert();
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    View.OnClickListener FCButtonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isOnline() == false) {
                CreateOfflineAlert();
            } else {
                Intent intent = new Intent(MainActivity.this, CreateQuizActivity.class);
                intent.putExtra("DataType", "fcdata");
                intent.putExtra("QuestionStructure", "Which FC is from ");
                intent.putExtra("Title", "Football Clubs");
                intent.putExtra("ReqCode", FCReqCode);
                startActivityForResult(intent, FCReqCode);
            }
        }
    };

    View.OnClickListener capitalCityOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isOnline() == false) {
                CreateOfflineAlert();
            } else {
                Intent intent = new Intent(MainActivity.this, CreateQuizActivity.class);
                intent.putExtra("DataType", "capitalcitydata");
                intent.putExtra("QuestionStructure", "Which country has capital city ");
                intent.putExtra("Title", "Capital Cities");
                intent.putExtra("ReqCode", CapCityReqCode);
                startActivityForResult(intent, CapCityReqCode);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FCReqCode) {
            Points += data.getIntExtra("Points", 0);
            PointsTextView.setText("Points " + String.valueOf(Points));
            FCButton.setEnabled(false);
        }
        if (requestCode == CapCityReqCode) {
            Points += data.getIntExtra("Points", 0);
            PointsTextView.setText("Points " + String.valueOf(Points));
            CapitalCityButton.setEnabled(false);
        }
        if (requestCode == CancelReqCode) {
            Points += data.getIntExtra("Points", 0);
            PointsTextView.setText("Points " + String.valueOf(Points));
        }
        if (requestCode == PickPhotoReqCode) {
//            Uri selectedImageUri = data.getData();
//            imgidText.setText(selectedImageUri.getPath());
//            Uri file = Uri.fromFile(new File(selectedImageUri.getPath()));
//            StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());
//            uploadTask = riversRef.putFile(file);
//
//// Register observers to listen for when the download is done or if it fails
//            uploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    // Handle unsuccessful uploads
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                }
//            });
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
