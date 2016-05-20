package com.uestc.lyreg.carsharing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;

public class EnrollActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);

        setupWindowAnimations();
        setupToolBar();
    }

    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Registration");
    }

    private void setupWindowAnimations() {
        Slide slide = new Slide(Gravity.RIGHT);
        slide.setDuration(500);
        getWindow().setEnterTransition(slide);
//        getWindow().setReturnTransition(slide);

        Fade fade = new Fade();
        fade.setDuration(500);
        getWindow().setReturnTransition(fade);
    }
}
