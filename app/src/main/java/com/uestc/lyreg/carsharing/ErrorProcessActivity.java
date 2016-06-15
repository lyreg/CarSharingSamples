package com.uestc.lyreg.carsharing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.View;

public class ErrorProcessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_process);

        setupToolbar();
        setupWindowAnimations();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("异常流程");
    }

    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(500);

        getWindow().setEnterTransition(fade);
        getWindow().setReturnTransition(fade);
    }

    public void onButtonErrorClicked(View v) {
        int id = v.getId();
        if(id == R.id.button_error1) {

        } else if(id == R.id.button_error2) {

        } else if(id == R.id.button_error3) {

        }
    }

}
