package com.ljube.bajtaktarov.quiz;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class CreateQuizActivity extends AppCompatActivity {

    Intent CalledFrom;
    String QuestionType, QuestionStructure;
    ListView list;
    MediaPlayer mp = new MediaPlayer();
    int CorrectAnswer, Points = 0, numOfQuestions = 0, numQuestion = 1;
    List<QuestionData> QuestionList = new ArrayList<>();
    List<QuestionData> RandomQuestions = new ArrayList<>();
    TextView QuestionTextView, PointsTextView, TimerTextView;
    Toolbar actionBarToolbar;
    RelativeLayout MainLayout;
    String[] Answers = new String[4];
    String[] AnswersImgID = new String[4];
    Button newgameButton;
    List<QuestionData> passedQuestions = new ArrayList<>();
    long timerMiliSeconds = 11000;
    CountDownTimer MainTimer, SecondTimer;
    Boolean isT2Running = false;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);
        CalledFrom = getIntent();
        QuestionType = CalledFrom.getStringExtra("DataType");
        list = (ListView) findViewById(R.id.list);
        QuestionTextView = (TextView) findViewById(R.id.QuestionText);
        TimerTextView = (TextView) findViewById(R.id.TimerText);
        PointsTextView = (TextView) findViewById(R.id.PointsText);
        newgameButton = (Button) findViewById(R.id.newGameButton);
        MainLayout = (RelativeLayout) findViewById(R.id.firstlayout);
//        UploadCVStoFirebase();
        readFirebase();
        centerTitle();
        setTitle(CalledFrom.getStringExtra("Title"));
        QuestionStructure = CalledFrom.getStringExtra("QuestionStructure");
        actionBarToolbar = (Toolbar) findViewById(R.id.action_bar);
        actionBarToolbar.setTitleTextColor(Color.parseColor("#000000"));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == CorrectAnswer) {
                    Points++;
                    playSound(1);
                } else {
                    Points--;
                    playSound(0);
                }
                numQuestion++;
                PointsTextView.setText(String.valueOf(Points));
                CreateQuestion();
                StartTimers();
            }
        });
        newgameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numQuestion = 1;
                playSound(2);
                passedQuestions.clear();
                Points = 0;
                PointsTextView.setText(String.valueOf(Points));
                CreateQuestion();
                StartTimers();
            }
        });
        MainTimer = new CountDownTimer(11000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimerTextView.setText(String.valueOf(millisUntilFinished / 1000));
                timerMiliSeconds = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                Points--;
                playSound(0);
                PointsTextView.setText(String.valueOf(Points));
                CreateQuestion();
                MainTimer.start();
            }
        };
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        newgameButton.setVisibility(View.INVISIBLE);
        list.setVisibility(View.INVISIBLE);
        QuestionTextView.setVisibility(View.INVISIBLE);

    }

    void finishQuiz() {
        Intent finishIntent = new Intent();
        finishIntent.putExtra("Points", Points);
        setResult(CalledFrom.getIntExtra("ReqCode", 0), finishIntent);
        finish();
    }

    void StartTimers() {
        if (isT2Running == true) {
            isT2Running = false;
            SecondTimer.cancel();
        }
        MainTimer.start();
    }

    void ChangeBackColor() {
        int red = CreateRandomNumber(150, 255);
        int green = CreateRandomNumber(150, 255);
        int blue = CreateRandomNumber(150, 255);
        int backColor = Color.argb(255, red, green, blue);
        actionBarToolbar.setBackgroundColor(backColor);
        MainLayout.setBackgroundColor(backColor);
        newgameButton.setBackgroundColor(backColor);
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

    void playSound(int x) {
        if (mp.isPlaying() == true) {
            mp.stop();
        }
        if (x == 0) {
            mp = MediaPlayer.create(this, R.raw.buzz);
            mp.start();
        }
        if (x == 1) {
            mp = MediaPlayer.create(this, R.raw.clap);
            mp.start();
        }
        if (x == 2) {
            mp = MediaPlayer.create(this, R.raw.newgame);
            mp.start();
        }
    }

    private int CreateRandomNumber(int min, int max) {
        Random r = new Random();
        int i1 = r.nextInt(max - min + 1) + min;
        return i1;
    }

    void CreateQuestion() {
        ChangeBackColor();
        int RandomNum = CreateRandomNumber(0, 3);
        Collections.shuffle(QuestionList);
        RandomQuestions.clear();
        for (int i = 0; i < 4; i++) {
            RandomQuestions.add(QuestionList.get(i));
            Answers[i] = QuestionList.get(i).Answer;
            AnswersImgID[i] = QuestionList.get(i).imgid;
        }
        QuestionTextView.setText(String.valueOf(numQuestion) + ": " + QuestionStructure + RandomQuestions.get(RandomNum).Question + " ?");
        CorrectAnswer = RandomNum;
        if (passedQuestions.size() != numOfQuestions) {
            if (passedQuestions.contains(RandomQuestions.get(RandomNum)) == true) {
                CreateQuestion();
            } else {
                passedQuestions.add(RandomQuestions.get(RandomNum));
            }
        } else {
            finishQuiz();
            passedQuestions.clear();
        }
        CustomListAdapter adapter = new CustomListAdapter(this, Answers, AnswersImgID);
        list.setAdapter(adapter);
    }

    void UploadCVStoFirebase() {
        String Text = "";
        try {
            int resourceID = this.getResources().getIdentifier(QuestionType, "raw", this.getPackageName());
            Scanner scan = new Scanner(getResources().openRawResource(resourceID));
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                Text += line + "\n";
            }
            scan.close();
            CSVReader reader = new CSVReader(new StringReader(Text));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                QuestionData newQuestion = new QuestionData();
                newQuestion.Answer = nextLine[0];
                newQuestion.Question = nextLine[1];
                newQuestion.imgid = nextLine[2];
                QuestionList.add(newQuestion);
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(QuestionType);
            int i = 0;
            for (QuestionData q : QuestionList) {
                myRef.push().setValue(q);
            }
        } catch (Exception ex) {
            Intent intent = new Intent();
            setResult(MainActivity.CancelReqCode, intent);
            finish();
        }
    }


    void readFirebase() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(QuestionType);
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    QuestionData newQuestion = new QuestionData();
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        newQuestion = child.getValue(QuestionData.class);
                        QuestionList.add(newQuestion);
                    }
                    numOfQuestions = QuestionList.size();
                    CreateQuestion();
                    CountDownTimer TimerStart = new CountDownTimer(1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            StartTimers();
                            progressBar.setVisibility(View.INVISIBLE);
                            newgameButton.setVisibility(View.VISIBLE);
                            list.setVisibility(View.VISIBLE);
                            QuestionTextView.setVisibility(View.VISIBLE);
                            playSound(2);
                        }
                    }.start();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Intent intent = new Intent();
                    setResult(MainActivity.CancelReqCode, intent);
                    finish();
                }
            });
        } catch (Exception ex) {
            Intent intent = new Intent();
            setResult(MainActivity.CancelReqCode, intent);
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SecondTimer = new CountDownTimer(timerMiliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimerTextView.setText(String.valueOf(millisUntilFinished / 1000));
                timerMiliSeconds = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                Points--;
                playSound(0);
                PointsTextView.setText(String.valueOf(Points));
                CreateQuestion();
                MainTimer.start();
            }
        };
        if (isT2Running == true) {
            SecondTimer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mp.stop();
        isT2Running = true;
        MainTimer.cancel();
        SecondTimer.cancel();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            this.moveTaskToBack(true);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            setResult(MainActivity.CancelReqCode, intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}
