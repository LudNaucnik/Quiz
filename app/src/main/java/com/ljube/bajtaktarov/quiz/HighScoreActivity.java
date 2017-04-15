package com.ljube.bajtaktarov.quiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HighScoreActivity extends AppCompatActivity {

    ListView HighScoreList;
    List<HighscoreData> highscoreArraylist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);
        setTitle("High Scores");
        centerTitle();
        HighScoreList = (ListView) findViewById(R.id.HighScoresList);
    }

    void readHighScore() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("HighScores");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
                for (HighscoreData data : highscoreArraylist) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Intent intent = new Intent();
                setResult(MainActivity.CancelReqCode, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            this.moveTaskToBack(true);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(MainActivity.CancelReqCode);
        }
        return super.onKeyDown(keyCode, event);
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
