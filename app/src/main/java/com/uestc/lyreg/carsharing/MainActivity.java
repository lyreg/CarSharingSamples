package com.uestc.lyreg.carsharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupWindowAnimations();

        setupToolBar();
    }

    private void setupToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    private void setupWindowAnimations() {

        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.LEFT);
        slide.setDuration(500);
        getWindow().setExitTransition(slide);
//        getWindow().setReenterTransition(slide);

        Explode explode = new Explode();
        explode.setDuration(500);
        getWindow().setReenterTransition(explode);
    }

    public void onButtonEnrollCliked(View v) {
        if(v.getId() == R.id.button_enroll) {
            Intent intent = new Intent(this, EnrollActivity.class);

            startActivity(intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
//            startActivity(intent);
        }
    }

    public void onButtonOpenClicked(View v) {
        if(v.getId() == R.id.button_open) {
            Intent intent = new Intent(this, OpenActivity.class);

            startActivity(intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
        }
    }

    public void onButtonErrorClicked(View v) {
        if(v.getId() == R.id.button_error) {
            Intent intent = new Intent(this, ErrorProcessActivity.class);

            startActivity(intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
        }
    }
}
