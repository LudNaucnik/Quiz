package com.ljube.bajtaktarov.quiz;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.*;
import com.google.firebase.messaging.*;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.*;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final int CancelReqCode = 8906, FCReqCode = 8907, CapCityReqCode = 8908, PickPhotoReqCode = 8909, MoviesDirectorReqCode = 8910, MountainsReqCode = 8911, HighScoreReqCode = 8912;
    Button FCButton, CapitalCityButton, NewGameButton, MoviesDirectorButton, MountainsButton, HighscoreButton;
    TextView PointsTextView, imgidText, QuestionText, AnswerText, ImageURL;
    int Points = 0;
    MediaPlayer mp;
    private StorageReference mStorageRef;
    public String DownloadURL;
    public final static String authKey = "AAAAxEuHZ4w:APA91bH_WglBiV-0qKZZMaGFqTPgovRVYWASeolQyrq4_zsINMuYnWawnboode8rt_CRxgpJgVYj3Kc-4AbWSsH3qiEnL2hWzOi-u8QvMbZq_cViVoozNrnBnJT00NAfJ0h3YBdHWwji";
    public final static String FCMurl = "https://fcm.googleapis.com/fcm/send";
    List<HighscoreData> highscoreArraylist = new ArrayList<>();
    RequestQueue queue;
    int highPointsFromFirebase;

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
        MoviesDirectorButton = (Button) findViewById(R.id.moviesDirector);
        MountainsButton = (Button) findViewById(R.id.MountainButton);
        HighscoreButton = (Button) findViewById(R.id.HighScoreButton);
        FirebaseMessaging.getInstance().subscribeToTopic("HighScore");
        DownloadURL = "";
        ButterKnife.bind(this);
        readHighScore();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        centerTitle();
        setTitle("Quiz");
        if (isOnline() == false) {
            CreateOfflineAlert();
        } else {
            PlayNewgamesound();
        }
    }

    void PlayNewgamesound() {
        mp = MediaPlayer.create(this, R.raw.ready);
        mp.start();
    }

    View.OnClickListener newGameMain = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isOnline() == false) {
                CreateOfflineAlert();
            } else {
                FCButton.setEnabled(true);
                CapitalCityButton.setEnabled(true);
                MoviesDirectorButton.setEnabled(true);
                MountainsButton.setEnabled(true);
                Points = 0;
                PointsTextView.setText("Points " + String.valueOf(Points));
                PlayNewgamesound();
            }
        }
    };

    @OnClick(R.id.HighScoreButton)
    public void HighScoreButtonClick() {
        if (isOnline() == false) {
            CreateOfflineAlert();
        } else {
            Intent intent = new Intent(MainActivity.this, HighScoreActivity.class);
            intent.putExtra("ReqCode", HighScoreReqCode);
            startActivityForResult(intent, HighScoreReqCode);
        }
    }

    @OnClick(R.id.AddButton)
    public void AddData() {
        if (isOnline() == false) {
            CreateOfflineAlert();
        } else {
            CreateChooseCategoryAlert();
//            sendNotification();
        }
    }

    public void sendNotification(String notificationTitle, String notificationBody) {
        try {
            queue = Volley.newRequestQueue(MainActivity.this);
            JsonObject notificationData = new JsonObject();
            notificationData.addProperty("body", notificationBody);
            notificationData.addProperty("title", notificationTitle);
            notificationData.addProperty("sound", "on");
            notificationData.addProperty("priority", "high");
            JsonObject params = new JsonObject();
            params.add("notification", notificationData);
            params.addProperty("to", "/topics/HighScore");
            JsonObjectRequest req = new JsonObjectRequest(FCMurl, new JSONObject(params.toString()),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("Response", response.toString(4));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Error: ", error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    params.put("authorization", "key=" + authKey);
                    return params;
                }
            };
            queue.add(req);
        } catch (Exception e) {
            Log.d("Notification Error", e.getMessage());
        }
    }

    void readHighScore() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("HighScores");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                highscoreArraylist.clear();
                HighscoreData highscore = new HighscoreData();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    highscore = child.getValue(HighscoreData.class);
                    highscoreArraylist.add(highscore);
                }
                Collections.sort(highscoreArraylist, new Comparator<HighscoreData>() {
                    public int compare(HighscoreData score1, HighscoreData score2) {
                        if (Integer.parseInt(score1.Points) < Integer.parseInt(score2.Points)) {
                            return +1;
                        } else if (Integer.parseInt(score1.Points) > Integer.parseInt(score2.Points)) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                highPointsFromFirebase = Integer.parseInt(highscoreArraylist.get(0).Points);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Intent intent = new Intent();
                setResult(MainActivity.CancelReqCode, intent);
                finish();
            }
        });
    }

    @OnClick(R.id.moviesDirector)
    public void MoviesDIrectorClick() {
        if (isOnline() == false) {
            CreateOfflineAlert();
        } else {
            Intent intent = new Intent(MainActivity.this, CreateQuizActivity.class);
            intent.putExtra("DataType", "moviesdirectorsdata");
            intent.putExtra("QuestionStructure", "Which movie is directed by ");
            intent.putExtra("Title", "Movies Directors");
            intent.putExtra("ReqCode", MoviesDirectorReqCode);
            startActivityForResult(intent, MoviesDirectorReqCode);
        }
    }

    @OnClick(R.id.MountainButton)
    public void MountainButtonClick() {
        if (isOnline() == false) {
            CreateOfflineAlert();
        } else {
            Intent intent = new Intent(MainActivity.this, CreateQuizActivity.class);
            intent.putExtra("DataType", "mountainsdata");
            intent.putExtra("QuestionStructure", "From which country region is mountain ");
            intent.putExtra("Title", "Mountains");
            intent.putExtra("ReqCode", MountainsReqCode);
            startActivityForResult(intent, MountainsReqCode);
        }
    }

    void CheckHighScoreForSubmit() {
        if (FCButton.isEnabled() == false && CapitalCityButton.isEnabled() == false && MountainsButton.isEnabled() == false && MoviesDirectorButton.isEnabled() == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Submit your high score");
            final EditText input = new EditText(this);
            input.setHint("Name");
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            builder.setView(input);
            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = input.getText().toString();
                    if (name.length() != 0) {
                        HighscoreData highscore = new HighscoreData();
                        highscore.Name = name;
                        highscore.Points = String.valueOf(Points);
                        if (Points > highPointsFromFirebase) {
                            sendNotification(String.valueOf(highscore.Points) + " points" + " by " + highscore.Name, "Click here to try to beat it.");
                        }
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("HighScores");
                        myRef.push().setValue(highscore);
                        Toast.makeText(MainActivity.this, "High Scores Submitted", Toast.LENGTH_LONG).show();
                        NewGameButton.callOnClick();
                    } else {
                        Toast.makeText(MainActivity.this, "Please fill a name", Toast.LENGTH_LONG).show();
                        CheckHighScoreForSubmit();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    NewGameButton.callOnClick();
                }
            });
            builder.show();
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
                        if (which == 2) {
                            CreateAddDataAlert("Movie Directors");
                        }
                        if (which == 3) {
                            CreateAddDataAlert("Mountains");
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    void CreateAddDataAlert(final String Category) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View myView = inflater.inflate(R.layout.add_data_alert, null);
        QuestionText = (TextView) myView.findViewById(R.id.question);
        AnswerText = (TextView) myView.findViewById(R.id.answer);
        imgidText = (TextView) myView.findViewById(R.id.imgid);
//        ImageURL = (TextView) myView.findViewById(R.id.imgurlText);
        Button choosePictire = (Button) myView.findViewById(R.id.choosePictureButton);
        choosePictire.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PickPhotoReqCode);
            }
        });
        builder.setView(myView)
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog d1 = builder.create();
        d1.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b1 = d1.getButton(AlertDialog.BUTTON_POSITIVE);
                b1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isOnline() == false) {
                            CreateOfflineAlert();
                        } else {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("SubmitQuestion");
                            SubmitQuestionData question = new SubmitQuestionData();
                            question.Answer = AnswerText.getText().toString();
                            question.Question = QuestionText.getText().toString();
                            question.imgid = DownloadURL;
                            question.QuestionCategory = Category;
                            if (question.Answer.length() == 0 || question.Question.length() == 0) {
                                Toast.makeText(MainActivity.this, "Fill all the field before submitting questions.", Toast.LENGTH_LONG).show();
                            } else if (question.imgid.length() == 0) {
                                Toast.makeText(MainActivity.this, "Upload a picture or wait for the upload to complete", Toast.LENGTH_LONG).show();
                            } else {
                                myRef.push().setValue(question);
                                Toast.makeText(MainActivity.this, "Question is submitted", Toast.LENGTH_LONG).show();
                                d1.dismiss();
                            }
                        }
                    }
                });
            }
        });
        d1.show();
    }


    void CreateOfflineAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connection Error");
        builder.setMessage("Looks like your device is not connected to internet. Please connect to internet and try again.")
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
        if (requestCode == MoviesDirectorReqCode) {
            Points += data.getIntExtra("Points", 0);
            PointsTextView.setText("Points " + String.valueOf(Points));
            MoviesDirectorButton.setEnabled(false);
        }
        if (requestCode == MountainsReqCode) {
            Points += data.getIntExtra("Points", 0);
            PointsTextView.setText("Points " + String.valueOf(Points));
            MountainsButton.setEnabled(false);
        }
        if (requestCode == HighScoreReqCode) {
        }
        if (requestCode == PickPhotoReqCode) {
            try {
                Uri selectedImageUri = data.getData();
                final File f = new File(selectedImageUri.getPath());
                StorageReference riversRef = mStorageRef.child("submittedpictures/" + f.getName());
                riversRef.putFile(selectedImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                @SuppressWarnings("VisibleForTests") Uri download = taskSnapshot.getDownloadUrl();
                                DownloadURL = download.toString();
                                imgidText.setText(f.getName());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Please select a valid picture", Toast.LENGTH_LONG).show();
            }
        }
        CheckHighScoreForSubmit();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            this.moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
